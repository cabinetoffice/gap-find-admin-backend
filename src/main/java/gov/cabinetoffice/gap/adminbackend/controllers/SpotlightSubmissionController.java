package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/spotlight-submissions")
@Tag(name = "Spotlight submissions", description = "API for handling spotlight submissions")
@RequiredArgsConstructor
public class SpotlightSubmissionController {

    private final SpotlightSubmissionService spotlightSubmissionService;

    @GetMapping(value = "/count/{schemeId}")
    public ResponseEntity<Long> getSpotlightSubmissionCount(@PathVariable Integer schemeId) {
        final Long count = spotlightSubmissionService.getCountBySchemeIdAndStatus(schemeId,
                SpotlightSubmissionStatus.SENT);
        return ResponseEntity.ok(count);
    }

    @GetMapping(value = "/last-updated/{schemeId}")
    public ResponseEntity<String> getLastUpdatedDate(@PathVariable Integer schemeId) {
        final String date = spotlightSubmissionService.getLastSubmissionDate(schemeId, SpotlightSubmissionStatus.SENT);
        return ResponseEntity.ok(date);
    }

}
