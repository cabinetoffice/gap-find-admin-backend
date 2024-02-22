package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.LambdasHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.dtos.FailedExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.OutstandingExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsListDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
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
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<FailedExportCountDTO> getFailedExportsCount(@PathVariable UUID exportId) {
        log.info("Getting failed exports count for exportId: {}", exportId);

        final Long count = exportService.getFailedExportsCount(exportId);
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


    @GetMapping("/{exportId}/submissions")
    @PageableAsQueryParam
    public ResponseEntity<ExportedSubmissionsListDto> getSubmissions(@PathVariable final UUID exportId,
            @RequestParam(name = "grabOnlyFailed", required = false,
                    defaultValue = "false") final boolean grabOnlyFailed, final Pageable pagination) {
        log.info("Getting submissions for exportId: {}, grabOnlyFailed : {}", exportId, grabOnlyFailed);

        final GrantExportStatus status = grabOnlyFailed ? GrantExportStatus.FAILED : GrantExportStatus.COMPLETE;
        final ExportedSubmissionsListDto exportedSubmissionsListDto = exportService
            .generateExportedSubmissionsListDto(exportId, status, pagination);

        return ResponseEntity.ok()
                .header("cache-control", "private, no-cache, max-age=0, must-revalidate")
                .body(exportedSubmissionsListDto);
    }

}
