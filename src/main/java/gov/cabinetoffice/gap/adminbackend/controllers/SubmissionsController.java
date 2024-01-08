package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.constants.SpotlightExports;
import gov.cabinetoffice.gap.adminbackend.dtos.S3ObjectKeyDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.UrlDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.LambdaSubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionExportsDTO;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.services.FileService;
import gov.cabinetoffice.gap.adminbackend.services.SecretAuthService;
import gov.cabinetoffice.gap.adminbackend.services.SubmissionsService;
import gov.cabinetoffice.gap.adminbackend.services.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/submissions")
@Tag(name = "Submissions", description = "API for handling applicants submissions")
@RequiredArgsConstructor
public class SubmissionsController {

    static final String EXPORT_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final SubmissionsService submissionsService;

    private final S3Service s3Service;

    private final SecretAuthService secretAuthService;

    private final FileService fileService;

    @GetMapping(value = "/spotlight-export/{applicationId}", produces = EXPORT_CONTENT_TYPE)
    public ResponseEntity<InputStreamResource> exportSpotlightChecks(@PathVariable Integer applicationId) {
        log.info("Started submissions export for application " + applicationId);
        long start = System.currentTimeMillis();

        final ByteArrayOutputStream stream = submissionsService.exportSpotlightChecks(applicationId);
        final InputStreamResource resource = fileService.createTemporaryFile(stream,
                SpotlightExports.REQUIRED_CHECKS_FILENAME);

        submissionsService.updateSubmissionLastRequiredChecksExport(applicationId);

        final int length = stream.toByteArray().length;
        log.info("Exporting spotlight checks for application ID {}, generated filename {} with length {}",
                applicationId, SpotlightExports.REQUIRED_CHECKS_FILENAME, length);

        // setting HTTP headers to tell caller we are returning a file
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.parse("attachment; filename=" + SpotlightExports.REQUIRED_CHECKS_FILENAME));

        long end = System.currentTimeMillis();
        log.info("Finished submissions export for application " + applicationId + ". Export time in millis: "
                + (end - start));

        return ResponseEntity.ok().headers(headers).contentLength(length)
                .contentType(MediaType.parseMediaType(EXPORT_CONTENT_TYPE)).body(resource);
    }

    @PostMapping("/export-all/{applicationId}")
    public ResponseEntity exportAllSubmissions(@PathVariable Integer applicationId) {
        submissionsService.triggerSubmissionsExport(applicationId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{applicationId}")
    public ResponseEntity getExportStatus(@PathVariable Integer applicationId) {
        final GrantExportStatus status = submissionsService.getExportStatus(applicationId);
        return new ResponseEntity<>(status.toString(), HttpStatus.OK);
    }

    @GetMapping("/{submissionId}/export-batch/{batchExportId}/submission")
    @Operation(summary = "Retrieve submission data for lambda submissions export")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned submission data for lambda",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LambdaSubmissionDefinition.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Unable to find batch or submission for this request",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity getSubmissionInfo(final @PathVariable @NotNull UUID submissionId,
            final @PathVariable @NotNull UUID batchExportId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        secretAuthService.authenticateSecret(authHeader);

        try {
            final LambdaSubmissionDefinition submission = submissionsService.getSubmissionInfo(submissionId,
                    batchExportId, authHeader);
            return ResponseEntity.ok(submission);
        }
        catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exports/{exportBatchId}")
    @Operation(
            summary = "Retrieve a list of completed submission exports with the specified batch that are created by the logged in user.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Returned completed submission exports.",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = SubmissionExportsDTO.class)))) })
    public ResponseEntity getCompletedSubmissionExports(@PathVariable UUID exportBatchId) {

        List<SubmissionExportsDTO> exports = submissionsService.getCompletedSubmissionExportsForBatch(exportBatchId);

        return ResponseEntity.ok(exports);
    }

    @PostMapping("/{submissionId}/export-batch/{batchExportId}/status")
    public ResponseEntity updateExportRecordStatus(@PathVariable String batchExportId,
            @PathVariable String submissionId, @RequestBody GrantExportStatus newStatus,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        secretAuthService.authenticateSecret(authHeader);
        submissionsService.updateExportStatus(submissionId, batchExportId, newStatus);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/signed-url")
    @Operation(summary = "Get presigned link for S3 object key")
    public ResponseEntity<UrlDTO> getPresignedUrl(@RequestBody S3ObjectKeyDTO s3ObjectKeyDTO) {
        final String objectKey = s3ObjectKeyDTO.getS3ObjectKey();
        final String presignedUrl = s3Service.generateExportDocSignedUrl(objectKey);
        return ResponseEntity.ok(new UrlDTO(presignedUrl));
    }

    @PatchMapping("/{submissionId}/export-batch/{batchExportId}/s3-object-key")
    @Operation(summary = "Add AWS S3 object key to batch export for download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully added S3 key",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400",
                    description = "Required path variables and body not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Something went wrong while updating S3 key",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity updateExportRecordLocation(@PathVariable UUID batchExportId, @PathVariable UUID submissionId,
            @RequestBody S3ObjectKeyDTO s3ObjectKeyDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        secretAuthService.authenticateSecret(authHeader);
        submissionsService.addS3ObjectKeyToSubmissionExport(submissionId, batchExportId,
                s3ObjectKeyDTO.getS3ObjectKey());
        return ResponseEntity.noContent().build();
    }

}
