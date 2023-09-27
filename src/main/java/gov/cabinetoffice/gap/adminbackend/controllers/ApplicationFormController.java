package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.*;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.List;

@Tag(name = "Application Forms", description = "API for handling organisations.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/application-forms")
public class ApplicationFormController {

    private final ApplicationFormService applicationFormService;

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application form created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GenericPostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request body",
                    content = @Content(mediaType = "application/json")), })
    public ResponseEntity<GenericPostResponseDTO> postApplicationForm(@RequestBody @Valid ApplicationFormPostDTO applicationFormPostDTO) {
        GenericPostResponseDTO idResponse = this.applicationFormService.saveApplicationForm(applicationFormPostDTO);

        return new ResponseEntity<GenericPostResponseDTO>(idResponse, HttpStatus.CREATED);
    }

    @GetMapping("/find")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application form(s) found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "400", description = "Bad request body",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No Application form found",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<List<ApplicationFormsFoundDTO>> checkApplicationFormsExists(@Valid ApplicationFormExistsDTO applicationFormExistsDTO) {
        List<ApplicationFormsFoundDTO> foundApplicationForms = this.applicationFormService
                .getMatchingApplicationFormsIds(applicationFormExistsDTO);

        if (foundApplicationForms.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        else {
            return ResponseEntity.ok().body(foundApplicationForms);
        }
    }

    @GetMapping("/{applicationId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application summary retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApplicationFormDTO.class))),
            @ApiResponse(responseCode = "403",
                    description = "Insufficient permissions to access this application form.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Application not found with given id",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> getApplicationFormSummary(@PathVariable @NotNull Integer applicationId,
            @RequestParam(defaultValue = "true") Boolean withSections,
            @RequestParam(defaultValue = "true") Boolean withQuestions) {
        try {
            ApplicationFormDTO response = this.applicationFormService.retrieveApplicationFormSummary(applicationId,
                    withSections, withQuestions);
            return new ResponseEntity<ApplicationFormDTO>(response, HttpStatus.OK);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity<ApplicationFormDTO>(HttpStatus.FORBIDDEN);
        }
        catch (ApplicationFormException e) {
            GenericErrorDTO genericErrorDTO = new GenericErrorDTO(e.getMessage());
            return new ResponseEntity<GenericErrorDTO>(genericErrorDTO, HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/{applicationId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application summary deleted successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403",
                    description = "Insufficient permissions to delete this application form.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Application not found with given id",
                    content = @Content(mediaType = "application/json")), })
    public ResponseEntity<?> deleteApplicationForm(@PathVariable @NotNull Integer applicationId) {
        try {
            this.applicationFormService.deleteApplicationForm(applicationId);
            return new ResponseEntity<ApplicationFormDTO>(HttpStatus.OK);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity<GenericErrorDTO>(HttpStatus.FORBIDDEN);
        }
        catch (EntityNotFoundException e) {
            GenericErrorDTO genericErrorDTO = new GenericErrorDTO(e.getMessage());
            return new ResponseEntity<GenericErrorDTO>(genericErrorDTO, HttpStatus.NOT_FOUND);
        }

    }

    @PatchMapping("/{applicationId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Application form updated successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request body",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403",
                    description = "Insufficient permissions to update this application form.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Application not found with given id",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<?> updateApplicationForm(@PathVariable @NotNull Integer applicationId,
            @Valid @RequestBody ApplicationFormPatchDTO applicationFormPatchDTO) {

        try {
            this.applicationFormService.patchApplicationForm(applicationId, applicationFormPatchDTO);
            return ResponseEntity.noContent().build();
        }
        catch (NotFoundException nfe) {
            GenericErrorDTO genericErrorDTO = new GenericErrorDTO(nfe.getMessage());
            return new ResponseEntity<GenericErrorDTO>(genericErrorDTO, HttpStatus.NOT_FOUND);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity<GenericErrorDTO>(HttpStatus.FORBIDDEN);
        }
        catch (ApplicationFormException afe) {
            GenericErrorDTO genericErrorDTO = new GenericErrorDTO(afe.getMessage());
            return ResponseEntity.internalServerError().body(genericErrorDTO);
        }
    }

}
