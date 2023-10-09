package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormPatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormsFoundDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;

import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.*;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators.randomApplicationFormFound;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationFormController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { ApplicationFormController.class, ControllerExceptionHandler.class })
class ApplicationFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationFormService applicationFormService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Test
    void saveApplicationFormHappyPathTest() throws Exception {
        when(this.applicationFormService.saveApplicationForm(SAMPLE_APPLICATION_POST_FORM_DTO))
                .thenReturn(SAMPLE_APPLICATION_RESPONSE_SUCCESS);

        this.mockMvc
                .perform(post("/application-forms/").contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_APPLICATION_POST_FORM_DTO)))
                .andExpect(status().isCreated());

    }

    @Test
    void saveApplicationFormUnhappyPathNoTemplateFound() throws Exception {
        when(this.applicationFormService.saveApplicationForm(SAMPLE_APPLICATION_POST_FORM_DTO))
                .thenThrow(new ApplicationFormException("Error message"));

        this.mockMvc
                .perform(post("/application-forms/").contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_APPLICATION_POST_FORM_DTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));

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
    void updateApplicationForm_SuccessfullyUpdatingApplication() throws Exception {
        doNothing().when(this.applicationFormService).patchApplicationForm(SAMPLE_APPLICATION_ID,
                SAMPLE_PATCH_APPLICATION_DTO, false);
        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_PATCH_APPLICATION_DTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateApplicationForm_BadRequest_NoApplicationPropertiesProvided() throws Exception {
        doNothing().when(this.applicationFormService).patchApplicationForm(SAMPLE_APPLICATION_ID,
                SAMPLE_PATCH_APPLICATION_DTO, false);
        this.mockMvc.perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON).content("{ \"testProp\": \"doesnt exist\"}"))
                .andExpect(status().isBadRequest());

        verify(this.applicationFormService, never()).patchApplicationForm(anyInt(), any(ApplicationFormPatchDTO.class),
                false);
    }

    @Test
    void updateApplicationForm_BadRequest_InvalidPropertieValue() throws Exception {
        this.mockMvc.perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON).content("{ \"applicationStatus\": \"INCORRECT\"}"))
                .andExpect(status().isBadRequest());

        verify(this.applicationFormService, never()).patchApplicationForm(anyInt(), any(ApplicationFormPatchDTO.class),
                false);
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
    }

    @Test
    void updateApplicationForm_AccessDenied() throws Exception {
        doThrow(new AccessDeniedException("Error")).when(this.applicationFormService)
                .patchApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_PATCH_APPLICATION_DTO, false);
        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(SAMPLE_PATCH_APPLICATION_DTO)))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
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
    }

}
