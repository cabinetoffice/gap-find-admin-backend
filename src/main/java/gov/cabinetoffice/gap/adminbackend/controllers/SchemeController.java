package gov.cabinetoffice.gap.adminbackend.controllers;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormService;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.services.SchemeService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Tag(name = "Schemes", description = "API for handling grant schemes.")
@RequestMapping("/schemes")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SchemeController {

    private final SchemeService schemeService;

    private final GrantAdvertService grantAdvertService;

    private final UserService userService;

    private final ApplicationFormService applicationFormService;

    @GetMapping("/{schemeId}")
    @Operation(summary = "Retrieve grant scheme which matches the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found scheme which matched the given id.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SchemeDTO.class))),
            @ApiResponse(responseCode = "404", description = "No scheme found with matching id.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "You do not have permissions to access this scheme.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<SchemeDTO> getSchemeById(@PathVariable final Integer schemeId) {
        SchemeDTO scheme = null;
        try {
            scheme = this.schemeService.getSchemeBySchemeId(schemeId);
            return ResponseEntity.ok(scheme);
        }
        catch (EntityNotFoundException enfe) {
            return ResponseEntity.notFound().build();
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheme created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Unable to save with the val",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<Integer> createNewGrantScheme(final @Valid @RequestBody @NotNull SchemePostDTO newScheme,
            HttpSession session) {
        try {
            Integer newSchemeId = this.schemeService.postNewScheme(newScheme, session);
            return ResponseEntity.ok(newSchemeId);
        }
        catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{schemeId}")
    @Operation(summary = "Update existing grant scheme which matches the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Grant scheme successfully updated.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Valid request body is required to update scheme.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to update this scheme.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No scheme found with matching id.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<String> updateSchemeData(@PathVariable final Integer schemeId,
            @Valid @RequestBody SchemePatchDTO scheme) {
        try {
            this.schemeService.patchExistingScheme(schemeId, scheme);
            return ResponseEntity.noContent().build();
        }
        catch (EntityNotFoundException nfe) {
            return ResponseEntity.notFound().build();
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{schemeId}")
    @Operation(summary = "Delete an existing scheme which matches the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scheme deleted successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to delete this scheme.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No scheme found with matching id.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<String> deleteAScheme(@PathVariable final Integer schemeId) {
        try {
            this.schemeService.deleteASchemeById(schemeId);
            return ResponseEntity.ok("Scheme deleted successfully");
        }
        catch (EntityNotFoundException nfe) {
            return ResponseEntity.notFound().build();
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Retrieve all grant schemes which belong to the logged in user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found schemes",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SchemeDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid properties provided",
                    content = @Content(mediaType = "application/json")), })
    @Parameter(in = ParameterIn.QUERY, description = "True to paginate results from endpoint", name = "paginate",
            schema = @Schema(type = "boolean"))
    @Parameter(name = "pagination", hidden = true)
    @PageableAsQueryParam
    public ResponseEntity<List<SchemeDTO>> getAllSchemes(final @RequestParam(defaultValue = "false") boolean paginate,
            final Pageable pagination) {
        try {
            List<SchemeDTO> fundingOrgSchemes;

            if (paginate) {
                fundingOrgSchemes = this.schemeService.getPaginatedSchemes(pagination);
            }
            else {
                fundingOrgSchemes = this.schemeService.getSignedInUsersSchemes();
            }

            return ResponseEntity.ok().header("cache-control", "private, no-cache, max-age=0, must-revalidate")
                    .body(fundingOrgSchemes);
        }
        catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{schemeId}/scheme-ownership/")
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity updateGrantOwnership(@PathVariable final Integer schemeId,
            @RequestBody final String newAdminEmail) {
        int grantAdminId = userService.getGrantAdminIdFromUserServiceEmail(newAdminEmail);
        schemeService.patchCreatedBy(grantAdminId, schemeId);
        grantAdvertService.patchCreatedBy(grantAdminId, schemeId);
        applicationFormService.patchCreatedBy(grantAdminId, schemeId);
        return ResponseEntity.ok("Grant ownership updated successfully");
    }

    @GetMapping("/admin/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<SchemeDTO>> getAdminsSchemes(final @PathVariable Integer adminId,
            final HttpServletRequest request) {
        List<SchemeDTO> schemes = this.schemeService.getAdminsSchemes(adminId);
        return ResponseEntity.ok().body(schemes);
    }

}
