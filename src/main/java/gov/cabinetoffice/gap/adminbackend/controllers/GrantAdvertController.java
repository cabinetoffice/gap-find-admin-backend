package gov.cabinetoffice.gap.adminbackend.controllers;

import com.contentful.java.cma.model.CMAHttpException;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.FieldErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.*;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.mappers.GrantAdvertMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.GrantAdvertPageResponse;
import gov.cabinetoffice.gap.adminbackend.models.ValidationError;
import gov.cabinetoffice.gap.adminbackend.services.EventLogService;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.services.SecretAuthService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("rawtypes")
@Log4j2
@RestController
@RequestMapping("/grant-advert")
@Tag(name = "Grant Advert", description = "API for handling Grant Adverts")
@RequiredArgsConstructor
public class GrantAdvertController {

    private final GrantAdvertService grantAdvertService;

    private final GrantAdvertMapper grantAdvertMapper;

    private final EventLogService eventLogService;

    private final Validator validator;

    private final SecretAuthService secretAuthService;

    @PostMapping("/create")
    public ResponseEntity<CreateGrantAdvertResponseDto> create(HttpServletRequest request,
                                                               @Valid @RequestBody CreateGrantAdvertDto createGrantAdvertDto) {
        log.info("Creating Grant Advert '{}' for Grant Scheme ID '{}'", createGrantAdvertDto.getName(),
                createGrantAdvertDto.getGrantSchemeId());
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        GrantAdvert grantAdvert = grantAdvertService.create(createGrantAdvertDto.getGrantSchemeId(),
                session.getGrantAdminId(), createGrantAdvertDto.getName());
        log.info("Successfully created Grant Advert named '{}' with id '{}'", grantAdvert.getGrantAdvertName(),
                grantAdvert.getId());

        try {
            eventLogService.logAdvertCreatedEvent(request.getRequestedSessionId(), session.getUserSub(),
                    session.getFunderId(), grantAdvert.getId().toString());
        } catch (Exception e) {
            // If anything goes wrong logging to event service, log and continue
            log.error("Could not send to event service. Exception: ", e);
        }

        CreateGrantAdvertResponseDto responseDto = this.grantAdvertMapper
                .grantAdvertToCreateGrantAdvertResponseDto(grantAdvert);

        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{grantAdvertId}/sections/{sectionId}/pages/{pageId}")
    public ResponseEntity updatePage(HttpServletRequest request, @PathVariable UUID grantAdvertId,
                                     @PathVariable String sectionId, @PathVariable String pageId,
                                     @RequestBody @NotNull GrantAdvertPagePatchResponseDto patchAdvertPageResponse) {
        GrantAdvertPageResponse responseWithId = GrantAdvertPageResponse.builder().id(pageId)
                .status(patchAdvertPageResponse.getStatus()).questions(patchAdvertPageResponse.getQuestions()).build();
        GrantAdvertPageResponseValidationDto patchPageDto = GrantAdvertPageResponseValidationDto.builder()
                .grantAdvertId(grantAdvertId).sectionId(sectionId).page(responseWithId).build();
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        // given we need sectionId to validate the Dto, we can't validate in the
        // controller method
        Set<ConstraintViolation<GrantAdvertPageResponseValidationDto>> validationErrorsSet = validator
                .validate(patchPageDto);

        if (!validationErrorsSet.isEmpty()) {
            List<ValidationError> validationErrorsList = reorderValidationErrors(patchAdvertPageResponse,
                    validationErrorsSet);
            return ResponseEntity.badRequest().body(new FieldErrorsDTO(validationErrorsList));
        }

        grantAdvertService.updatePageResponse(patchPageDto);

        try {
            eventLogService.logAdvertUpdatedEvent(request.getRequestedSessionId(), session.getUserSub(),
                    session.getFunderId(), grantAdvertId.toString());
        } catch (Exception e) {
            // If anything goes wrong logging to event service, log and continue
            log.error("Could not send to event service. Exception: ", e);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @Operation(summary = "Retrieves the status of a grant advert for query params provided")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully fetched the status of grant advert for query params provided",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetGrantAdvertStatusResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to fetch this grant advert",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No grant advert found for scheme id provided",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request body",
                    content = @Content(mediaType = "application/json")) })

    public ResponseEntity getAdvertStatus(@RequestParam @NotNull final Integer grantSchemeId) {

        GetGrantAdvertStatusResponseDTO grantAdvertResponse = this.grantAdvertService
                .getGrantAdvertStatusBySchemeId(grantSchemeId);

        return ResponseEntity.ok(grantAdvertResponse);
    }

    @GetMapping("/publish-information")
    @Operation(summary = "Retrieves the information around publishing a grant advert for query params provided")
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
            description = "Successfully fetched the publishing information of grant advert for query params provided",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetGrantAdvertPublishingInformationResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to fetch this grant advert",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No grant advert found for scheme id provided",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request body",
                    content = @Content(mediaType = "application/json")) })

    public ResponseEntity getPublishInformation(@RequestParam @NotNull final Integer grantSchemeId) {

        GetGrantAdvertPublishingInformationResponseDTO grantAdvertPublishingInformationResponse = this.grantAdvertService
                .getGrantAdvertPublishingInformationBySchemeId(grantSchemeId);

        return ResponseEntity.ok(grantAdvertPublishingInformationResponse);
    }

    @DeleteMapping("/{grantAdvertId}")
    @Operation(summary = "Delete the grant advert with the given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted grant advert with id provided"),
            @ApiResponse(responseCode = "400", description = "Required path variable not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Unable to find grant advert with id provided",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity deleteGrantAdvert(@PathVariable UUID grantAdvertId) {

        grantAdvertService.deleteGrantAdvert(grantAdvertId);

        return ResponseEntity.noContent().build();
    }
    /*  */

    /**
     * This addresses an issue where validation errors can be sent out of order, since
     * validator impl adds them to a HashSet which does not retain their order. This puts
     * them into the same order as the initial DTO we receive
     */
    private List<ValidationError> reorderValidationErrors(GrantAdvertPagePatchResponseDto patchAdvertPageResponse,
                                                          Set<ConstraintViolation<GrantAdvertPageResponseValidationDto>> validationErrorsSet) {
        List<ValidationError> validationErrorsList = new ArrayList<>();

        // map questions, if question errors exist
        patchAdvertPageResponse.getQuestions().forEach(questionResponse -> {
            validationErrorsSet.stream()
                    .filter(error -> error.getPropertyPath().toString().startsWith(questionResponse.getId()))
                    .findFirst().ifPresent(violation -> validationErrorsList
                            .add(new ValidationError(violation.getPropertyPath().toString(), violation.getMessage())));
        });

        // map page status, if it exists
        validationErrorsSet.stream().filter(error -> error.getPropertyPath().toString().equals("completed")).findFirst()
                .ifPresent(violation -> validationErrorsList
                        .add(new ValidationError(violation.getPropertyPath().toString(), violation.getMessage())));

        return validationErrorsList;
    }

    @PostMapping("/{grantAdvertId}/publish")
    public ResponseEntity<GrantAdvert> publishGrantAdvert(HttpServletRequest request,
                                                          final @PathVariable UUID grantAdvertId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        final GrantAdvert publishedAdvert = grantAdvertService.publishAdvert(grantAdvertId, false);

        try {
            eventLogService.logAdvertPublishedEvent(request.getRequestedSessionId(), session.getUserSub(),
                    session.getFunderId(), grantAdvertId.toString());
        } catch (Exception e) {
            // If anything goes wrong logging to event service, log and continue
            log.error("Could not send to event service. Exception: ", e);
        }

        return ResponseEntity.ok(publishedAdvert);
    }

    @PostMapping("/lambda/{grantAdvertId}/publish")
    public ResponseEntity publishGrantAdvertLambda(final @PathVariable UUID grantAdvertId,
                                                   final @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        secretAuthService.authenticateSecret(authHeader);
        try {
            grantAdvertService.publishAdvert(grantAdvertId, true);
        } catch (CMAHttpException cmae) {
            log.error("Contentful Error Body - " + cmae.getErrorBody().toString());

            return ResponseEntity.badRequest().body(cmae.getErrorBody());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{grantAdvertId}/schedule")
    public ResponseEntity scheduleGrantAdvert(HttpServletRequest request, final @PathVariable UUID grantAdvertId) {
        grantAdvertService.scheduleGrantAdvert(grantAdvertId);
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        try {
            eventLogService.logAdvertPublishedEvent(request.getRequestedSessionId(), session.getUserSub(),
                    session.getFunderId(), grantAdvertId.toString());
        } catch (Exception e) {
            // If anything goes wrong logging to event service, log and continue
            log.error("Could not send to event service. Exception: ", e);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{grantAdvertId}/unpublish")
    @Operation(summary = "Unpublishes the advert with id provided")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully unpublished the advert",
            content = @Content(mediaType = "application/json")) })
    public ResponseEntity unpublishGrantAdvert(final @PathVariable UUID grantAdvertId) {
        grantAdvertService.unpublishAdvert(grantAdvertId, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/lambda/{grantAdvertId}/unpublish")
    @Operation(summary = "Unpublishes the advert with id provided, callable from the lambda that's authed via a secret")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unpublished the advert",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No credentials or invalid credentials provided",
                    content = @Content(mediaType = "application/json")), })
    public ResponseEntity unpublishGrantAdvert(final @PathVariable UUID grantAdvertId,
                                               final @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        secretAuthService.authenticateSecret(authHeader);
        grantAdvertService.unpublishAdvert(grantAdvertId, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{grantAdvertId}/unschedule")
    public ResponseEntity unscheduleGrantAdvert(final @PathVariable UUID grantAdvertId) {
        grantAdvertService.unscheduleGrantAdvert(grantAdvertId);
        return ResponseEntity.ok().build();
    }

}
