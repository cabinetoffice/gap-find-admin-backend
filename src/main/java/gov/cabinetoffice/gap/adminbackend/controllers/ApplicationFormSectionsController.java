package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationSectionOrderPatchDto;
import gov.cabinetoffice.gap.adminbackend.dtos.application.PostSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormSectionService;
import gov.cabinetoffice.gap.adminbackend.services.EventLogService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Slf4j
@Tag(name = "Application Forms")
@RequestMapping("/application-forms/{applicationId}/sections")
@RestController
@RequiredArgsConstructor
public class ApplicationFormSectionsController {

    private final ApplicationFormSectionService applicationFormSectionService;

    private final EventLogService eventLogService;

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
    public ResponseEntity postNewSection(final HttpServletRequest request, @PathVariable @NotNull Integer applicationId,
            @RequestBody @Validated PostSectionDTO sectionDTO) {
        try {
            String sectionId = this.applicationFormSectionService.addSectionToApplicationForm(applicationId,
                    sectionDTO);

            logApplicationUpdatedEvent(request.getRequestedSessionId(), applicationId);

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
    public ResponseEntity deleteSection(final HttpServletRequest request, @PathVariable Integer applicationId,
            @PathVariable String sectionId) {
        try {
            // don't allow admins to delete mandatory sections
            if (Objects.equals(sectionId, "ELIGIBILITY") || Objects.equals(sectionId, "ESSENTIAL")) {
                return new ResponseEntity(new GenericErrorDTO("You cannot delete mandatory sections"),
                        HttpStatus.BAD_REQUEST);
            }

            this.applicationFormSectionService.deleteSectionFromApplication(applicationId, sectionId);

            logApplicationUpdatedEvent(request.getRequestedSessionId(), applicationId);

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
    public ResponseEntity updateSectionStatus(final HttpServletRequest request,
            final @PathVariable Integer applicationId, final @PathVariable String sectionId,
            final @RequestBody SectionStatusEnum newStatus) {
        try {
            // don't allow admins to update the status of custom sections
            if (!Objects.equals(sectionId, "ELIGIBILITY") && !Objects.equals(sectionId, "ESSENTIAL")) {
                return new ResponseEntity(new GenericErrorDTO("You cannot update the status of a custom section"),
                        HttpStatus.BAD_REQUEST);
            }

            this.applicationFormSectionService.updateSectionStatus(applicationId, sectionId, newStatus);

            logApplicationUpdatedEvent(request.getRequestedSessionId(), applicationId);

            return ResponseEntity.ok().build();
        }
        catch (NotFoundException e) {
            return new ResponseEntity(new GenericErrorDTO(e.getMessage()), HttpStatus.NOT_FOUND);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    @PatchMapping("/{sectionId}/title")
    public ResponseEntity<Void> updateSectionTitle(final HttpServletRequest request,
            final @PathVariable Integer applicationId, final @PathVariable String sectionId,
            final @RequestBody @Validated PostSectionDTO sectionDTO) {
        if (Objects.equals(sectionId, "ELIGIBILITY") || Objects.equals(sectionId, "ESSENTIAL")) {
            return new ResponseEntity(new GenericErrorDTO("You cannot update the title of a non-custom section"),
                    HttpStatus.BAD_REQUEST);
        }
        this.applicationFormSectionService.updateSectionTitle(applicationId, sectionId, sectionDTO.getSectionTitle());
        logApplicationUpdatedEvent(request.getSession().getId(), applicationId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Section order updated successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to update section.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No application or section found with given id.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<String> updateSectionOrder(final HttpServletRequest request,
            final @PathVariable Integer applicationId,
            final @RequestBody ApplicationSectionOrderPatchDto sectionOrderPatchDto) {
        try {
            this.applicationFormSectionService.updateSectionOrder(applicationId, sectionOrderPatchDto.getSectionId(),
                    sectionOrderPatchDto.getIncrement());
            logApplicationUpdatedEvent(request.getSession().getId(), applicationId);
            return ResponseEntity.ok().build();
        }
        catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        catch (AccessDeniedException ade) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private void logApplicationUpdatedEvent(String sessionId, Integer applicationId) {
        try {
            AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
            eventLogService.logApplicationUpdatedEvent(sessionId, session.getUserSub(), session.getFunderId(),
                    applicationId.toString());
        }
        catch (Exception e) {
            // If anything goes wrong logging to event service, log and continue
            log.error("Could not send to event service. Exception: ", e);
        }
    }

}