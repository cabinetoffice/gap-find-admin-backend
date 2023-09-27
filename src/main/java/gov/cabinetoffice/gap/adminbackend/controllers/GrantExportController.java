package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.OutstandingExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportService;
import gov.cabinetoffice.gap.adminbackend.services.SecretAuthService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/export-batch")
@Tag(name = "Grant Exports", description = "API for handling grant/submissions exports")
@RequiredArgsConstructor
public class GrantExportController {

    private final GrantExportService exportService;

    private final SecretAuthService secretAuthService;

    @GetMapping("/{exportId}/outstandingCount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned outstanding submission exports for batch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OutstandingExportCountDTO.class))),
            @ApiResponse(responseCode = "400", description = "Required path variables not provided in expected format",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> getOutstandingExportsCount(@PathVariable UUID exportId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        secretAuthService.authenticateSecret(authHeader);

        Long count = exportService.getOutstandingExportCount(exportId);

        return ResponseEntity.ok(new OutstandingExportCountDTO(count));

    }

}
