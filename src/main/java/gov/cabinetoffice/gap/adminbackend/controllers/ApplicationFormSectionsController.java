package gov.cabinetoffice.gap.adminbackend.controllers;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.PostSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormSectionService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Application Forms")
@RequestMapping("/application-forms/{applicationId}/sections")
@RestController
@RequiredArgsConstructor
public class ApplicationFormSectionsController {

    private final ApplicationFormSectionService applicationFormSectionService;

    @GetMapping("/{sectionId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Section successfully retrieved from application form.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApplicationFormSectionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid properties provided to return unique section.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to access this section.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No section found with provided ids.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<ApplicationFormSectionDTO> getApplicationFormSectionById(@PathVariable Integer applicationId,
            @PathVariable String sectionId, @RequestParam(defaultValue = "true") Boolean withQuestions) {

        try {
            ApplicationFormSectionDTO sectionDTO = this.applicationFormSectionService.getSectionById(applicationId,
                    sectionId, withQuestions);
            return ResponseEntity.ok(sectionDTO);
        }
        catch (NotFoundException nfe) {
            return ResponseEntity.notFound().build();
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        catch (ApplicationFormException afe) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Section added successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GenericPostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Valid request body is required to add section",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to create new section.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No application found with given id.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity postNewSection(@PathVariable @NotNull Integer applicationId,
            @RequestBody @Validated PostSectionDTO sectionDTO) {
        try {
            String sectionId = this.applicationFormSectionService.addSectionToApplicationForm(applicationId,
                    sectionDTO);

            return ResponseEntity.ok().body(new GenericPostResponseDTO(sectionId));
        }
        catch (NotFoundException e) {
            return new ResponseEntity(new GenericErrorDTO(e.getMessage()), HttpStatus.NOT_FOUND);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/{sectionId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Section deleted successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Cannot delete mandatory sections",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to delete section.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No application or section found with given id.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity deleteSection(@PathVariable Integer applicationId, @PathVariable String sectionId) {
        try {
            // don't allow admins to delete mandatory sections
            if (Objects.equals(sectionId, "ELIGIBILITY") || Objects.equals(sectionId, "ESSENTIAL")) {
                return new ResponseEntity(new GenericErrorDTO("You cannot delete mandatory sections"),
                        HttpStatus.BAD_REQUEST);
            }

            this.applicationFormSectionService.deleteSectionFromApplication(applicationId, sectionId);

            return ResponseEntity.ok().build();
        }
        catch (NotFoundException e) {
            return new ResponseEntity(new GenericErrorDTO(e.getMessage()), HttpStatus.NOT_FOUND);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    @PatchMapping("/{sectionId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Section status updated successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Cannot update the status of a custom section",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to update section.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No application or section found with given id.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity updateSectionStatus(final @PathVariable Integer applicationId,
            final @PathVariable String sectionId, final @RequestBody SectionStatusEnum newStatus) {
        try {
            // don't allow admins to update the status of custom sections
            if (!Objects.equals(sectionId, "ELIGIBILITY") && !Objects.equals(sectionId, "ESSENTIAL")) {
                return new ResponseEntity(new GenericErrorDTO("You cannot update the status of a custom section"),
                        HttpStatus.BAD_REQUEST);
            }

            this.applicationFormSectionService.updateSectionStatus(applicationId, sectionId, newStatus);

            return ResponseEntity.ok().build();
        }
        catch (NotFoundException e) {
            return new ResponseEntity(new GenericErrorDTO(e.getMessage()), HttpStatus.NOT_FOUND);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

}