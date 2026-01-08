package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.config.SpotlightConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.SpotlightQueueConfigProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SendToSpotlightDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SpotlightSchemeDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response.DraftAssessmentResponseDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response.SpotlightResponseDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response.SpotlightResponseResultsDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.GetSpotlightBatchErrorCountDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.JsonParseException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SecretValueException;
import gov.cabinetoffice.gap.adminbackend.mappers.MandatoryQuestionsMapper;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightBatchRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static gov.cabinetoffice.gap.adminbackend.enums.DraftAssessmentResponseDtoStatus.SUCCESS;

@Service
@RequiredArgsConstructor
@Log4j2
public class SpotlightBatchService {

    public static final String ACCESS_TOKEN = "access_token";

    private static final String RESPONSE_MESSAGE_406_SCHEME_NOT_EXIST = "Scheme Does Not Exist";

    private static final String RESPONSE_MESSAGE_409_FIELD_MISSING = "Required fields are missing";

    private static final String RESPONSE_MESSAGE_409_LENGTH = "data value too large";

    private final SpotlightBatchRepository spotlightBatchRepository;

    private final MandatoryQuestionsMapper mandatoryQuestionsMapper;

    private final SecretsManagerClient secretsManagerClient;

    private final RestTemplate restTemplate;

    private final SpotlightSubmissionRepository spotlightSubmissionRepository;

    private final SpotlightConfigProperties spotlightConfig;

    private final ObjectMapper jacksonObjectMapper;

    private final SpotlightQueueConfigProperties spotlightQueueProperties;

    private final AmazonSQS amazonSqs;

    private final SpotlightSubmissionService spotlightSubmissionService;

    private final SnsService snsService;

    public boolean existsByStatusAndMaxBatchSize(SpotlightBatchStatus status, int maxSize) {
        return spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(status, maxSize);
    }

    public SpotlightBatch getMostRecentSpotlightBatchWithStatus(SpotlightBatchStatus status, int maxSize) {
        return spotlightBatchRepository.findByStatusAndSpotlightSubmissionsSizeLessThan(status, maxSize).orElseThrow(
                () -> new NotFoundException("A spotlight batch with status " + status + " could not be found"));
    }

    public SpotlightBatch getSpotlightBatchById(UUID spotlightBatchId) {
        return spotlightBatchRepository.findById(spotlightBatchId).orElseThrow(
                () -> new NotFoundException("A spotlight batch with id " + spotlightBatchId + " could not be found"));
    }

    public SpotlightBatch createSpotlightBatch() {
        return spotlightBatchRepository.save(SpotlightBatch.builder().version(1).lastUpdated(Instant.now()).build());
    }

    public SpotlightBatch addSpotlightSubmissionToSpotlightBatch(SpotlightSubmission spotlightSubmission,
            UUID spotlightBatchId) {
        final SpotlightBatch spotlightBatch = getSpotlightBatch(spotlightBatchId);
        final List<SpotlightSubmission> existingSpotlightSubmissions = spotlightBatch.getSpotlightSubmissions();

        // Check if submission is already in the batch (idempotent operation)
        boolean alreadyExists = existingSpotlightSubmissions != null
                && existingSpotlightSubmissions.stream()
                        .anyMatch(s -> s.getId().equals(spotlightSubmission.getId()));

        if (alreadyExists) {
            log.info("Submission {} is already in batch {}. Returning existing batch (idempotent).",
                    spotlightSubmission.getId(), spotlightBatchId);
            return spotlightBatch;
        }

        final List<SpotlightBatch> existingSpotlightBatches = spotlightSubmission.getBatches();

        existingSpotlightSubmissions.add(spotlightSubmission);
        existingSpotlightBatches.add(spotlightBatch);

        spotlightBatch.setSpotlightSubmissions(existingSpotlightSubmissions);
        spotlightSubmission.setBatches(existingSpotlightBatches);

        return spotlightBatchRepository.save(spotlightBatch);
    }

    private SpotlightBatch getSpotlightBatch(UUID spotlightBatchId) {
        return spotlightBatchRepository.findById(spotlightBatchId).orElseThrow(
                () -> new NotFoundException("A spotlight batch with id " + spotlightBatchId + " could not be found"));
    }

    public SpotlightBatch getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId(String gapId) {
        List<SpotlightBatch> batches = spotlightBatchRepository
                .findByStatusAndSpotlightSubmissions_MandatoryQuestions_GapId(SpotlightBatchStatus.QUEUED, gapId);
        
        if (batches.isEmpty()) {
            throw new NotFoundException(
                    "A spotlight batch with spotlightSubmission for mandatory question with gap id " + gapId
                            + " could not be found");
        }
        
        if (batches.size() > 1) {
            log.warn("Found {} QUEUED batches for gapId {}. Using the most recent one.", batches.size(), gapId);
        }
        
        // Return the first batch (already ordered by created DESC in the query)
        return batches.get(0);
    }

    public List<SpotlightBatch> getSpotlightBatchesByStatus(SpotlightBatchStatus status) {
        return spotlightBatchRepository.findByStatus(status).orElse(new ArrayList<>());
    }

    public List<SendToSpotlightDto> generateSendToSpotlightDtosList(SpotlightBatchStatus status) {

        final List<SendToSpotlightDto> sendToSpotlightDtos = new ArrayList<>();
        final List<SpotlightBatch> spotlightBatches = getSpotlightBatchesByStatus(status);

        // Track submissions we've already included to prevent duplicates across batches
        final Set<UUID> processedSubmissionIds = new HashSet<>();

        for (SpotlightBatch spotlightBatch : spotlightBatches) {
            try {
                final List<SpotlightSubmission> submissions = spotlightBatch.getSpotlightSubmissions();
                
                // Filter: Only non-SENT submissions that haven't been processed yet
                final List<SpotlightSubmission> submissionsToProcess = submissions.stream()
                        .filter(sub -> !SpotlightSubmissionStatus.SENT.toString().equals(sub.getStatus()))
                        .filter(sub -> !processedSubmissionIds.contains(sub.getId()))
                        .collect(Collectors.toList());
                
                if (submissionsToProcess.isEmpty()) {
                    log.info("Batch {} skipped - no eligible submissions (all are SENT or already processed)", 
                            spotlightBatch.getId());
                    // Mark batch as SUCCESS since all submissions are already SENT
                    if (submissions.stream().allMatch(s -> SpotlightSubmissionStatus.SENT.toString().equals(s.getStatus()))) {
                        spotlightBatch.setStatus(SpotlightBatchStatus.SUCCESS);
                        spotlightBatch.setLastUpdated(Instant.now());
                        spotlightBatchRepository.save(spotlightBatch);
                    }
                    continue;
                }
                
                // Mark these submissions as processed to prevent duplicates in other batches
                submissionsToProcess.forEach(sub -> processedSubmissionIds.add(sub.getId()));
                
                log.info("Processing batch {} with {} submissions (filtered from {})", 
                        spotlightBatch.getId(), submissionsToProcess.size(), submissions.size());

                final List<SpotlightSchemeDto> schemes = new ArrayList<>();
                addSpotlightSchemeDtoToListFiltered(spotlightBatch, schemes, submissionsToProcess);

                final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().schemes(schemes).build();
                sendToSpotlightDtos.add(sendToSpotlightDto);
            }
            catch (Exception e) {
                log.error("An exception occurred when generating spotlight data for batch {}", spotlightBatch.getId(),
                        e);
            }
        }

        return sendToSpotlightDtos;
    }

    /**
     * Adds spotlight scheme DTOs to the list using a pre-filtered list of submissions.
     * This prevents duplicate submissions being sent to Spotlight.
     */
    private void addSpotlightSchemeDtoToListFiltered(SpotlightBatch spotlightBatch, 
            List<SpotlightSchemeDto> schemes, List<SpotlightSubmission> submissionsToProcess) {
        
        final List<String> uniqueSchemeIds = getUniqueSchemeIds(submissionsToProcess);
        log.info("uniqueSchemeIds for batch {}: {}", spotlightBatch.getId(), uniqueSchemeIds);

        uniqueSchemeIds.stream()
                .map(uniqueSchemeId -> generateSchemeDto(uniqueSchemeId, submissionsToProcess))
                .forEach(schemes::add);
    }

    public List<String> getUniqueSchemeIds(List<SpotlightSubmission> spotlightSubmissions) {
        return spotlightSubmissions.stream()
                .map(submission -> submission.getMandatoryQuestions().getSchemeEntity().getGgisIdentifier()).distinct()
                .toList();

    }

    private List<SpotlightSubmission> getSpotlightSubmissionByGGisIdentifier(String uniqueSchemeId,
            List<SpotlightSubmission> spotlightSubmissions) {
        return spotlightSubmissions.stream().filter(submission -> submission.getMandatoryQuestions().getSchemeEntity()
                .getGgisIdentifier().equals(uniqueSchemeId)).toList();
    }

    protected void addSpotlightSchemeDtoToList(SpotlightBatch spotlightBatch, List<SpotlightSchemeDto> schemes) {

        final List<SpotlightSubmission> spotlightSubmissions = spotlightBatch.getSpotlightSubmissions();

        // get all the schemeId present in the submissions
        final List<String> uniqueSchemeIds = getUniqueSchemeIds(spotlightSubmissions);

        log.info("uniqueSchemeIds: {}", uniqueSchemeIds);

        // for each scheme ggis id build a spotlightSchemeDto
        uniqueSchemeIds.stream().map(uniqueSchemeId -> generateSchemeDto(uniqueSchemeId, spotlightSubmissions))
                .forEach(schemes::add);
    }

    private SpotlightSchemeDto generateSchemeDto(String schemeId, List<SpotlightSubmission> spotlightSubmissions) {
        final List<DraftAssessmentDto> draftAssessments = generateDraftAssessmentsFromMandatoryQuestions(schemeId,
                spotlightSubmissions);

        return SpotlightSchemeDto.builder().ggisSchemeId(schemeId).draftAssessments(draftAssessments).build();
    }

    @NotNull
    private List<DraftAssessmentDto> generateDraftAssessmentsFromMandatoryQuestions(String uniqueSchemeId,
            List<SpotlightSubmission> spotlightSubmissions) {

        final List<SpotlightSubmission> filteredSubmissions = getSpotlightSubmissionByGGisIdentifier(uniqueSchemeId,
                spotlightSubmissions);

        return filteredSubmissions.stream().map(submission -> mandatoryQuestionsMapper
                .mandatoryQuestionsToDraftAssessmentDto(submission.getMandatoryQuestions())).toList();
    }

    public void sendQueuedBatchesToSpotlightAndProcessThem() {
        final List<SendToSpotlightDto> spotlightData = this
                .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

        final String accessToken = getAccessTokenFromSecretsManager();

        for (SendToSpotlightDto spotlightBatch : spotlightData) {
            try {
                final SpotlightResponseResultsDto spotlightResponses = sendBatchToSpotlight(spotlightBatch,
                        accessToken);
                processSpotlightResponse(spotlightBatch, spotlightResponses);
            }
            catch (Exception e) {
                log.error("An exception occurred while sending batches to spotlight", e);
            }
        }
    }

    public void processSpotlightResponse(SendToSpotlightDto spotlightBatchDto,
            SpotlightResponseResultsDto spotlightResponses) {

        AtomicInteger errorCount = new AtomicInteger();

        if (spotlightResponses.getResults() == null) {

            updateSpotlightSubmissionStatus(spotlightBatchDto, SpotlightSubmissionStatus.SEND_ERROR);
            addMessageToQueue(spotlightBatchDto);
            updateSpotlightBatchStatus(spotlightBatchDto, SpotlightBatchStatus.FAILURE);

        }
        else {

            for (SpotlightResponseDto spotlightResponse : spotlightResponses.getResults()) {
                processSpotlightResults(spotlightResponse, errorCount);
            }

            if (errorCount.get() == 0) {
                updateSpotlightBatchStatus(spotlightBatchDto, SpotlightBatchStatus.SUCCESS);
            }
            else {
                updateSpotlightBatchStatus(spotlightBatchDto, SpotlightBatchStatus.FAILURE);
            }
        }
    }

    private void processSpotlightResults(SpotlightResponseDto spotlightResponse, AtomicInteger errorCount) {
        for (DraftAssessmentResponseDto draftAssessmentResponse : spotlightResponse.getDraftAssessmentsResults()) {
            SpotlightSubmission spotlightSubmission = getSpotlightSubmissionByApplicationNumber(
                    draftAssessmentResponse.getApplicationNumber());

            if (isSuccess(draftAssessmentResponse)) {
                spotlightSubmission.setStatus(SpotlightSubmissionStatus.SENT.toString());
            }
            else {
                handleError(spotlightSubmission, draftAssessmentResponse);
                errorCount.incrementAndGet();
            }

            updateSpotlightSubmission(spotlightSubmission);
        }
    }

    private boolean isSuccess(DraftAssessmentResponseDto draftAssessmentResponseDto) {
        return draftAssessmentResponseDto.getStatus().equals(SUCCESS.toString());
    }

    private void handleError(SpotlightSubmission spotlightSubmission,
            DraftAssessmentResponseDto draftAssessmentResponse) {
        if (draftAssessmentResponse.getMessage() != null) {
            handleErrorMessage(spotlightSubmission, draftAssessmentResponse.getMessage());
        }
        else {
            spotlightSubmission.setStatus(SpotlightSubmissionStatus.SEND_ERROR.toString());
            sendMessageToQueue(spotlightSubmission);
        }
    }

    private void handleErrorMessage(SpotlightSubmission spotlightSubmission, String errorMessage) {
        if (errorMessage.contains(RESPONSE_MESSAGE_406_SCHEME_NOT_EXIST)) {
            spotlightSubmission.setStatus(SpotlightSubmissionStatus.GGIS_ERROR.toString());
            // DON'T re-queue GGIS errors - they require manual intervention to fix the scheme ID
            // Re-queuing these creates an infinite loop of failures
            log.warn("GGIS_ERROR for submission {} - scheme doesn't exist in Spotlight. " +
                    "Manual intervention required to fix the GGIS identifier.", spotlightSubmission.getId());
        }
        else if (errorMessage.contains(RESPONSE_MESSAGE_409_FIELD_MISSING)
                || errorMessage.contains(RESPONSE_MESSAGE_409_LENGTH)) {
            // has a validation error - DON'T re-queue as these need manual data fixes
            spotlightSubmission.setStatus(SpotlightSubmissionStatus.VALIDATION_ERROR.toString());

            log.info("Sending Spotlight validation support email using SNS for status code: 409");
            final String snsResponse = snsService.spotlightValidationError();
            log.info(snsResponse);
            log.warn("VALIDATION_ERROR for submission {} - {}. Manual intervention required.", 
                    spotlightSubmission.getId(), errorMessage);
        }
    }

    private void updateSpotlightSubmission(SpotlightSubmission spotlightSubmission) {
        spotlightSubmission.setLastUpdated(Instant.now());
        spotlightSubmission.setLastSendAttempt(Instant.now());
        spotlightSubmissionRepository.save(spotlightSubmission);
    }

    private SpotlightSubmission getSpotlightSubmissionByApplicationNumber(String applicationNumber) {
        return spotlightSubmissionService.getSpotlightSubmissionByMandatoryQuestionGapId(applicationNumber);

    }

    public SpotlightResponseResultsDto sendBatchToSpotlight(SendToSpotlightDto spotlightBatch, String accessToken) {
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Authorization", "Bearer " + accessToken);
        requestHeaders.add("Content-Type", "application/json");

        final String spotlightBatchAsJsonString = convertBatchToJsonString(spotlightBatch);

        final HttpEntity<String> requestEntity = new HttpEntity<>(spotlightBatchAsJsonString, requestHeaders);

        // Construct the endpoint URL - avoid duplicating the path if already in config
        String baseUrl = spotlightConfig.getSpotlightUrl();
        final String draftAssessmentsEndpoint = baseUrl.contains("/services/apexrest/DraftAssessments")
                ? baseUrl
                : baseUrl + "/services/apexrest/DraftAssessments";

        SpotlightResponseResultsDto list = SpotlightResponseResultsDto.builder().build();

        try {
            log.info("Spotlight request endpoint: {}", draftAssessmentsEndpoint);
            log.info("Spotlight request body: {}", requestEntity.toString());

            final ResponseEntity<String> response = restTemplate.postForEntity(draftAssessmentsEndpoint, requestEntity,
                    String.class);

            list = mapToDto(response.getBody());
        }
        catch (HttpClientErrorException e) { // 4xx codes

            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                log.info("Sending Spotlight OAuth disconnected support email using SNS for status code: "
                        + e.getStatusCode());
                final String snsResponse = snsService.spotlightOAuthDisconnected();
                log.info(snsResponse);
            }
            // if 406 or 409, map the response as we would need to handle every
            // spotlightSubmission status
            if (e.getStatusCode().equals(HttpStatus.NOT_ACCEPTABLE) || e.getStatusCode().equals(HttpStatus.CONFLICT)) {
                list = mapToDto(e.getResponseBodyAsString());
            }

            log.error("Hitting {} returned status code {} with body {}", draftAssessmentsEndpoint, e.getStatusCode(),
                    e.getResponseBodyAsString());
        }
        catch (HttpServerErrorException e) {
            if (e.getStatusCode().is5xxServerError()) { // 5xx codes
                log.info("Sending spotlight API error support email using SNS for status code: " + e.getStatusCode());
                final String snsResponse = snsService.spotlightApiError();
                log.info(snsResponse);

                log.error("Hitting {} returned status code {} with body {}", draftAssessmentsEndpoint,
                        e.getStatusCode(), e.getResponseBodyAsString());
            }
        }

        return list;
    }

    public void updateSpotlightBatchStatus(SendToSpotlightDto spotlightBatchDto, SpotlightBatchStatus status) {
        final SpotlightBatch spotlightBatch = getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId(
                spotlightBatchDto.getSchemes().get(0).getDraftAssessments().get(0).getApplicationNumber());

        spotlightBatch.setStatus(status);
        spotlightBatch.setLastUpdated(Instant.now());
        spotlightBatch.setLastSendAttempt(Instant.now());

        spotlightBatchRepository.save(spotlightBatch);
    }

    public void updateSpotlightSubmissionStatus(SendToSpotlightDto spotlightBatchDto,
            SpotlightSubmissionStatus status) {
        final SpotlightBatch spotlightBatch = getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId(
                spotlightBatchDto.getSchemes().get(0).getDraftAssessments().get(0).getApplicationNumber());

        List<SpotlightSubmission> spotlightSubmissions = spotlightBatch.getSpotlightSubmissions();

        spotlightSubmissions.forEach(spotlightSubmission -> {
            spotlightSubmission.setStatus(status.toString());
            spotlightSubmission.setLastUpdated(Instant.now());
            spotlightSubmission.setLastSendAttempt(Instant.now());
        });

        spotlightBatch.setSpotlightSubmissions(spotlightSubmissions);

        spotlightBatchRepository.save(spotlightBatch);
    }

    public void addMessageToQueue(SendToSpotlightDto spotlightBatchDto) {
        final SpotlightBatch spotlightBatch = getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId(
                spotlightBatchDto.getSchemes().get(0).getDraftAssessments().get(0).getApplicationNumber());

        final List<SpotlightSubmission> spotlightSubmissions = spotlightBatch.getSpotlightSubmissions();

        spotlightSubmissions.forEach(this::sendMessageToQueue);
    }

    public void sendMessageToQueue(SpotlightSubmission spotlightSubmission) {
        final UUID messageId = UUID.randomUUID();

        final SendMessageRequest messageRequest = new SendMessageRequest()
                .withQueueUrl(spotlightQueueProperties.getQueueUrl()).withMessageGroupId(messageId.toString())
                .withMessageBody(spotlightSubmission.getId().toString())
                .withMessageDeduplicationId(messageId.toString());

        amazonSqs.sendMessage(messageRequest);
        log.info("Message sent to queue for spotlight_submission {}", spotlightSubmission.getId().toString());
    }

    private SpotlightResponseResultsDto mapToDto(String responseBodyAsString) {
        try {
            final SpotlightResponseDto[] spotlightResponseDtos = jacksonObjectMapper.readValue(responseBodyAsString,
                    SpotlightResponseDto[].class);
            return SpotlightResponseResultsDto.builder().results(Arrays.asList(spotlightResponseDtos)).build();
        }
        catch (JsonProcessingException e) {
            log.error("Could not convert dto to json string ", e);
            throw new JsonParseException("could not convert spotlight response to dto");
        }
    }

    private String convertBatchToJsonString(SendToSpotlightDto spotlightBatch) {
        try {
            return jacksonObjectMapper.writeValueAsString(spotlightBatch);
        }
        catch (JsonProcessingException e) {
            log.error("Could not convert dto to json string ", e);
            throw new JsonParseException("could not convert dto to json string");
        }
    }

    private String getAccessTokenFromSecretsManager() {
        log.info("Getting secret {}...", spotlightConfig.getSecretName());

        final GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(spotlightConfig.getSecretName()).build();

        log.info("Request to AWS secrets manager: {}", valueRequest.toString());

        final GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);

        log.info("Response from AWS secrets manager: {}", valueResponse.toString());

        return getSecretValue(ACCESS_TOKEN, valueResponse.secretString());
    }

    private String getSecretValue(String objectKey, String secretString) {
        try {
            final JsonNode apiNode = jacksonObjectMapper.readTree(secretString).get(objectKey);

            return jacksonObjectMapper.readValue(apiNode.toString(), String.class);
        }
        catch (JsonProcessingException e) {
            log.error("could not read json value ", e);
            throw new SecretValueException();
        }
    }

    private GetSpotlightBatchErrorCountDTO orderSpotlightErrorStatusesByPriority(
            List<SpotlightSubmission> filteredSubmissions, boolean hasValidationError) {
        int apiErrorCount = 0;
        int ggisErrorCount = 0;
        int validationErrorCount = 0;

        for (SpotlightSubmission submission : filteredSubmissions) {
            switch (SpotlightSubmissionStatus.valueOf(submission.getStatus())) {
                case SEND_ERROR -> apiErrorCount++;
                case GGIS_ERROR -> ggisErrorCount++;
                case VALIDATION_ERROR -> validationErrorCount++;
                default -> {
                    // do nothing as no errors found
                }
            }
        }
        if (apiErrorCount == 0 && ggisErrorCount == 0 && validationErrorCount == 0) {
            return GetSpotlightBatchErrorCountDTO.builder().errorCount(0).errorStatus("OK").errorFound(false)
                    .isValidationErrorPresent(hasValidationError).build();
        }

        log.info("There are {} api errors", apiErrorCount);
        log.info("There are {} ggis errors", ggisErrorCount);
        log.info("There are {} validation errors", validationErrorCount);

        // Priority order: API > GGIS > VALIDATION. Validation is the lowest/default
        // priority
        // and will be overwritten if any higher-priority statuses exist.
        int errorCount = validationErrorCount;
        String errorStatus = "VALIDATION";

        if (apiErrorCount > 0) {
            errorCount = apiErrorCount;
            errorStatus = "API";
        }
        else if (ggisErrorCount > 0) {
            errorCount = ggisErrorCount;
            errorStatus = "GGIS";
        }
        return GetSpotlightBatchErrorCountDTO.builder().errorCount(errorCount).errorStatus(errorStatus).errorFound(true)
                .isValidationErrorPresent(hasValidationError).build();
    }

    public GetSpotlightBatchErrorCountDTO getSpotlightBatchErrorCount(Integer schemeId) {
        final List<SpotlightSubmission> filteredSubmissions = getSpotlightBatchSubmissionsBySchemeId(schemeId);
        final boolean hasValidationError = spotlightSubmissionService.existBySchemeIdAndStatus(schemeId,
                SpotlightSubmissionStatus.VALIDATION_ERROR);

        if (filteredSubmissions.isEmpty()) {

            log.info("No spotlight_submission for scheme id {} found in the latest batch", schemeId);

            return GetSpotlightBatchErrorCountDTO.builder().errorCount(0).errorStatus("OK").errorFound(false)
                    .isValidationErrorPresent(hasValidationError).build();
        }

        log.info("Found {} spotlight_submission for scheme id {},  in the latest batch", filteredSubmissions.size(),
                schemeId);

        return this.orderSpotlightErrorStatusesByPriority(filteredSubmissions, hasValidationError);
    }

    @NotNull
    private List<SpotlightSubmission> getSpotlightBatchSubmissionsBySchemeId(Integer schemeId) {
        final Pageable pageable = PageRequest.of(0, 1);
        final List<SpotlightBatch> spotlightBatches = spotlightBatchRepository
                .findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable);

        if (!spotlightBatches.isEmpty()) {

            final SpotlightBatch spotlightBatch = spotlightBatches.get(0);

            log.info("Last sent spotlight batch has id {}", spotlightBatch.getId());

            return spotlightBatch.getSpotlightSubmissions().stream()
                    .filter(s -> s.getGrantScheme().getId().equals(schemeId)).toList();
        }

        log.info("No spotlight batch found in the db");
        return List.of();

    }

}
