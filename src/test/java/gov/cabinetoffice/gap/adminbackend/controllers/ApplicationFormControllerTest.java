package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.LambdasInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormPatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormsFoundDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.EncryptedEmailAddressDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.*;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.OdtException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.*;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.*;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators.randomApplicationFormFound;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationFormController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(
        classes = { ApplicationFormController.class, ControllerExceptionHandler.class, LambdasInterceptor.class })
class ApplicationFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationFormService applicationFormService;

    @MockBean
    @Qualifier("submissionExportAndScheduledPublishingLambdasInterceptor")
    private AuthorizationHeaderInterceptor mockAuthorizationHeaderInterceptor;

    @MockBean
    private LambdasInterceptor mockLambdasInterceptor;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @MockBean
    private GrantAdvertService grantAdvertService;

    @MockBean
    private ApplicationFormRepository applicationFormRepository;

    @MockBean
    private EventLogService eventLogService;

    @MockBean
    private SchemeService schemeService;

    @MockBean
    private UserService userService;

    @MockBean
    private OdtService odtService;

    @Test
    @WithAdminSession
    void saveApplicationFormHappyPathTest() throws Exception {
        final SchemeDTO schemeDTO = SchemeDTO.builder().build();
        when(this.schemeService.getSchemeBySchemeId(SAMPLE_APPLICATION_POST_FORM_DTO.getGrantSchemeId()))
                .thenReturn(schemeDTO);
        when(this.applicationFormService.saveApplicationForm(SAMPLE_APPLICATION_POST_FORM_DTO, schemeDTO))
                .thenReturn(SAMPLE_APPLICATION_RESPONSE_SUCCESS);

        this.mockMvc
                .perform(post("/application-forms/").contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_APPLICATION_POST_FORM_DTO)))
                .andExpect(status().isCreated());

        verify(eventLogService).logApplicationCreatedEvent(any(), anyString(), anyLong(),
                eq(SAMPLE_APPLICATION_ID.toString()));

    }

    @Test
    void saveApplicationFormUnhappyPathNoTemplateFound() throws Exception {
        final SchemeDTO schemeDTO = SchemeDTO.builder().build();
        when(this.schemeService.getSchemeBySchemeId(SAMPLE_APPLICATION_POST_FORM_DTO.getGrantSchemeId()))
                .thenReturn(schemeDTO);

        when(this.applicationFormService.saveApplicationForm(SAMPLE_APPLICATION_POST_FORM_DTO, schemeDTO))
                .thenThrow(new ApplicationFormException("Error message"));

        this.mockMvc
                .perform(post("/application-forms/").contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_APPLICATION_POST_FORM_DTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));

        verifyNoInteractions(eventLogService);

    }

    @Test
    void checkApplicationFormExists_ApplicationFormExists_SingleProp() throws Exception {
        ApplicationFormsFoundDTO applicationFormFoundDTO = randomApplicationFormFound().build();
        List<ApplicationFormsFoundDTO> applicationFormsFoundList = Collections.singletonList(applicationFormFoundDTO);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grantSchemeId", SAMPLE_SCHEME_ID.toString());

        when(this.applicationFormService.getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP))
                .thenReturn(applicationFormsFoundList);
        this.mockMvc.perform(get("/application-forms/find").contentType(MediaType.APPLICATION_JSON).params(params))
                .andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(applicationFormsFoundList)));
    }

    @Test
    void checkApplicationFormExists_ApplicationFormExists_MultipleProps() throws Exception {
        ApplicationFormsFoundDTO applicationFormFoundDTO = randomApplicationFormFound().build();
        List<ApplicationFormsFoundDTO> applicationFormsFoundList = Collections.singletonList(applicationFormFoundDTO);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grantApplicationId", SAMPLE_APPLICATION_ID.toString());
        params.add("grantSchemeId", SAMPLE_SCHEME_ID.toString());
        params.add("applicationName", SAMPLE_APPLICATION_NAME);

        when(this.applicationFormService
                .getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_MULTIPLE_PROPS))
                        .thenReturn(applicationFormsFoundList);
        this.mockMvc.perform(get("/application-forms/find").contentType(MediaType.APPLICATION_JSON).params(params))
                .andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(applicationFormsFoundList)));
    }

    @Test
    void checkApplicationFormExists_MultipleApplicationFormExist() throws Exception {
        ApplicationFormsFoundDTO applicationFormFoundDTO = randomApplicationFormFound().build();
        List<ApplicationFormsFoundDTO> applicationFormsFoundList = Collections.singletonList(applicationFormFoundDTO);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grantSchemeId", SAMPLE_SCHEME_ID.toString());

        when(this.applicationFormService.getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP))
                .thenReturn(applicationFormsFoundList);
        this.mockMvc.perform(get("/application-forms/find").contentType(MediaType.APPLICATION_JSON).params(params))
                .andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(applicationFormsFoundList)));
    }

    @Test
    void checkApplicationFormExists_ApplicationFormNotFound_SingleProp() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grantSchemeId", SAMPLE_SCHEME_ID.toString());

        when(this.applicationFormService.getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP))
                .thenReturn(Collections.emptyList());
        this.mockMvc.perform(get("/application-forms/find").contentType(MediaType.APPLICATION_JSON).params(params))
                .andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    void checkApplicationFormExists_ApplicationFromNotFound_MultipleProps() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grantApplicationId", SAMPLE_APPLICATION_ID.toString());
        params.add("grantSchemeId", SAMPLE_SCHEME_ID.toString());
        params.add("applicationName", SAMPLE_APPLICATION_NAME);

        when(this.applicationFormService
                .getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_MULTIPLE_PROPS))
                        .thenReturn(Collections.emptyList());
        this.mockMvc.perform(get("/application-forms/find").contentType(MediaType.APPLICATION_JSON).params(params))
                .andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    void checkApplicationFormExists_InvalidRequestBody() throws Exception {
        this.mockMvc.perform(get("/application-forms/find")).andExpect(status().isBadRequest())
                .andExpect(content().json(HelperUtils.asJsonString(SAMPLE_CLASS_ERROR_NO_PROPS_PROVIDED)));
    }

    @Test
    void retrieveApplicationFormSummaryHappyPathTest() throws Exception {
        when(this.applicationFormService.retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, true, true))
                .thenReturn(SAMPLE_APPLICATION_FORM_DTO);
        this.mockMvc.perform(get("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(SAMPLE_APPLICATION_POST_FORM_DTO)));
    }

    @Test
    void retrieveApplicationFormSummaryNoSectionsPathTest() throws Exception {
        when(this.applicationFormService.retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, false, true))
                .thenReturn(SAMPLE_APPLICATION_FORM_DTO);
        this.mockMvc
                .perform(get("/application-forms/" + SAMPLE_APPLICATION_ID + "?withSections=false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(SAMPLE_APPLICATION_POST_FORM_DTO)));
    }

    @Test
    void retrieveApplicationFormSummaryNoQuestionsPathTest() throws Exception {
        when(this.applicationFormService.retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, true, false))
                .thenReturn(SAMPLE_APPLICATION_FORM_DTO);
        this.mockMvc
                .perform(get("/application-forms/" + SAMPLE_APPLICATION_ID + "?withQuestions=false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(SAMPLE_APPLICATION_POST_FORM_DTO)));
    }

    @Test
    void retrieveApplicationFormSummaryNotFoundTest() throws Exception {
        when(this.applicationFormService.retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, true, true))
                .thenThrow(new ApplicationFormException());
        this.mockMvc.perform(get("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void retrieveApplicationFormSummary_AccessDeniedTest() throws Exception {
        when(this.applicationFormService.retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, true, true))
                .thenThrow(new AccessDeniedException("Error"));
        this.mockMvc.perform(get("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    void retrieveApplicationFormSummaryUnexpectedErrorTest() throws Exception {
        when(this.applicationFormService.retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, true, true))
                .thenThrow(new RuntimeException("Generic error message"));
        this.mockMvc.perform(get("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Generic error message"))));
    }

    @Test
    void deleteApplicationFormHappyPathTest() throws Exception {
        doNothing().when(this.applicationFormService).deleteApplicationForm(SAMPLE_APPLICATION_ID);
        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID)).andExpect(status().isOk());
    }

    @Test
    void deleteApplicationFormFailsTest() throws Exception {
        doThrow(new ApplicationFormException("Could not delete application form with id " + SAMPLE_APPLICATION_ID))
                .when(this.applicationFormService).deleteApplicationForm(SAMPLE_APPLICATION_ID);
        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID))
                .andExpect(status().isInternalServerError()).andExpect(content().json(HelperUtils.asJsonString(
                        new GenericErrorDTO("Could not delete application form with id " + SAMPLE_APPLICATION_ID))));
    }

    @Test
    void deleteApplicationForm_AccessDeniedTest() throws Exception {
        doThrow(new AccessDeniedException("Error")).when(this.applicationFormService)
                .deleteApplicationForm(SAMPLE_APPLICATION_ID);

        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID)).andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void removesApplicationAttachedToGrantAdvert_Successfully() throws Exception {
        SchemeEntity scheme = SchemeEntity.builder().id(1).name("scheme").build();
        GrantAdvert grantAdvert = GrantAdvert.builder().grantAdvertName("grant-advert").scheme(scheme).build();
        when(grantAdvertService.getAdvertById(SAMPLE_ADVERT_ID)).thenReturn(grantAdvert);
        when(applicationFormService.getOptionalApplicationFromSchemeId(scheme.getId()))
                .thenReturn(Optional.of(ApplicationFormEntity.builder().grantApplicationId(1)
                        .applicationName("application").grantSchemeId(scheme.getId()).build()));
        doNothing().when(this.applicationFormService).patchApplicationForm(SAMPLE_APPLICATION_ID,
                SAMPLE_PATCH_APPLICATION_DTO, true);

        this.mockMvc
                .perform(delete("/application-forms/lambda/" + SAMPLE_ADVERT_ID + "/application/")
                        .contentType(MediaType.APPLICATION_JSON).header("Authorization", "shh"))
                .andExpect(status().isNoContent());

        Mockito.verify(applicationFormService, Mockito.times(1)).patchApplicationForm(1,
                new ApplicationFormPatchDTO(ApplicationStatusEnum.REMOVED), true);
    }

    @Test
    void removesApplicationAttachedToGrantAdvert_throwsNotFoundWhenNoAdvertFound() throws Exception {
        doThrow(NotFoundException.class).when(grantAdvertService).getAdvertById(SAMPLE_ADVERT_ID);

        this.mockMvc
                .perform(delete("/application-forms/lambda/" + SAMPLE_ADVERT_ID + "/application/")
                        .contentType(MediaType.APPLICATION_JSON).header("Authorization", "shh"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removesApplicationAttachedToGrantAdvert_throwsApplicationFormExceptionWhenUnableToPatch() throws Exception {
        SchemeEntity scheme = SchemeEntity.builder().id(1).name("scheme").build();
        GrantAdvert grantAdvert = GrantAdvert.builder().grantAdvertName("grant-advert").scheme(scheme).build();
        when(grantAdvertService.getAdvertById(SAMPLE_ADVERT_ID)).thenReturn(grantAdvert);
        when(applicationFormService.getOptionalApplicationFromSchemeId(scheme.getId()))
                .thenReturn(Optional.of(ApplicationFormEntity.builder().grantApplicationId(1)
                        .applicationName("application").grantSchemeId(scheme.getId()).build()));

        doThrow(ApplicationFormException.class).when(this.applicationFormService).patchApplicationForm(anyInt(), any(),
                eq(true));

        this.mockMvc
                .perform(delete("/application-forms/lambda/" + SAMPLE_ADVERT_ID + "/application/")
                        .contentType(MediaType.APPLICATION_JSON).header("Authorization", "shh"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithAdminSession
    void updateApplicationForm_SuccessfullyUpdatingApplication() throws Exception {
        doNothing().when(this.applicationFormService).patchApplicationForm(SAMPLE_APPLICATION_ID,
                SAMPLE_PATCH_UPDATED_APPLICATION_DTO, false);
        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_PATCH_UPDATED_APPLICATION_DTO)))
                .andExpect(status().isNoContent());

        verify(eventLogService).logApplicationUpdatedEvent(any(), anyString(), anyLong(),
                eq(SAMPLE_APPLICATION_ID.toString()));
    }

    @Test
    @WithAdminSession
    void updateApplicationForm_SuccessfullyPublishingApplication() throws Exception {
        doNothing().when(this.applicationFormService).patchApplicationForm(SAMPLE_APPLICATION_ID,
                SAMPLE_PATCH_APPLICATION_DTO, false);
        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_PATCH_APPLICATION_DTO)))
                .andExpect(status().isNoContent());

        verify(eventLogService).logApplicationPublishedEvent(any(), anyString(), anyLong(),
                eq(SAMPLE_APPLICATION_ID.toString()));
    }

    @Test
    void updateApplicationForm_BadRequest_NoApplicationPropertiesProvided() throws Exception {
        doNothing().when(this.applicationFormService).patchApplicationForm(SAMPLE_APPLICATION_ID,
                SAMPLE_PATCH_APPLICATION_DTO, false);
        this.mockMvc.perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON).content("{ \"testProp\": \"doesnt exist\"}"))
                .andExpect(status().isBadRequest());

        verify(this.applicationFormService, never()).patchApplicationForm(anyInt(), any(ApplicationFormPatchDTO.class),
                eq(false));

        verifyNoInteractions(eventLogService);
    }

    @Test
    void updateApplicationForm_BadRequest_InvalidPropertieValue() throws Exception {
        this.mockMvc.perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON).content("{ \"applicationStatus\": \"INCORRECT\"}"))
                .andExpect(status().isBadRequest());

        verify(this.applicationFormService, never()).patchApplicationForm(anyInt(), any(ApplicationFormPatchDTO.class),
                eq(false));

        verifyNoInteractions(eventLogService);
    }

    @Test
    void updateApplicationForm_ApplicationFormNotFound() throws Exception {
        doThrow(new NotFoundException("Not Found Message")).when(this.applicationFormService)
                .patchApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_PATCH_APPLICATION_DTO, false);
        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_PATCH_APPLICATION_DTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Not Found Message"))));

        verifyNoInteractions(eventLogService);
    }

    @Test
    void updateApplicationForm_AccessDenied() throws Exception {
        doThrow(new AccessDeniedException("Error")).when(this.applicationFormService)
                .patchApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_PATCH_APPLICATION_DTO, false);
        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_PATCH_APPLICATION_DTO)))
                .andExpect(status().isForbidden()).andExpect(content().string(""));

        verifyNoInteractions(eventLogService);
    }

    @Test
    void updateApplicationForm_GenericApplicationFormException() throws Exception {
        doThrow(new ApplicationFormException("Application Form Error Message")).when(this.applicationFormService)
                .patchApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_PATCH_APPLICATION_DTO, false);
        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_PATCH_APPLICATION_DTO)))
                .andExpect(status().isInternalServerError()).andExpect(content()
                        .json(HelperUtils.asJsonString(new GenericErrorDTO("Application Form Error Message"))));

        verifyNoInteractions(eventLogService);
    }

    @Test
    void getLastUpdatedEmailHappyPath() throws Exception {
        byte[] encryptedEmail = "test@test.gov".getBytes();
        EncryptedEmailAddressDTO encryptedLastUpdatedEmailDTO =
                EncryptedEmailAddressDTO.builder().encryptedEmail(encryptedEmail).build();
        when(userService.getEmailAddressForSub(anyString())).thenReturn(encryptedEmail);
        when(applicationFormRepository.findById(anyInt())).thenReturn(Optional.of(ApplicationFormEntity.builder().lastUpdateBy(1).build()));
        when(userService.getGrantAdminById(anyInt())).thenReturn(Optional.of(GrantAdmin.builder().gapUser(GapUser.builder().userSub("sub").build()).build()));
        when(userService.getEmailAddressForSub(anyString())).thenReturn("test@test.gov".getBytes());
        when(applicationFormService.getApplicationById(anyInt())).thenReturn(ApplicationFormEntity.builder()
                .lastUpdateBy(1).build());

        this.mockMvc.perform(get("/application-forms/1/lastUpdated/email")).andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(encryptedLastUpdatedEmailDTO)));

    }

    @Test
    void shouldReturnDeletedUserWhenLastUpdatedByIsNullAndLastUpdatedIsValid() throws Exception {
        when(userService.getEmailAddressForSub(anyString())).thenReturn("test@test.gov".getBytes());
        when(applicationFormService.getApplicationById(anyInt())).thenReturn(ApplicationFormEntity.builder()
                .lastUpdated(Instant.now()).build());

        this.mockMvc.perform(get("/application-forms/1/lastUpdated/email")).andExpect(status().isOk())
                .andExpect(content().json(
                        HelperUtils.asJsonString(EncryptedEmailAddressDTO.builder().deletedUser(true).build())
                ));
    }

    @Test
    void getLastUpdatedEmailReturnsNotFoundWhenNoGrantAdminFound() throws Exception {
        when(applicationFormService.getApplicationById(anyInt())).thenReturn(ApplicationFormEntity.builder()
                .lastUpdateBy(1).build());
        when(userService.getGrantAdminById(anyInt())).thenReturn(Optional.empty());
        this.mockMvc.perform(get("/application-forms/1/lastUpdated/email")).andExpect(status().isNotFound());
    }

    @Test
    void getApplicationStatus() throws Exception {
        when(applicationFormService.getApplicationStatus(anyInt())).thenReturn(ApplicationStatusEnum.PUBLISHED);

        this.mockMvc.perform(get("/application-forms/1/status"))
                .andExpect(status().isOk())
                .andExpect(content().string(ApplicationStatusEnum.PUBLISHED.toString()));
    }

    @Test
    void getApplicationStatusNotFoundException() throws Exception {
        when(applicationFormService.getApplicationStatus(anyInt())).thenThrow(new NotFoundException());

        this.mockMvc.perform(get("/application-forms/1/status"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testExportApplication() throws Exception {
        Integer applicationId = 1;
        OdfTextDocument odfTextDocument = OdfTextDocument.newTextDocument();
        when(applicationFormService.getApplicationFormExport(applicationId)).thenReturn(odfTextDocument);
        when(odtService.odtToResource(any())).thenReturn(new ByteArrayResource(new byte[]{1}));

        this.mockMvc.perform(get("/application-forms/1/download-summary"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"application.odt\""))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));
        verify(applicationFormService, times(1)).getApplicationFormExport(any());
        verify(odtService, times(1)).odtToResource(any());
    }

    @Test
    void testExportApplicationOdtServiceThrowsIOException() throws Exception {
        Integer applicationId = 1;
        OdfTextDocument odfTextDocument = OdfTextDocument.newTextDocument();
        when(applicationFormService.getApplicationFormExport(applicationId)).thenReturn(odfTextDocument);
        when(odtService.odtToResource(Mockito.any())).thenThrow(new IOException());

        MvcResult result = this.mockMvc.perform(get("/application-forms/" + applicationId + "/download-summary"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        Exception resolvedException = result.getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(OdtException.class, resolvedException.getClass());
        verify(applicationFormService, times(1)).getApplicationFormExport(any());
        verify(odtService, times(1)).odtToResource(any());


        UUID submissionId = UUID.randomUUID();
        HttpServletRequest mockRequest = new MockHttpServletRequest();
        Submission mockSubmission = mock(Submission.class);
    }


    @Test
    void testExportApplicationThrowsRuntimeError() throws Exception {
        when(applicationFormService.getApplicationFormExport(Mockito.any()))
                .thenThrow(new RuntimeException());

        MvcResult result = this.mockMvc.perform(get("/application-forms/1/download-summary"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        Exception resolvedException = result.getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(OdtException.class, resolvedException.getClass());
        verify(applicationFormService, times(1)).getApplicationFormExport(any());
        verify(odtService, times(0)).odtToResource(any());
    }

}
