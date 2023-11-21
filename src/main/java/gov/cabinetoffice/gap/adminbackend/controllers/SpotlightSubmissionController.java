package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.SpotlightPublisherHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightSubmissionDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.mappers.SpotlightSubmissionMapper;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/spotlight-submissions")
@Tag(name = "Spotlight Submission", description = "API for handling spotlight submissions")
@RequiredArgsConstructor
public class SpotlightSubmissionController {

    private final SpotlightSubmissionService spotlightSubmissionService;

    private final SpotlightSubmissionMapper spotlightSubmissionMapper;

    @GetMapping("/{spotlightSubmissionId}")
    @Operation(summary = "Get spotlight submission by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved spotlight submission by id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404",
                    description = "Spotlight spotlight submission for the given id does not exist",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to get spotlight submission",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json")) })
    @SpotlightPublisherHeaderValidator
    public ResponseEntity<SpotlightSubmissionDto> getSpotlightSubmissionById(
            @PathVariable final UUID spotlightSubmissionId) {

        log.info("Getting spotlight submission with id {}", spotlightSubmissionId);
        final SpotlightSubmission spotlightSubmission = spotlightSubmissionService
                .getSpotlightSubmission(spotlightSubmissionId);
        return ResponseEntity.ok()
                .body(spotlightSubmissionMapper.spotlightSubmissionToSpotlightSubmissionDto(spotlightSubmission));
    }

}