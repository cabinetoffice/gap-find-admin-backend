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
import java.util.List;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.enums.DraftAssessmentResponseDtoStatus.FAILURE;
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

    public boolean existsByStatusAndMaxBatchSize(SpotlightBatchStatus status, int maxSize) {
        return spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(status, maxSize);
    }

    // TODO refactor this - it can potentially return more than one result and will cause
    // errors
    public SpotlightBatch getSpotlightBatchWithStatus(SpotlightBatchStatus status, int maxSize) {
        return spotlightBatchRepository.findByStatusAndSpotlightSubmissionsSizeLessThan(status, maxSize)
            .orElseThrow(
                    () -> new NotFoundException("A spotlight batch with status " + status + " could not be found"));
    }

    public SpotlightBatch createSpotlightBatch() {
        return spotlightBatchRepository.save(SpotlightBatch.builder().version(1).lastUpdated(Instant.now()).build());
    }

    public SpotlightBatch addSpotlightSubmissionToSpotlightBatch(SpotlightSubmission spotlightSubmission,
            UUID spotlightBatchId) {
        final SpotlightBatch spotlightBatch = getSpotlightBatch(spotlightBatchId);
        final List<SpotlightSubmission> existingSpotlightSubmissions = spotlightBatch.getSpotlightSubmissions();
        final List<SpotlightBatch> existingSpotlightBatches = spotlightSubmission.getBatches();

        existingSpotlightSubmissions.add(spotlightSubmission);
        existingSpotlightBatches.add(spotlightBatch);

        spotlightBatch.setSpotlightSubmissions(existingSpotlightSubmissions);
        spotlightSubmission.setBatches(existingSpotlightBatches);

        return spotlightBatchRepository.save(spotlightBatch);
    }

    private SpotlightBatch getSpotlightBatch(UUID spotlightBatchId) {
        return spotlightBatchRepository.findById(spotlightBatchId)
            .orElseThrow(() -> new NotFoundException(
                    "A spotlight batch with id " + spotlightBatchId + " could not be found"));
    }

    public SpotlightBatch getSpotlightBatchByMandatoryQuestionGapId(String gapId) {
        return spotlightBatchRepository.findBySpotlightSubmissions_MandatoryQuestions_GapId(gapId)
            .orElseThrow(() -> new NotFoundException(
                    "A spotlight batch with spotlightSubmission for mandatory question with gap id " + gapId
                            + " could not be found"));
    }

    public List<SpotlightBatch> getSpotlightBatchesByStatus(SpotlightBatchStatus status) {
        return spotlightBatchRepository.findByStatus(status).orElse(new ArrayList<>());
    }

    public List<SendToSpotlightDto> generateSendToSpotlightDtosList(SpotlightBatchStatus status) {

        final List<SendToSpotlightDto> sendToSpotlightDtos = new ArrayList<>();
        final List<SpotlightBatch> spotlightBatches = getSpotlightBatchesByStatus(status);

        // gav comment - think we could turn this loop into a stream to make the method
        // smaller
        for (SpotlightBatch spotlightBatch : spotlightBatches) {

            // gav comment - we could make this method return a list then we don't need to
            // initialise and pass in an empty one
            // since all we do is add to it anyway
            final List<SpotlightSchemeDto> schemes = new ArrayList<>();
            addSpotlightSchemeDtoToList(spotlightBatch, schemes);

            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().schemes(schemes).build();
            sendToSpotlightDtos.add(sendToSpotlightDto);
        }

        return sendToSpotlightDtos;
    }

    public List<String> getUniqueSchemeIds(List<SpotlightSubmission> spotlightSubmissions) {
        return spotlightSubmissions.stream()
            .map(submission -> submission.getMandatoryQuestions().getSchemeEntity().getGgisIdentifier())
            .distinct()
            .toList();

    }

    private List<SpotlightSubmission> getSpotlightSubmissionByGGisIdentifier(String uniqueSchemeId,
            List<SpotlightSubmission> spotlightSubmissions) {
        return spotlightSubmissions.stream()
            .filter(submission -> submission.getMandatoryQuestions()
                .getSchemeEntity()
                .getGgisIdentifier()
                .equals(uniqueSchemeId))
            .toList();
    }

    protected void addSpotlightSchemeDtoToList(SpotlightBatch spotlightBatch, List<SpotlightSchemeDto> schemes) {

        final List<SpotlightSubmission> spotlightSubmissions = spotlightBatch.getSpotlightSubmissions();

        // get all the schemeId present in the submissions
        final List<String> uniqueSchemeIds = getUniqueSchemeIds(spotlightSubmissions);

        log.info("uniqueSchemeIds: {}", uniqueSchemeIds);

        // for each scheme ggis id build a spotlightSchemeDto
        uniqueSchemeIds.stream()
            .map(uniqueSchemeId -> generateSchemeDto(uniqueSchemeId, spotlightSubmissions))
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

        return filteredSubmissions.stream()
            .map(submission -> mandatoryQuestionsMapper
                .mandatoryQuestionsToDraftAssessmentDto(submission.getMandatoryQuestions()))
            .toList();

    }

    public void sendQueuedBatchesToSpotlightAndProcessThem() {

        final List<SendToSpotlightDto> spotlightData = this
            .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

        // grab authorization header from AWS secrets manager
        final String accessToken = getAccessTokenFromSecretsManager();

        for (SendToSpotlightDto spotlightBatch : spotlightData) {

            final SpotlightResponseResultsDto spotlightResponses = sendBatchToSpotlight(spotlightBatch, accessToken);

            processSpotlightResponse(spotlightBatch, spotlightResponses);

        }
    }

    private void processSpotlightResponse(SendToSpotlightDto spotlightBatch,
            SpotlightResponseResultsDto spotlightResponses) {

        if (spotlightResponses.getResults() == null) {

            updateSpotlightBatchStatus(spotlightBatch, SpotlightBatchStatus.FAILURE);
            updateSpotlightSubmissionStatus(spotlightBatch, SpotlightSubmissionStatus.SEND_ERROR);
            addMessageToQueue(spotlightBatch);
        }
        else {
            int errorCount = 0;

            for (SpotlightResponseDto spotlightResponse : spotlightResponses.getResults()) {
                for (DraftAssessmentResponseDto draftAssessmentResponse : spotlightResponse
                    .getDraftAssessmentsResults()) {

                    final SpotlightSubmission spotlightSubmission = spotlightSubmissionService
                        .getSpotligtSubmissionByMandatoryQuestionGapId(draftAssessmentResponse.getApplicationNumber());

                    if (draftAssessmentResponse.getStatus().equals(SUCCESS.toString())) {
                        spotlightSubmission.setStatus(SpotlightSubmissionStatus.SENT.toString());
                    }

                    if (draftAssessmentResponse.getStatus().equals(FAILURE.toString())
                            && draftAssessmentResponse.getMessage() != null) {

                        if (draftAssessmentResponse.getMessage().contains(RESPONSE_MESSAGE_406_SCHEME_NOT_EXIST)) {

                            errorCount++;

                            spotlightSubmission.setStatus(SpotlightSubmissionStatus.GGIS_ERROR.toString());

                            sendMessageToQueue(spotlightSubmission);

                        }

                        if (draftAssessmentResponse.getMessage().contains(RESPONSE_MESSAGE_409_FIELD_MISSING)
                                || draftAssessmentResponse.getMessage().contains(RESPONSE_MESSAGE_409_LENGTH)) {

                            errorCount++;

                            spotlightSubmission.setStatus(SpotlightSubmissionStatus.VALIDATION_ERROR.toString());

                        }
                    }

                    spotlightSubmission.setLastUpdated(Instant.now());
                    spotlightSubmission.setLastSendAttempt(Instant.now());
                    spotlightSubmissionRepository.save(spotlightSubmission);
                }

            }

            // if there were no errors, update the batch status to success, otherwise to
            // failure
            final SpotlightBatchStatus spotlightBatchStatus = errorCount == 0 ? SpotlightBatchStatus.SUCCESS
                    : SpotlightBatchStatus.FAILURE;

            updateSpotlightBatchStatus(spotlightBatch, spotlightBatchStatus);
        }
    }

    private SpotlightResponseResultsDto sendBatchToSpotlight(SendToSpotlightDto spotlightBatch, String accessToken) {
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Authorization", "Bearer " + accessToken);
        requestHeaders.add("Content-Type", "application/json");

        final String spotlightBatchAsJsonString = convertBatchToJsonString(spotlightBatch);

        final HttpEntity<String> requestEntity = new HttpEntity<>(spotlightBatchAsJsonString, requestHeaders);

        final String draftAssessmentsEndpoint = spotlightConfig.getSpotlightUrl()
                + "/services/apexrest/DraftAssessments";

        SpotlightResponseResultsDto list = SpotlightResponseResultsDto.builder().build();

        try {
            final ResponseEntity<String> response = restTemplate.postForEntity(draftAssessmentsEndpoint, requestEntity,
                    String.class);

            list = mapToDto(response.getBody());
        }

        catch (HttpClientErrorException e) { // 4xx codes

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

                log.error("Hitting {} returned status code {} with body {}", draftAssessmentsEndpoint,
                        e.getStatusCode(), e.getResponseBodyAsString());
            }
        }

        return list;
    }

    private void updateSpotlightBatchStatus(SendToSpotlightDto spotlightBatchDto, SpotlightBatchStatus status) {
        final SpotlightBatch spotlightBatch = getSpotlightBatchByMandatoryQuestionGapId(
                spotlightBatchDto.getSchemes().get(0).getDraftAssessments().get(0).getApplicationNumber());

        spotlightBatch.setStatus(status);
        spotlightBatch.setLastUpdated(Instant.now());
        spotlightBatch.setLastSendAttempt(Instant.now());

        spotlightBatchRepository.save(spotlightBatch);
    }

    private void updateSpotlightSubmissionStatus(SendToSpotlightDto spotlightBatchDto,
            SpotlightSubmissionStatus status) {
        final SpotlightBatch spotlightBatch = getSpotlightBatchByMandatoryQuestionGapId(
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

    private void addMessageToQueue(SendToSpotlightDto spotlightBatchDto) {
        final SpotlightBatch spotlightBatch = getSpotlightBatchByMandatoryQuestionGapId(
                spotlightBatchDto.getSchemes().get(0).getDraftAssessments().get(0).getApplicationNumber());

        final List<SpotlightSubmission> spotlightSubmissions = spotlightBatch.getSpotlightSubmissions();

        spotlightSubmissions.forEach(this::sendMessageToQueue);
    }

    private void sendMessageToQueue(SpotlightSubmission spotlightSubmission) {
        final UUID messageId = UUID.randomUUID();

        final SendMessageRequest messageRequest = new SendMessageRequest()
            .withQueueUrl(spotlightQueueProperties.getQueueUrl())
            .withMessageGroupId(messageId.toString())
            .withMessageBody(spotlightSubmission.getId().toString())
            .withMessageDeduplicationId(messageId.toString());

        amazonSqs.sendMessage(messageRequest);
    }

    private SpotlightResponseResultsDto mapToDto(String responseBodyAsString) {
        try {
            return jacksonObjectMapper.readValue(responseBodyAsString, SpotlightResponseResultsDto.class);
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
            .secretId(spotlightConfig.getSecretName())
            .build();
        final GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);

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

}
