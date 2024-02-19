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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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

        final GrantExportListDTO completedGrantExports = exportService.getGrantExportsByIdAndStatus(exportId, GrantExportStatus.COMPLETE);

        return ResponseEntity.ok(completedGrantExports);

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
        final Long count = exportService.getFailedExportsCount(exportId);
        return ResponseEntity.ok(new FailedExportCountDTO(count));

    }

}
