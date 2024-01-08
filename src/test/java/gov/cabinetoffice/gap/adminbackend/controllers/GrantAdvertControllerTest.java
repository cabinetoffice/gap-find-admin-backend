package gov.cabinetoffice.gap.adminbackend.controllers;

import com.contentful.java.cma.model.CMAHttpException;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.LambdasInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.CreateGrantAdvertDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPublishingInformationResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertStatusResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GrantAdvertPageResponseValidationDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.GrantAdvertMapperImpl;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.GrantAdvertPageResponse;
import gov.cabinetoffice.gap.adminbackend.models.GrantAdvertQuestionResponse;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.EventLogService;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.Validator;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomGrantAdvertGenerators.randomGrantAdvertEntity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GrantAdvertController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(
        classes = { GrantAdvertController.class, ControllerExceptionHandler.class, LambdasInterceptor.class })
class GrantAdvertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrantAdvertService grantAdvertService;

    @SpyBean
    private GrantAdvertMapperImpl grantAdvertMapper;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @MockBean
    private EventLogService eventLogService;

    @MockBean
    private Validator validator;

    @MockBean
    @Qualifier("submissionExportAndScheduledPublishingLambdasInterceptor")
    private AuthorizationHeaderInterceptor mockAuthorizationHeaderInterceptor;

    @MockBean
    private LambdasInterceptor mockLambdasInterceptor;

    @Nested
    class createAdvert {

        Integer grantSchemeId = 1;

        String advertName = "AdvertName";

        CreateGrantAdvertDto dto = CreateGrantAdvertDto.builder().grantSchemeId(grantSchemeId).name(advertName).build();

        UUID advertId = UUID.randomUUID();

        GrantAdvert expectedAdvert = GrantAdvert.builder().id(advertId).grantAdvertName(advertName)

                .build();

        @Test
        @WithAdminSession
        void createAdvert_HappyPath() throws Exception {

            when(grantAdvertService.create(eq(grantSchemeId), any(), eq(advertName))).thenReturn(expectedAdvert);

            mockMvc.perform(post("/grant-advert/create").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonString(dto))).andExpect(status().isOk());

            verify(grantAdvertService).create(eq(grantSchemeId), anyInt(), eq(advertName));
            verify(eventLogService).logAdvertCreatedEvent(any(), anyString(), anyLong(), eq(advertId.toString()));

            // Could probably do with verifying the response entity here too if someone
            // has some time.
            // I'm just writing this to test the event log service stuff

        }

    }

    @Nested
    class updatePageResponse {

        UUID grantAdvertId = UUID.fromString("33bbb645-271f-4a2f-b272-8153e68a8bd7");

        String sectionId = "123";

        String pageId = "987";

        String questionId = "grantShortDescription";

        String expectedResponse = "This is a description";

        GrantAdvertPageResponse samplePage = GrantAdvertPageResponse.builder()
                .status(GrantAdvertPageResponseStatus.IN_PROGRESS)
                .questions(Collections.singletonList(GrantAdvertQuestionResponse.builder().id(questionId).seen(true)
                        .response(expectedResponse).build()))
                .build();

        GrantAdvertPageResponseValidationDto pagePatchDto = GrantAdvertPageResponseValidationDto.builder()
                .grantAdvertId(grantAdvertId).sectionId(sectionId).page(samplePage).build();

        @Test
        @WithAdminSession
        void updatePageResponse_HappyPath() throws Exception {
            when(validator.validate(pagePatchDto)).thenReturn(Set.of());
            doNothing().when(grantAdvertService).updatePageResponse(pagePatchDto);

            mockMvc.perform(
                    patch(String.format("/grant-advert/%s/sections/%s/pages/%s", grantAdvertId, sectionId, pageId))
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(samplePage)))
                    .andExpect(status().isOk());

            verify(eventLogService).logAdvertUpdatedEvent(any(), anyString(), anyLong(), eq(grantAdvertId.toString()));
        }

        // missing test around failed validation - I can't find a way to handcraft a
        // ConstraintViolationImpl obj
        // if time permits, can create our own impl, but very time consuming for just this
        // test scenario
        // verified manually, failed validation produces expected fieldErrors and a 400

        @Test
        @WithAdminSession
        void updatePageResponse_NoGrantAdvert() throws Exception {
            when(validator.validate(pagePatchDto)).thenReturn(Set.of());
            doThrow(new NotFoundException()).when(grantAdvertService).updatePageResponse(any());

            mockMvc.perform(
                    patch(String.format("/grant-advert/%s/sections/%s/pages/%s", grantAdvertId, sectionId, pageId))
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(samplePage)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void updatePageResponse_NullBody() throws Exception {
            mockMvc.perform(
                    patch(String.format("/grant-advert/%s/sections/%s/pages/%s", grantAdvertId, sectionId, pageId)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        void updatePageResponse_GenericException() throws Exception {
            when(validator.validate(pagePatchDto)).thenReturn(Set.of());
            doThrow(new RuntimeException()).when(grantAdvertService).updatePageResponse(any());

            mockMvc.perform(
                    patch(String.format("/grant-advert/%s/sections/%s/pages/%s", grantAdvertId, sectionId, pageId))
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(samplePage)))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class deleteGrantAdvert {

        @Test
        void successfulRequest_ReturningAllViewDataForAdvertBuilderPages() throws Exception {

            UUID grantAdvertId = UUID.randomUUID();

            mockMvc.perform(delete("/grant-advert/" + grantAdvertId)).andExpect(status().isNoContent());
        }

        @Test
        void badRequest_IncorrectRequestParams() throws Exception {
            mockMvc.perform(delete("/grant-advert/" + "non-uuid-string")).andExpect(status().isBadRequest());
        }

        @Test
        void notFound_AttemptingToAccessAdvertWhichDoesntExist() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();
            doThrow(NotFoundException.class).when(grantAdvertService).deleteGrantAdvert(grantAdvertId);

            mockMvc.perform(delete("/grant-advert/" + grantAdvertId)).andExpect(status().isNotFound());
        }

    }

    @Nested
    class publishGrantAdvert {

        final UUID grantAdvertId = UUID.randomUUID();

        final GrantAdvert grantAdvert = GrantAdvert.builder().id(grantAdvertId).grantAdvertName("grant-advert-name")
                .contentfulSlug("slug").build();

        @Test
        @WithAdminSession
        void publishesAndReturnsExpectedResponse() throws Exception {
            when(grantAdvertService.publishAdvert(grantAdvertId, false)).thenReturn(grantAdvert);
            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/publish").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonStringWithNulls(grantAdvert))).andExpect(status().isOk());

            verify(eventLogService).logAdvertPublishedEvent(any(), anyString(), anyLong(),
                    eq(grantAdvertId.toString()));

        }

        @Test
        @WithAdminSession
        void publishAndReturnsNotFound() throws Exception {
            when(grantAdvertService.publishAdvert(grantAdvertId, false)).thenThrow(NotFoundException.class);
            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/publish")).andExpect(status().isNotFound());

        }

        @Test
        @WithAdminSession
        void publishAndReturnsNotEnoughPermissions() throws Exception {
            when(grantAdvertService.publishAdvert(grantAdvertId, false)).thenThrow(AccessDeniedException.class);
            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/publish")).andExpect(status().isForbidden());

        }

        @Test
        void publishAndReturnsUnexpectedServiceError() throws Exception {
            when(grantAdvertService.publishAdvert(grantAdvertId, false)).thenThrow(RuntimeException.class);
            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/publish"))
                    .andExpect(status().isInternalServerError());

        }

    }

    @Nested
    class unpublishGrantAdvert {

        final UUID grantAdvertId = UUID.randomUUID();

        @Test
        void successfulRequest_ReturnsIsOk() throws Exception {
            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/unpublish")).andExpect(status().isOk());
        }

        @Test
        void internalServerError_ReturnsContentfulException() throws Exception {
            doThrow(CMAHttpException.class).when(grantAdvertService).unpublishAdvert(grantAdvertId, false);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/unpublish"))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class unpublishGrantAdvertLambda {

        final String LAMBDA_AUTH_HEADER = "topSecretKey";

        @Test
        void unpublishGrantAdvertLambda_success() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();
            final GrantAdvert grantAdvert = randomGrantAdvertEntity().id(grantAdvertId).build();

            when(grantAdvertService.publishAdvert(grantAdvertId, true)).thenReturn(grantAdvert);

            mockMvc.perform(post("/grant-advert/lambda/" + grantAdvertId + "/unpublish")
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isOk());
        }

        @Test
        void unpublishGrantAdvertLambda_badPathVariable() throws Exception {
            mockMvc.perform(get("/grant-advert/lambda/this_isnt_a_uuid/unpublish"))
                    .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()));
        }

        @Test
        void unpublishGrantAdvertLambda_unexpectedError() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();

            doThrow(new RuntimeException()).when(grantAdvertService).unpublishAdvert(grantAdvertId, true);

            mockMvc.perform(post("/grant-advert/lambda/" + grantAdvertId + "/unpublish")
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isInternalServerError());
        }

        @Test
        void unpublishGrantAdvertLambda_notFoundError() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();

            doThrow(new NotFoundException()).when(grantAdvertService).unpublishAdvert(grantAdvertId, true);

            mockMvc.perform(post("/grant-advert/lambda/" + grantAdvertId + "/unpublish")
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isNotFound());
        }

    }

    @Nested
    class publishGrantAdvertLambda {

        final String LAMBDA_AUTH_HEADER = "topSecretKey";

        @Test
        void publishGrantAdvertLambda_success() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();
            final GrantAdvert grantAdvert = randomGrantAdvertEntity().id(grantAdvertId).build();

            when(grantAdvertService.publishAdvert(grantAdvertId, true)).thenReturn(grantAdvert);

            mockMvc.perform(post("/grant-advert/lambda/" + grantAdvertId + "/publish").header(HttpHeaders.AUTHORIZATION,
                    LAMBDA_AUTH_HEADER)).andExpect(status().isOk());
        }

        @Test
        void publishGrantAdvertLambda_badPathVariable() throws Exception {
            mockMvc.perform(get("/grant-advert/lambda/this_isnt_a_uuid/publish"))
                    .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()));
        }

        @Test
        void publishGrantAdvertLambda_unexpectedError() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();

            when(grantAdvertService.publishAdvert(grantAdvertId, true)).thenThrow(RuntimeException.class);

            mockMvc.perform(post("/grant-advert/lambda/" + grantAdvertId + "/publish").header(HttpHeaders.AUTHORIZATION,
                    LAMBDA_AUTH_HEADER)).andExpect(status().isInternalServerError());
        }

        @Test
        void publishGrantAdvertLambda_notFoundError() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();

            when(grantAdvertService.publishAdvert(grantAdvertId, true)).thenThrow(NotFoundException.class);

            mockMvc.perform(post("/grant-advert/lambda/" + grantAdvertId + "/publish").header(HttpHeaders.AUTHORIZATION,
                    LAMBDA_AUTH_HEADER)).andExpect(status().isNotFound());
        }

    }

    @Nested
    class getAdvertStatus {

        private final Integer grantSchemeId = 1;

        private final UUID grantAdvertId = UUID.randomUUID();

        private final GrantAdvertStatus grantAdvertStatus = GrantAdvertStatus.DRAFT;

        private final String contentfulSlug = "dummy-contentful-slug";

        @Test
        void getAdvertStatus_GrantAdvertStatusReturned() throws Exception {
            GetGrantAdvertStatusResponseDTO grantAdvertStatusResponseDTO = GetGrantAdvertStatusResponseDTO.builder()
                    .grantAdvertId(grantAdvertId).grantAdvertStatus(grantAdvertStatus).contentfulSlug(contentfulSlug)
                    .build();

            when(grantAdvertService.getGrantAdvertStatusBySchemeId(grantSchemeId))
                    .thenReturn(grantAdvertStatusResponseDTO);

            mockMvc.perform(get("/grant-advert/status").param("grantSchemeId", grantSchemeId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(grantAdvertStatusResponseDTO)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void getAdvertStatus_badRequest() throws Exception {

            mockMvc.perform(get("/grant-advert/status")).andExpect(status().isBadRequest());
        }

        @Test
        void getAdvertStatus_UnexpectedServiceError() throws Exception {
            when(grantAdvertService.getGrantAdvertStatusBySchemeId(grantSchemeId)).thenThrow(RuntimeException.class);

            mockMvc.perform(get("/grant-advert/status").param("grantSchemeId", grantSchemeId.toString()))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void getAdvertStatus_NoGrantAdvertFound() throws Exception {
            when(grantAdvertService.getGrantAdvertStatusBySchemeId(grantSchemeId)).thenThrow(NotFoundException.class);

            mockMvc.perform(get("/grant-advert/status").param("grantSchemeId", grantSchemeId.toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getAdvertStatus_NotEnoughPermissions() throws Exception {
            when(grantAdvertService.getGrantAdvertStatusBySchemeId(grantSchemeId))
                    .thenThrow(AccessDeniedException.class);

            mockMvc.perform(get("/grant-advert/status").param("grantSchemeId", grantSchemeId.toString()))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    class getAdvertPublishInformation {

        private final Integer grantSchemeId = 1;

        private final UUID grantAdvertId = UUID.randomUUID();

        private final GrantAdvertStatus grantAdvertStatus = GrantAdvertStatus.DRAFT;

        private final String contentfulSlug = "dummy-contentful-slug";

        private final Instant dateTimeInput = Instant.parse("2022-01-01T00:00:00.00Z");

        private final Instant openingDate = dateTimeInput;

        private final Instant closingDate = dateTimeInput;

        private final Instant firstPublishedDate = dateTimeInput;

        private final Instant lastPublishedDate = dateTimeInput;

        private final Instant unpublishedDate = dateTimeInput;

        @Test
        void getAdvertPublishingInformation_GrantAdvertPublishingInformationReturned() throws Exception {
            GetGrantAdvertPublishingInformationResponseDTO grantAdvertPublishingInformationResponseDTO = GetGrantAdvertPublishingInformationResponseDTO
                    .builder().grantAdvertId(grantAdvertId).grantAdvertStatus(grantAdvertStatus)
                    .contentfulSlug(contentfulSlug).unpublishedDate(unpublishedDate)
                    .firstPublishedDate(firstPublishedDate).lastPublishedDate(lastPublishedDate)
                    .closingDate(closingDate).openingDate(openingDate).build();

            when(grantAdvertService.getGrantAdvertPublishingInformationBySchemeId(grantSchemeId))
                    .thenReturn(grantAdvertPublishingInformationResponseDTO);

            mockMvc.perform(get("/grant-advert/publish-information").param("grantSchemeId", grantSchemeId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(grantAdvertPublishingInformationResponseDTO)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void getAdvertStatus_badRequest() throws Exception {

            mockMvc.perform(get("/grant-advert/publish-information")).andExpect(status().isBadRequest());
        }

        @Test
        void getAdvertStatus_UnexpectedServiceError() throws Exception {
            when(grantAdvertService.getGrantAdvertPublishingInformationBySchemeId(grantSchemeId))
                    .thenThrow(RuntimeException.class);

            mockMvc.perform(get("/grant-advert/publish-information").param("grantSchemeId", grantSchemeId.toString()))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void getAdvertStatus_NoGrantAdvertFound() throws Exception {
            when(grantAdvertService.getGrantAdvertPublishingInformationBySchemeId(grantSchemeId))
                    .thenThrow(NotFoundException.class);

            mockMvc.perform(get("/grant-advert/publish-information").param("grantSchemeId", grantSchemeId.toString()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getAdvertStatus_NotEnoughPermissions() throws Exception {
            when(grantAdvertService.getGrantAdvertPublishingInformationBySchemeId(grantSchemeId))
                    .thenThrow(AccessDeniedException.class);

            mockMvc.perform(get("/grant-advert/publish-information").param("grantSchemeId", grantSchemeId.toString()))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    class scheduledGrantAdvert {

        final UUID grantAdvertId = UUID.fromString("5b30cb45-7339-466a-a700-270c3983c604");

        @Test
        @WithAdminSession
        void scheduledGrantAdvert_Success() throws Exception {

            doNothing().when(grantAdvertService).scheduleGrantAdvert(grantAdvertId);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/schedule")).andExpect(status().isOk());

            verify(eventLogService).logAdvertPublishedEvent(any(), anyString(), anyLong(),
                    eq(grantAdvertId.toString()));
        }

        @Test
        void scheduledGrantAdvert_invalidUUIDGrantAdvertId() throws Exception {
            mockMvc.perform(post("/grant-advert/" + "hf7232nf" + "/schedule")).andExpect(status().isBadRequest());
        }

        @Test
        void scheduledGrantAdvert_NotFound() throws Exception {

            doThrow(new NotFoundException()).when(grantAdvertService).scheduleGrantAdvert(grantAdvertId);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/schedule")).andExpect(status().isNotFound());
        }

        @Test
        void scheduledGrantAdvert_AccessDenied() throws Exception {

            doThrow(new AccessDeniedException(null)).when(grantAdvertService).scheduleGrantAdvert(grantAdvertId);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/schedule")).andExpect(status().isForbidden());
        }

        @Test
        void scheduledGrantAdvert_GenericException() throws Exception {

            doThrow(new RuntimeException()).when(grantAdvertService).scheduleGrantAdvert(grantAdvertId);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/schedule"))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class unscheduleGrantAdvert {

        final UUID grantAdvertId = UUID.fromString("5b30cb45-7339-466a-a700-270c3983c604");

        @Test
        void scheduledGrantAdvert_Success() throws Exception {
            doNothing().when(grantAdvertService).unscheduleGrantAdvert(grantAdvertId);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/unschedule")).andExpect(status().isOk());
        }

        @Test
        void scheduledGrantAdvert_invalidUUIDGrantAdvertId() throws Exception {
            mockMvc.perform(post("/grant-advert/" + "hf7232nf" + "/unschedule")).andExpect(status().isBadRequest());
        }

        @Test
        void scheduledGrantAdvert_NotFound() throws Exception {
            doThrow(new NotFoundException()).when(grantAdvertService).unscheduleGrantAdvert(grantAdvertId);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/unschedule")).andExpect(status().isNotFound());
        }

        @Test
        void scheduledGrantAdvert_AccessDenied() throws Exception {
            doThrow(new AccessDeniedException(null)).when(grantAdvertService).unscheduleGrantAdvert(grantAdvertId);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/unschedule")).andExpect(status().isForbidden());
        }

        @Test
        void scheduledGrantAdvert_GenericException() throws Exception {
            doThrow(new RuntimeException()).when(grantAdvertService).unscheduleGrantAdvert(grantAdvertId);

            mockMvc.perform(post("/grant-advert/" + grantAdvertId + "/unschedule"))
                    .andExpect(status().isInternalServerError());
        }

    }

}
