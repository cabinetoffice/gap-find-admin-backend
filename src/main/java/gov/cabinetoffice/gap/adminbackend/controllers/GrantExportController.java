package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.LambdasHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.dtos.FailedExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.OutstandingExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/export-batch")
@Tag(name = "Grant Exports", description = "API for handling grant/submissions exports")
@RequiredArgsConstructor
public class GrantExportController {

    private final GrantExportService exportService;

    @GetMapping("/{exportId}/outstandingCount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned outstanding submission exports for batch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OutstandingExportCountDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")) })
    @LambdasHeaderValidator
    public ResponseEntity getOutstandingExportsCount(@PathVariable UUID exportId) {

        Long count = exportService.getOutstandingExportCount(exportId);

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
        try {
            final GrantExportListDTO completedGrantExports = exportService.getGrantExportsByIdAndStatus(exportId, GrantExportStatus.COMPLETE);
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
        final Long count = exportService.getExportCountByStatus(exportId, GrantExportStatus.FAILED);
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
    public ResponseEntity getRemainingExportsCount(@PathVariable UUID exportId) {
        final Long count = exportService.getRemainingExportsCount(exportId);
        return ResponseEntity.ok(new OutstandingExportCountDTO(count));
    }

    @GetMapping("/{exportId}/status/count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned submission exports count for batch id by status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OutstandingExportCountDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity getExportCountByStatus(@PathVariable() UUID exportId, @RequestParam @NotNull GrantExportStatus status){
        final Long count = exportService.getExportCountByStatus(exportId, status);
        return ResponseEntity.ok(count);
    }

}
