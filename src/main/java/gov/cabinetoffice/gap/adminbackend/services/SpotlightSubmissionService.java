package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpotlightSubmissionService {

    private final SpotlightSubmissionRepository spotlightSubmissionRepository;

    public SpotlightSubmission getSpotlightSubmission(UUID spotlightSubmissionId) {
        return spotlightSubmissionRepository.findById(spotlightSubmissionId).orElseThrow(() -> new NotFoundException(
                "A spotlight submission with id " + spotlightSubmissionId + " could not be found"));
    }

}
