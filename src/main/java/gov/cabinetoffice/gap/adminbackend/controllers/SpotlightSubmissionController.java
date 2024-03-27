package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.LambdasHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.constants.SpotlightExports;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.GetSpotlightSubmissionDataBySchemeIdDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightSubmissionDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.SpotlightSubmissionMapper;
import gov.cabinetoffice.gap.adminbackend.security.CheckSchemeOwnership;
import gov.cabinetoffice.gap.adminbackend.services.FileService;
import gov.cabinetoffice.gap.adminbackend.services.SchemeService;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import gov.cabinetoffice.gap.adminbackend.services.SubmissionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;

@Log4j2
@RestController
@RequestMapping("/spotlight-submissions")
@Tag(name = "Spotlight Submission", description = "API for handling spotlight submissions")
@RequiredArgsConstructor
public class SpotlightSubmissionController {

    private final SpotlightSubmissionService spotlightSubmissionService;

    private final SpotlightSubmissionMapper spotlightSubmissionMapper;

    private final SchemeService schemeService;

    private final FileService fileService;

    private final SubmissionsService submissionsService;

    // check spring security whitelist before adding endpoints

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
    @LambdasHeaderValidator
    public ResponseEntity<SpotlightSubmissionDto> getSpotlightSubmissionById(
            @PathVariable final UUID spotlightSubmissionId) {

        log.info("Getting spotlight submission with id {}", spotlightSubmissionId);
        final SpotlightSubmission spotlightSubmission = spotlightSubmissionService
                .getSpotlightSubmission(spotlightSubmissionId);
        return ResponseEntity.ok()
                .body(spotlightSubmissionMapper.spotlightSubmissionToSpotlightSubmissionDto(spotlightSubmission));
    }

    @GetMapping(value = "/scheme/{schemeId}/due-diligence-data")
    @CheckSchemeOwnership
    public ResponseEntity<GetSpotlightSubmissionDataBySchemeIdDto> getSpotlightSubmissionDataBySchemeId(
            @PathVariable Integer schemeId) {
        log.info("Getting spotlight submission data for scheme {}", schemeId);

        final boolean hasSpotlightSubmissions = spotlightSubmissionService.doesSchemeHaveSpotlightSubmission(schemeId);

        final GetSpotlightSubmissionDataBySchemeIdDto data = GetSpotlightSubmissionDataBySchemeIdDto.builder()
                .hasSpotlightSubmissions(hasSpotlightSubmissions).build();

        if (!hasSpotlightSubmissions) {
            log.info("No spotlight submissions found for scheme {}", schemeId);

            return ResponseEntity.ok(data);
        }

        final Long count = spotlightSubmissionService.getCountBySchemeIdAndStatus(schemeId,
                SpotlightSubmissionStatus.SENT);
        final String date = spotlightSubmissionService.getLastSubmissionDate(schemeId, SpotlightSubmissionStatus.SENT);

        data.setSentCount(count);
        data.setSentLastUpdatedDate(date);

        log.info("Spotlight submission for scheme {} are {} last updated at {}", schemeId, data.getSentCount(),
                data.getSentLastUpdatedDate());

        return ResponseEntity.ok(data);
    }

    @GetMapping(value = "/scheme/{schemeId}/download", produces = EXPORT_CONTENT_TYPE)
    @CheckSchemeOwnership
    public ResponseEntity<InputStreamResource> downloadDueDiligenceChecks(@PathVariable Integer schemeId,
            @RequestParam boolean onlyValidationErrors) {
        final String logMessage = onlyValidationErrors ? "validation errors" : "all";
        log.info("Downloading {} due diligence data for scheme with id {}", logMessage, schemeId);

        final SchemeDTO scheme = schemeService.getSchemeBySchemeId(schemeId);
        final ByteArrayOutputStream stream = spotlightSubmissionService.generateDownloadFile(scheme,
                onlyValidationErrors);

        submissionsService.updateLastRequiredChecksExportBySchemeId(schemeId);

        return getInputStreamResourceResponseEntity(schemeId, stream, SpotlightExports.SPOTLIGHT_CHECKS_FILENAME);
    }

    @NotNull
    private ResponseEntity<InputStreamResource> getInputStreamResourceResponseEntity(@PathVariable Integer schemeId,
            ByteArrayOutputStream stream, String exportFileName) {
        log.info("Started due diligence data export for scheme " + schemeId);
        long start = System.currentTimeMillis();

        final InputStreamResource resource = fileService.createTemporaryFile(stream, exportFileName);
        final int length = stream.toByteArray().length;

        // setting HTTP headers to tell caller we are returning a file
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.parse("attachment; filename=" + exportFileName));

        long end = System.currentTimeMillis();
        log.info("Finished due diligence data export for scheme " + schemeId + ". Export time in millis: "
                + (end - start));

        return ResponseEntity.ok().headers(headers).contentLength(length)
                .contentType(MediaType.parseMediaType(EXPORT_CONTENT_TYPE)).body(resource);
    }

}
