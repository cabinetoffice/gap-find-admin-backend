package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotlightSubmissionService {

    private final SpotlightSubmissionRepository spotlightSubmissionRepository;

    public SpotlightSubmission getSpotlightSubmission(UUID spotlightSubmissionId) {
        return spotlightSubmissionRepository.findById(spotlightSubmissionId).orElseThrow(() -> new NotFoundException(
                "A spotlight submission with id " + spotlightSubmissionId + " could not be found"));
    }

    public void addSpotlightBatchToSpotlightSubmission(UUID spotlightSubmissionId, SpotlightBatch spotlightBatch) {
        final SpotlightSubmission spotlightSubmission = getSpotlightSubmission(spotlightSubmissionId);

        final List<SpotlightBatch> existingBatch = spotlightSubmission.getBatches();
        existingBatch.add(spotlightBatch);
        spotlightSubmission.setBatches(existingBatch);

        spotlightSubmissionRepository.save(spotlightSubmission);
    }

    public List<SpotlightSubmission> getSubmissionsByBySchemeIdAndStatus(Integer schemeId,
            SpotlightSubmissionStatus status) {
        return spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(schemeId, status.toString());
    }

    public long getCountBySchemeIdAndStatus(Integer schemeId, SpotlightSubmissionStatus status) {
        return spotlightSubmissionRepository.countByGrantSchemeIdAndStatus(schemeId, status.toString());
    }

    public String getLastSubmissionDate(Integer schemeId, SpotlightSubmissionStatus status) {
        final List<SpotlightSubmission> spotlightSubmissions = getSubmissionsByBySchemeIdAndStatus(schemeId, status);
        return spotlightSubmissions.stream().map(SpotlightSubmission::getLastSendAttempt).max(Instant::compareTo)
                .map(date -> date.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .orElse(null);
    }

}
