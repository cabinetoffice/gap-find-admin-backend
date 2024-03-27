package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.LambdasHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.dtos.FailedExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.OutstandingExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.S3ObjectKeyDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsListDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.FailedSubmissionExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportBatchService;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportService;
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
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/grant-export")
@Tag(name = "Grant Exports", description = "API for handling grant/submissions exports")
@RequiredArgsConstructor
public class GrantExportController {

    private final GrantExportService exportService;
    private final GrantExportBatchService grantExportBatchService;
    private final SubmissionsService submissionsService;

    @GetMapping("/{exportId}/outstandingCount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned outstanding submission exports for batch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OutstandingExportCountDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")) })
    @LambdasHeaderValidator
    public ResponseEntity<OutstandingExportCountDTO> getOutstandingExportsCount(@PathVariable UUID exportId) {
        log.info("Getting outstanding exports count for exportId: {}", exportId);

        final Long count = exportService.getOutstandingExportCount(exportId);
        log.info("Outstanding exports count for exportId: {} is {}", exportId, count);

        return ResponseEntity.ok(new OutstandingExportCountDTO(count));

    }

    @GetMapping("/{exportId}/completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned list of completed grant exports for batch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GrantExportEntity.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")) })
    @LambdasHeaderValidator
    public ResponseEntity<GrantExportListDTO> getCompletedExportRecordsByExportId(@PathVariable UUID exportId) {
        log.info("Getting completed grant exports with export id {}", exportId);
        try {
            final GrantExportListDTO completedGrantExports = exportService.getGrantExportsByIdAndStatus(exportId,
                    GrantExportStatus.COMPLETE);
            log.info("Successfully got grant exports with completed status with export id {}", exportId);
            return ResponseEntity.ok(completedGrantExports);
        }
        catch (Exception e) {
            log.error("Error retrieving completed grant exports with export id {}", exportId);
            throw e;
        }

    }

    @GetMapping("/{exportId}/failedCount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned failed submission exports count for batch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OutstandingExportCountDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")) })
    @LambdasHeaderValidator
    public ResponseEntity getFailedExportsCount(@PathVariable UUID exportId) {
        log.info("Getting failed exports count for exportId: {}", exportId);

        final Long count = exportService.getExportCountByStatus(exportId, GrantExportStatus.FAILED);
        log.info("Failed exports count for exportId: {} is {}", exportId, count);

        return ResponseEntity.ok(new FailedExportCountDTO(count));
    }

    @GetMapping("/{exportId}/remainingCount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned remaining submission exports for batch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OutstandingExportCountDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")) })
    @LambdasHeaderValidator
    public ResponseEntity<OutstandingExportCountDTO> getRemainingExportsCount(@PathVariable UUID exportId) {
        log.info("Getting remaining exports count for exportId: {}", exportId);
        final Long count = exportService.getRemainingExportsCount(exportId);
        log.info("Remaining exports count for exportId: {} is {}", exportId, count);

        return ResponseEntity.ok(new OutstandingExportCountDTO(count));
    }

    @GetMapping("/{exportId}/details")
    @PageableAsQueryParam
    public ResponseEntity<ExportedSubmissionsListDto> getSubmissions(@PathVariable final UUID exportId,
            @RequestParam(name = "grabOnlyFailed", required = false,
                    defaultValue = "false") final boolean grabOnlyFailed, final Pageable pagination) {
        log.info("Getting submissions for exportId: {}, grabOnlyFailed : {}", exportId, grabOnlyFailed);

        final GrantExportStatus status = grabOnlyFailed ? GrantExportStatus.FAILED : GrantExportStatus.COMPLETE;
        final String superZipLocation = grantExportBatchService.getSuperZipLocation(exportId);
        final ExportedSubmissionsListDto exportedSubmissionsListDto = exportService
            .generateExportedSubmissionsListDto(exportId, status, pagination, superZipLocation);

        return ResponseEntity.ok()
                .header("cache-control", "private, no-cache, max-age=0, must-revalidate")
                .body(exportedSubmissionsListDto);
    }

    @PatchMapping("/{exportId}/batch/status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updates grant export batch status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OutstandingExportCountDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")) })
    @LambdasHeaderValidator
    public ResponseEntity updateGrantExportBatchStatus(@PathVariable UUID exportId, @RequestBody GrantExportStatus newStatus) {
       log.info("Updating grant export {} batch status to {}", exportId, newStatus);
        try {
            grantExportBatchService.updateExportBatchStatusById(exportId, newStatus);
            log.info("Updated grant_export_batch table status to {} with exportId: {}", newStatus, exportId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch (Exception e) {
            log.error("Error updating grant_export_batch table status to {} with exportId: {}", newStatus, exportId);
            throw e;
        }
    }

    @PatchMapping("/{exportId}/batch/s3-object-key")
    @Operation(summary = "Add AWS S3 object key to grant export batch for download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully added S3 key to grant export batch location",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400",
                    description = "Required path variables and body not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Something went wrong while updating S3 key",
                    content = @Content(mediaType = "application/json")) })
    @LambdasHeaderValidator
    public ResponseEntity updateGrantExportBatchLocation(@PathVariable UUID exportId, @RequestBody S3ObjectKeyDTO s3ObjectKeyDTO) {
        log.info("Updating grant export {} batch s3 Object key to {}", exportId, s3ObjectKeyDTO.getS3ObjectKey());

        try {
            grantExportBatchService.addS3ObjectKeyToGrantExportBatch(exportId, s3ObjectKeyDTO.getS3ObjectKey());
            log.info("Updated grant_export_batch table location to {} with exportId: {}", s3ObjectKeyDTO.toString(), exportId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch (Exception e) {
            log.error("Error updating grant_export_batch table location to {} with exportId: {}", s3ObjectKeyDTO.toString(), exportId);
            throw e;
        }

    }

    @GetMapping("/{exportId}/submissions/{submissionId}/details")
    @Operation(
            summary = "Retrieve a failed grant export by ID.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Returned failed grant export.",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Submission.class)))) })
    public ResponseEntity<FailedSubmissionExportDTO> getFailedSubmissionExportById(@PathVariable UUID exportId,
                                                                                   @PathVariable UUID submissionId) {
        try {
            final Submission submission = submissionsService.getSubmissionById(submissionId);
            final GrantExportEntity grantExport = exportService.getGrantExportById(exportId, submissionId);
            final FailedSubmissionExportDTO failedSubmissionExportDTO = FailedSubmissionExportDTO.builder()
                    .submissionId(submissionId)
                    .schemeId(submission.getScheme().getId())
                    .schemeName(submission.getScheme().getName())
                    .legalName(submission.getApplicant().getOrganisationProfile().getLegalName())
                    .applicationName(submission.getApplicationName())
                    .sections(submission.getDefinition().getSections())
                    .attachmentsZipLocation(grantExport.getLocation())
                    .build();
            return ResponseEntity.ok(failedSubmissionExportDTO);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
