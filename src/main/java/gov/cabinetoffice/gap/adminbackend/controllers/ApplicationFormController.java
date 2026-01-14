package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.LambdasHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.*;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.enums.EventType;
import gov.cabinetoffice.gap.adminbackend.exceptions.*;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.security.CheckSchemeOwnership;
import gov.cabinetoffice.gap.adminbackend.services.*;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Tag(name = "Application Forms", description = "API for handling organisations.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/application-forms")
public class ApplicationFormController {

    private final ApplicationFormService applicationFormService;

    private final GrantAdvertService grantAdvertService;

    private final SchemeService schemeService;

    private final EventLogService eventLogService;

    private final UserService userService;

    private final OdtService odtService;

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application form created successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GenericPostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request body",
                    content = @Content(mediaType = "application/json")), })
    @CheckSchemeOwnership
    public ResponseEntity<Void> postApplicationForm(HttpServletRequest request,
            @RequestBody @Valid ApplicationFormPostDTO applicationFormPostDTO) {
        final SchemeDTO scheme = schemeService.getSchemeBySchemeId(applicationFormPostDTO.getGrantSchemeId());
        final GenericPostResponseDTO idResponse = this.applicationFormService
                .saveApplicationForm(applicationFormPostDTO, scheme);

        logApplicationEvent(EventType.APPLICATION_CREATED, request.getRequestedSessionId(),
                idResponse.getId().toString());

        return new ResponseEntity(idResponse, HttpStatus.CREATED);
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
    @CheckSchemeOwnership
    public ResponseEntity<List<ApplicationFormsFoundDTO>> checkApplicationFormsExists(
            @Valid ApplicationFormExistsDTO applicationFormExistsDTO) {
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
    @CheckSchemeOwnership
    public ResponseEntity<Void> getApplicationFormSummary(@PathVariable @NotNull Integer applicationId,
            @RequestParam(defaultValue = "true") Boolean withSections,
            @RequestParam(defaultValue = "true") Boolean withQuestions) {
        try {
            ApplicationFormDTO response = this.applicationFormService.retrieveApplicationFormSummary(applicationId,
                    withSections, withQuestions);
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        catch (ApplicationFormException e) {
            GenericErrorDTO genericErrorDTO = new GenericErrorDTO(e.getMessage());
            return new ResponseEntity(genericErrorDTO, HttpStatus.NOT_FOUND);
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
    @CheckSchemeOwnership
    public ResponseEntity<Void> deleteApplicationForm(@PathVariable @NotNull Integer applicationId) {
        try {
            this.applicationFormService.deleteApplicationForm(applicationId);
            return new ResponseEntity(HttpStatus.OK);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        catch (EntityNotFoundException e) {
            GenericErrorDTO genericErrorDTO = new GenericErrorDTO(e.getMessage());
            return new ResponseEntity(genericErrorDTO, HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/lambda/{grantAdvertId}/application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Application form updated successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request body",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403",
                    description = "Insufficient permissions to update this application form.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Application not found with given id",
                    content = @Content(mediaType = "application/json")), })
    @LambdasHeaderValidator
    public ResponseEntity<Void> removeApplicationAttachedToGrantAdvert(@PathVariable @NotNull final UUID grantAdvertId) {
        try {
            final Integer schemeId = grantAdvertService.getSchemeIdFromAdvert(grantAdvertId);
            final Optional<ApplicationFormEntity> applicationForm = applicationFormService.getOptionalApplicationFromSchemeId(schemeId);
            if (applicationForm.isEmpty()) {
                log.info("No application form attached to grant advert with id: " + grantAdvertId + " was found.");
                return ResponseEntity.noContent().build();
            }

            final ApplicationFormPatchDTO applicationFormPatchDTO = new ApplicationFormPatchDTO();
            applicationFormPatchDTO.setApplicationStatus(ApplicationStatusEnum.REMOVED);
            applicationFormService.patchApplicationForm(applicationForm.get().getGrantApplicationId(), applicationFormPatchDTO, true);

            return ResponseEntity.noContent().build();
        }
        catch (NotFoundException error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch (UnauthorizedException error) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        catch (Exception error) {
            log.error("Error removing application attached to grant advert with id: " + grantAdvertId, error);
            return ResponseEntity.internalServerError().build();
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
    @CheckSchemeOwnership
    public ResponseEntity<GenericErrorDTO> updateApplicationForm(HttpServletRequest request,
            @PathVariable @NotNull Integer applicationId,
            @Valid @RequestBody ApplicationFormPatchDTO applicationFormPatchDTO) {

        try {

            this.applicationFormService.patchApplicationForm(applicationId, applicationFormPatchDTO, false);

            EventType eventType = applicationFormPatchDTO.getApplicationStatus() != null 
                    && applicationFormPatchDTO.getApplicationStatus().equals(ApplicationStatusEnum.PUBLISHED)
                    ? EventType.APPLICATION_PUBLISHED : EventType.APPLICATION_UPDATED;

            logApplicationEvent(eventType, request.getRequestedSessionId(), applicationId.toString());

            return ResponseEntity.noContent().build();
        }
        catch (NotFoundException nfe) {
            GenericErrorDTO genericErrorDTO = new GenericErrorDTO(nfe.getMessage());
            return new ResponseEntity(genericErrorDTO, HttpStatus.NOT_FOUND);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        catch (ApplicationFormException afe) {
            GenericErrorDTO genericErrorDTO = new GenericErrorDTO(afe.getMessage());
            return ResponseEntity.internalServerError().body(genericErrorDTO);
        }

    }

    @GetMapping("/{applicationId}/lastUpdated/email")
    @CheckSchemeOwnership
    public ResponseEntity<EncryptedEmailAddressDTO> getLastUpdatedEmail(@PathVariable final Integer applicationId) {
        final ApplicationFormEntity applicationForm = applicationFormService.getApplicationById(applicationId);

        if (applicationForm.getLastUpdateBy() == null && applicationForm.getLastUpdated() != null) {
            return ResponseEntity.ok(EncryptedEmailAddressDTO.builder().deletedUser(true).build());
        }

        final Optional<GrantAdmin> grantAdmin = userService.getGrantAdminById(Objects
                .requireNonNull(applicationForm.getLastUpdateBy()));
        if (grantAdmin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final String sub = grantAdmin.get().getGapUser().getUserSub();
        final byte[] email = userService.getEmailAddressForSub(sub);
        return ResponseEntity.ok()
                .body(EncryptedEmailAddressDTO.builder().encryptedEmail(email).build());
    }

    @GetMapping("/{applicationId}/status")
    @CheckSchemeOwnership
    public ResponseEntity<String> getApplicationStatus(@PathVariable final Integer applicationId) {
        final ApplicationStatusEnum applicationStatus = applicationFormService.getApplicationStatus(applicationId);
        return ResponseEntity.ok(applicationStatus.toString());
    }

    @GetMapping("/{applicationId}/download-summary")
    @CheckSchemeOwnership
    public ResponseEntity<ByteArrayResource> exportApplication(
            @PathVariable final Integer applicationId) {
        try (OdfTextDocument odt = applicationFormService.getApplicationFormExport(applicationId)) {

            ByteArrayResource odtResource = odtService.odtToResource(odt);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"application.odt\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok().headers(headers).contentLength(odtResource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(odtResource);
        } catch (RuntimeException e) {
            log.error("Could not generate ODT for application " + applicationId + ". Exception: ", e);
            throw new OdtException("Could not generate ODT for this application");
        } catch (Exception e) {
            log.error("Could not convert ODT to resource for application " + applicationId + ". Exception: ", e);
            throw new OdtException("Could not download ODT for this application");
        }
    }


    private void logApplicationEvent(EventType eventType, String sessionId, String applicationId) {

        try {
            AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
            switch (eventType) {
                case APPLICATION_CREATED -> eventLogService.logApplicationCreatedEvent(sessionId, session.getUserSub(),
                        session.getFunderId(), applicationId);

                case APPLICATION_UPDATED -> eventLogService.logApplicationUpdatedEvent(sessionId, session.getUserSub(),
                        session.getFunderId(), applicationId);

                case APPLICATION_PUBLISHED -> eventLogService.logApplicationPublishedEvent(sessionId,
                        session.getUserSub(), session.getFunderId(), applicationId);

                default -> throw new InvalidEventException("Invalid event provided: " + eventType);
            }

        }
        catch (Exception e) {
            // If anything goes wrong logging to event service, log and continue
            log.error("Could not send to event service. Exception: ", e);
        }
    }

}
