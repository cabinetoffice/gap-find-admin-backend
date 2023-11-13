package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotlightSubmissionService {

    private final SpotlightSubmissionRepository spotlightSubmissionRepository;

    public List<SpotlightSubmission> getSubmissionsByBySchemeIdAndStatus(Integer schemeId,
            SpotlightSubmissionStatus status) {
        return spotlightSubmissionRepository.findBySchemeIdAndStatus(schemeId, status);
    }

    public long getCountBySchemeIdAndStatus(Integer schemeId, SpotlightSubmissionStatus status) {
        return spotlightSubmissionRepository.countBySchemeIdAndStatus(schemeId, status);
    }

    public String getLastSubmissionDate(Integer schemeId, SpotlightSubmissionStatus status) {
        final List<SpotlightSubmission> spotlightSubmissions = getSubmissionsByBySchemeIdAndStatus(schemeId, status);
        return spotlightSubmissions.stream().map(SpotlightSubmission::getLastSendAttempt).max(LocalDateTime::compareTo)
                .map(date -> date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))).orElse(null);
    }

}
