package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormQuestionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.security.CheckSchemeOwnership;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormService;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Slf4j
@Tag(name = "Application Forms")
@RequestMapping("/application-forms/{applicationId}/sections/{sectionId}/questions")
@RestController
@RequiredArgsConstructor
public class ApplicationFormQuestionsController {

    private final ApplicationFormService applicationFormService;

    private final EventLogService eventLogService;

    @PatchMapping("/{questionId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Question updated successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Valid request body is required to update question",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to update this question.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No question found with id.",
                    content = @Content(mediaType = "application/json")) })
    @CheckSchemeOwnership
    public ResponseEntity<Void> patchQuestion(HttpServletRequest request, @PathVariable @NotNull Integer applicationId,
            @PathVariable @NotBlank String sectionId, @PathVariable @NotBlank String questionId,
            @RequestBody @NotNull ApplicationFormQuestionDTO question, HttpSession session) {
        try {
            this.applicationFormService.patchQuestionValues(applicationId, sectionId, questionId, question, session);

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

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question added successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GenericPostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Valid request body is required to add question",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to add question.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No application or section found with given id.",
                    content = @Content(mediaType = "application/json")) })
    @CheckSchemeOwnership
    public ResponseEntity<GenericPostResponseDTO> postNewQuestion(HttpServletRequest request,
            @PathVariable @NotNull Integer applicationId, @PathVariable @NotBlank String sectionId,
            @RequestBody @NotNull ApplicationFormQuestionDTO question, HttpSession session) {
        try {
            String questionId = this.applicationFormService.addQuestionToApplicationForm(applicationId, sectionId,
                    question, session);

            logApplicationUpdatedEvent(request.getRequestedSessionId(), applicationId);

            return ResponseEntity.ok().body(new GenericPostResponseDTO(questionId));
        }
        catch (NotFoundException e) {
            return new ResponseEntity(new GenericErrorDTO(e.getMessage()), HttpStatus.NOT_FOUND);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/{questionId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question deleted successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GenericPostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cannot delete question from mandatory section",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to delete question.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No application or section found with given id.",
                    content = @Content(mediaType = "application/json")) })
    @CheckSchemeOwnership
    public ResponseEntity<Void> deleteQuestion(HttpServletRequest request, @PathVariable @NotNull Integer applicationId,
            @PathVariable @NotBlank String sectionId, @PathVariable @NotBlank String questionId) {
        try {
            // don't allow admins to delete questions from mandatory sections
            if (Objects.equals(sectionId, "ELIGIBILITY") || Objects.equals(sectionId, "ESSENTIAL")) {
                return new ResponseEntity(new GenericErrorDTO("You cannot delete mandatory sections"),
                        HttpStatus.BAD_REQUEST);
            }
            this.applicationFormService.deleteQuestionFromSection(applicationId, sectionId, questionId);

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

    @GetMapping("/{questionId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question retrieved successfully.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GenericPostResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to view question.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404",
                    description = "No application, section, or question found with given id.",
                    content = @Content(mediaType = "application/json")) })
    @CheckSchemeOwnership
    public ResponseEntity<ApplicationFormQuestionDTO> getQuestion(@PathVariable @NotNull Integer applicationId,
            @PathVariable @NotBlank String sectionId, @PathVariable @NotBlank String questionId) {
        try {
            ApplicationFormQuestionDTO question = this.applicationFormService.retrieveQuestion(applicationId, sectionId,
                    questionId);

            return ResponseEntity.ok().body(question);
        }
        catch (NotFoundException e) {
            return new ResponseEntity(new GenericErrorDTO(e.getMessage()), HttpStatus.NOT_FOUND);
        }
        catch (AccessDeniedException ade) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    @PatchMapping("/{questionId}/order/{increment}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question order updated successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to update question.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No question found with given ids.",
                    content = @Content(mediaType = "application/json")) })
    @CheckSchemeOwnership
    public ResponseEntity<String> updateSectionOrder(final HttpServletRequest request,
                                                     final @PathVariable @NotBlank Integer applicationId,
                                                     final @PathVariable @NotBlank String sectionId,
                                                     final @PathVariable @NotBlank String questionId,
                                                     final @PathVariable @NotBlank Integer increment) {
        try {
            this.applicationFormService.updateQuestionOrder(applicationId, sectionId, questionId, increment);
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
            log.error("Could not send application update event to event service. Exception: ", e);
        }
    }

}
