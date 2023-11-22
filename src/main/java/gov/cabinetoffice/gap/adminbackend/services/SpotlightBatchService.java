package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SendToSpotlightDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SpotlightSchemeDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.MandatoryQuestionsMapper;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpotlightBatchService {

    private final SpotlightBatchRepository spotlightBatchRepository;
    private final MandatoryQuestionsMapper mandatoryQuestionsMapper;

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

    public SendToSpotlightDto generateSendToSpotlightDto() {
        // get all the batches
        final List<SpotlightBatch> spotlightBatches = getSpotlightBatchesByStatus(SpotlightBatchStatus.QUEUED);
        final List<SpotlightSchemeDto> schemesDto = new ArrayList<>();

        for (SpotlightBatch spotlightBatch : spotlightBatches) {

            // get each batch's submissions

            final List<SpotlightSubmission> spotlightSubmissions = spotlightBatch.getSpotlightSubmissions();

            // get all the schemeId present in the submissions
            final List<String> uniqueSchemeIds = spotlightSubmissions.stream()
                    .map(submission -> submission.getMandatoryQuestions().getSchemeEntity().getGgisIdentifier())
                    .distinct().toList();

            // for each scheme ggis id build a spotlightSchemeDto
            for (String uniqueSchemeId : uniqueSchemeIds) {
                final SpotlightSchemeDto schemeDto = SpotlightSchemeDto.builder().GGISSchemeId(uniqueSchemeId).build();
                final List<SpotlightSubmission> filteredBySchemeIdSubmissions = spotlightSubmissions.stream()
                        .filter(submission -> submission.getMandatoryQuestions().getSchemeEntity().getGgisIdentifier()
                                .equals(uniqueSchemeId))
                        .toList();

                for (SpotlightSubmission submission : filteredBySchemeIdSubmissions) {
                    DraftAssessmentDto draftAssessmentDto = mandatoryQuestionsMapper.mandatoryQuestionsToDraftAssessmentDto(submission.getMandatoryQuestions());
                    schemeDto.getDraftAssessments().add(draftAssessmentDto);
                }

                schemesDto.add(schemeDto);
            }
        }
        return SendToSpotlightDto.builder().Schemes(schemesDto).build();

    }

}
