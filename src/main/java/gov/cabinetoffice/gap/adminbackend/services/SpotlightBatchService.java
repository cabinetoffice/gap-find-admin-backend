package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.GetSpotlightBatchErrorCountDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.config.SpotlightConfigProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SendToSpotlightDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SpotlightSchemeDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.JsonParseException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SecretValueException;
import gov.cabinetoffice.gap.adminbackend.mappers.MandatoryQuestionsMapper;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SpotlightBatchService {

    public static final String ACCESS_TOKEN = "access_token";

    private final SpotlightBatchRepository spotlightBatchRepository;

    private final MandatoryQuestionsMapper mandatoryQuestionsMapper;

    private final SecretsManagerClient secretsManagerClient;

    private final SpotlightConfigProperties spotlightConfig;

    private final ObjectMapper jacksonObjectMapper;

    private final RestTemplate restTemplate;

    public boolean existsByStatusAndMaxBatchSize(SpotlightBatchStatus status, int maxSize) {
        return spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(status, maxSize);
    }

    // TODO refactor this - it can potentially return more than one result and will cause
    // errors
    public SpotlightBatch getSpotlightBatchWithStatus(SpotlightBatchStatus status, int maxSize) {
        return spotlightBatchRepository.findByStatusAndSpotlightSubmissionsSizeLessThan(status, maxSize).orElseThrow(
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
        return spotlightBatchRepository.findById(spotlightBatchId).orElseThrow(
                () -> new NotFoundException("A spotlight batch with id " + spotlightBatchId + " could not be found"));
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

    public void sendQueuedBatchesToSpotlight() {

        final List<SendToSpotlightDto> spotlightData = this
                .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

        // grab authorization header from AWS secrets manager
        final String accessToken = getAccessTokenFromSecretsManager();

        for (SendToSpotlightDto spotlightBatch : spotlightData) {
            sendBatchToSpotlight(spotlightBatch, accessToken);
        }
    }

    private void sendBatchToSpotlight(SendToSpotlightDto spotlightBatch, String accessToken) {
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Authorization", "Bearer " + accessToken);
        requestHeaders.add("Content-Type", "application/json");

        final String spotlightBatchAsJsonString = convertBatchToJsonString(spotlightBatch);

        final HttpEntity<String> requestEntity = new HttpEntity<>(spotlightBatchAsJsonString, requestHeaders);

        final String draftAssessmentsEndpoint = spotlightConfig.getSpotlightUrl()
                + "/services/apexrest/DraftAssessments";

        restTemplate.postForObject(draftAssessmentsEndpoint, requestEntity, String.class);
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

    private GetSpotlightBatchErrorCountDTO orderSpotlightErrorStatusesByPriority(
            List<SpotlightSubmission> filteredSubmissions) {
        int apiErrorCount = 0;
        int ggisErrorCount = 0;
        int validationErrorCount = 0;

        for (SpotlightSubmission submission : filteredSubmissions) {
            switch (SpotlightSubmissionStatus.valueOf(submission.getStatus())) {
                case SEND_ERROR:
                    apiErrorCount++;
                    break;
                case GGIS_ERROR:
                    ggisErrorCount++;
                    break;
                case VALIDATION_ERROR:
                    validationErrorCount++;
                    break;
                default:
                    break;
            }
        }
        if (apiErrorCount == 0 && ggisErrorCount == 0 && validationErrorCount == 0) {
            return GetSpotlightBatchErrorCountDTO.builder().errorCount(0).errorStatus("OK").errorFound(false).build();
        }

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
                .build();
    }

    public GetSpotlightBatchErrorCountDTO getSpotlightBatchErrorCount(Integer schemeId) {
        Pageable pageable = PageRequest.of(0, 1);
        final List<SpotlightBatch> spotlightBatches = spotlightBatchRepository.findMostRecentSpotlightBatch(pageable);
        final SpotlightBatch spotlightBatch = spotlightBatches.get(0);
        final List<SpotlightSubmission> filteredSubmissions = spotlightBatch.getSpotlightSubmissions().stream()
                .filter(s -> s.getGrantScheme().getId().equals(schemeId)).toList();

        if (filteredSubmissions.isEmpty()) {
            return GetSpotlightBatchErrorCountDTO.builder().errorCount(0).errorStatus("OK").errorFound(false).build();
        }
        return this.orderSpotlightErrorStatusesByPriority(filteredSubmissions);
    }
}
