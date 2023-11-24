package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.GetSpotlightBatchErrorCountDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpotlightBatchService {

    private final SpotlightBatchRepository spotlightBatchRepository;

    public boolean existsByStatusAndMaxBatchSize(SpotlightBatchStatus status, int maxSize) {
        return spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(status, maxSize);
    }

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
        // TODO: This will need to be refactored to get multiple batches if they all share
        // the most recent timestamp date
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
