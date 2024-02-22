package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.LambdasHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.dtos.OutstandingExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.S3ObjectKeyDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportBatchDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportBatchEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.GrantExportMapper;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/grant-export-batch")
@Tag(name = "Grant Export Batch", description = "API for handling grant submission export batches")
@RequiredArgsConstructor
public class GrantExportBatchController {

    private final GrantExportBatchService grantExportBatchService;
    private final GrantExportMapper grantExportMapper;

    @GetMapping("/{exportBatchId}")
    @Operation(
            summary = "Retrieves information about export batch for a given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned submission data for grant export batch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GrantExportBatchDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Unable to find a batch export for this request",
                    content = @Content(mediaType = "application/json"))})
    public ResponseEntity<GrantExportBatchDTO> getExportBatchInfo(@PathVariable UUID exportBatchId) {
        try {
            final GrantExportBatchEntity grantExportBatchEntity = grantExportBatchService.getGrantExportBatch(exportBatchId);
            return ResponseEntity.ok(grantExportMapper.grantExportBatchEntityToGrantExportBatchDTO(grantExportBatchEntity));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{exportId}/status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updates grant export batch status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OutstandingExportCountDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json"))})
    @LambdasHeaderValidator
    public ResponseEntity updateGrantExportBatchStatus(@PathVariable UUID exportId, @RequestBody GrantExportStatus newStatus) {
        try {
            grantExportBatchService.updateExportBatchStatusById(exportId, newStatus);
            log.info("Updated grant_export_batch table status to {} with exportId: {}", newStatus, exportId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error updating grant_export_batch table status to {} with exportId: {}", newStatus, exportId);
            throw e;
        }
    }

    @PatchMapping("/{exportId}/s3-object-key")
    @Operation(summary = "Add AWS S3 object key to grant export batch for download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully added S3 key to grant export batch location",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400",
                    description = "Required path variables and body not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Something went wrong while updating S3 key",
                    content = @Content(mediaType = "application/json"))})
    @LambdasHeaderValidator
    public ResponseEntity updateGrantExportBatchLocation(@PathVariable UUID exportId, @RequestBody S3ObjectKeyDTO s3ObjectKeyDTO) {
        try {
            grantExportBatchService.addS3ObjectKeyToGrantExportBatch(exportId, s3ObjectKeyDTO.getS3ObjectKey());
            log.info("Updated grant_export_batch table location to {} with exportId: {}", s3ObjectKeyDTO.toString(), exportId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error updating grant_export_batch table location to {} with exportId: {}", s3ObjectKeyDTO.toString(), exportId);
            throw e;
        }

    }
}
