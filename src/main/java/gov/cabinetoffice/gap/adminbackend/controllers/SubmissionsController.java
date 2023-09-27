package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.AddingSignedUrlDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.LambdaSubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionExportsDTO;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.services.SecretAuthService;
import gov.cabinetoffice.gap.adminbackend.services.SubmissionsService;
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

    private final SecretAuthService secretAuthService;

    @GetMapping(value = "/spotlight-export/{applicationId}", produces = EXPORT_CONTENT_TYPE)
    public ResponseEntity<InputStreamResource> exportSpotlightChecks(@PathVariable Integer applicationId) {
        final ByteArrayOutputStream stream = submissionsService.exportSpotlightChecks(applicationId);
        final String exportFileName = submissionsService.generateExportFileName(applicationId);
        final InputStreamResource resource = createTemporaryFile(stream, exportFileName);

        submissionsService.updateSubmissionLastRequiredChecksExport(applicationId);

        final int length = stream.toByteArray().length;
        log.info("Exporting spotlight checks for application ID {}, generated filename {} with length {}",
                applicationId, exportFileName, length);

        // setting HTTP headers to tell caller we are returning a file
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.parse("attachment; filename=" + exportFileName));

        return ResponseEntity.ok().headers(headers).contentLength(length)
                .contentType(MediaType.parseMediaType(EXPORT_CONTENT_TYPE)).body(resource);
    }

    private InputStreamResource createTemporaryFile(ByteArrayOutputStream stream, String filename) {
        try {
            File tempFile = File.createTempFile(filename, ".xlsx");
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                stream.writeTo(out);
            }
            return new InputStreamResource(new ByteArrayInputStream(stream.toByteArray()));
        }
        catch (Exception e) {
            log.error("Error creating temporary for file {} problem reported {}", filename, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/export-all/{applicationId}")
    public ResponseEntity<?> exportAllSubmissions(@PathVariable Integer applicationId) {
        submissionsService.triggerSubmissionsExport(applicationId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{applicationId}")
    public ResponseEntity<?> getExportStatus(@PathVariable Integer applicationId) {
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
    public ResponseEntity<?> getSubmissionInfo(final @PathVariable @NotNull UUID submissionId,
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
    public ResponseEntity<?> getCompletedSubmissionExports(@PathVariable UUID exportBatchId) {

        List<SubmissionExportsDTO> exports = submissionsService.getCompletedSubmissionExportsForBatch(exportBatchId);

        return ResponseEntity.ok(exports);
    }

    @PostMapping("/{submissionId}/export-batch/{batchExportId}/status")
    public ResponseEntity<?> updateExportRecordStatus(@PathVariable String batchExportId,
            @PathVariable String submissionId, @RequestBody GrantExportStatus newStatus,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        secretAuthService.authenticateSecret(authHeader);
        submissionsService.updateExportStatus(submissionId, batchExportId, newStatus);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{submissionId}/export-batch/{batchExportId}/signedUrl")
    @Operation(summary = "Add AWS signed url to batch export for download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully added signed url",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400",
                    description = "Required path variables and body not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Something went wrong while updating signed url",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> updateExportRecordLocation(@PathVariable UUID batchExportId,
            @PathVariable UUID submissionId, @RequestBody AddingSignedUrlDTO signedUrlDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        secretAuthService.authenticateSecret(authHeader);
        submissionsService.addSignedUrlToSubmissionExport(submissionId, batchExportId, signedUrlDTO.getSignedUrl());
        return ResponseEntity.noContent().build();
    }

}
