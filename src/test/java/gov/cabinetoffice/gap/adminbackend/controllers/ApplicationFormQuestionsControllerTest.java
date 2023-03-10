package gov.cabinetoffice.gap.adminbackend.controllers;

import javax.servlet.http.HttpSession;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormQuestionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
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

import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_APPLICATION_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_OPTIONS;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_SECTION_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationFormQuestionsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { ApplicationFormQuestionsController.class, ControllerExceptionHandler.class })
class ApplicationFormQuestionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationFormService applicationFormService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Test
    void updateQuestionHappyPathTest() throws Exception {
        ApplicationFormQuestionDTO applicationFormQuestionDTO = new ApplicationFormQuestionDTO();
        applicationFormQuestionDTO.setDisplayText("New display text");
        doNothing().when(this.applicationFormService).patchQuestionValues(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                SAMPLE_QUESTION_ID, applicationFormQuestionDTO);

        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                        + "/questions/" + SAMPLE_QUESTION_ID).contentType(MediaType.APPLICATION_JSON)
                                .content(HelperUtils.asJsonString(applicationFormQuestionDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateQuestionEmptyBodyTest() throws Exception {

        this.mockMvc.perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                + "/questions/" + SAMPLE_QUESTION_ID)).andExpect(status().isBadRequest());
    }

    @Test
    void updateQuestionGenericErrorTest() throws Exception {
        ApplicationFormQuestionDTO applicationFormQuestionDTO = new ApplicationFormQuestionDTO();
        doThrow(new ApplicationFormException("Error message")).when(this.applicationFormService).patchQuestionValues(
                SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, applicationFormQuestionDTO);

        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                        + "/questions/" + SAMPLE_QUESTION_ID).contentType(MediaType.APPLICATION_JSON)
                                .content(HelperUtils.asJsonString(applicationFormQuestionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
    }

    @Test
    void updateQuestion_AccessDeniedTest() throws Exception {
        ApplicationFormQuestionDTO applicationFormQuestionDTO = new ApplicationFormQuestionDTO();
        doThrow(new AccessDeniedException("Error message")).when(this.applicationFormService).patchQuestionValues(
                SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, applicationFormQuestionDTO);

        this.mockMvc
                .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                        + "/questions/" + SAMPLE_QUESTION_ID).contentType(MediaType.APPLICATION_JSON)
                                .content(HelperUtils.asJsonString(applicationFormQuestionDTO)))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    void addQuestionHappyPathTest() throws Exception {
        ApplicationFormQuestionDTO applicationFormQuestionDTO = ApplicationFormQuestionDTO.builder()
                .fieldTitle("What is the question?").hintText("Enter a smart question")
                .responseType(ResponseTypeEnum.YesNo).build();

        when(this.applicationFormService.addQuestionToApplicationForm(eq(SAMPLE_APPLICATION_ID), eq(SAMPLE_SECTION_ID),
                eq(applicationFormQuestionDTO), any(HttpSession.class))).thenReturn(SAMPLE_QUESTION_ID);

        this.mockMvc.perform(
                post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID + "/questions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(applicationFormQuestionDTO)))
                .andExpect(status().isOk()).andExpect(content().json("{id: " + SAMPLE_QUESTION_ID + "}"));
    }

    @Test
    void addQuestionNullBodyTest() throws Exception {

        this.mockMvc.perform(
                post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID + "/questions/"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addQuestionNotFoundTest() throws Exception {
        ApplicationFormQuestionDTO applicationFormQuestionDTO = new ApplicationFormQuestionDTO();
        when(this.applicationFormService.addQuestionToApplicationForm(eq(SAMPLE_APPLICATION_ID), eq(SAMPLE_SECTION_ID),
                eq(applicationFormQuestionDTO), any(HttpSession.class)))
                        .thenThrow(new NotFoundException("Error message"));

        this.mockMvc.perform(
                post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID + "/questions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(applicationFormQuestionDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
    }

    @Test
    void addQuestionGenericErrorTest() throws Exception {
        ApplicationFormQuestionDTO applicationFormQuestionDTO = new ApplicationFormQuestionDTO();
        when(this.applicationFormService.addQuestionToApplicationForm(eq(SAMPLE_APPLICATION_ID), eq(SAMPLE_SECTION_ID),
                eq(applicationFormQuestionDTO), any(HttpSession.class)))
                        .thenThrow(new ApplicationFormException("Error message"));

        this.mockMvc.perform(
                post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID + "/questions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(applicationFormQuestionDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
    }

    @Test
    void addQuestion_AccessDeniedTest() throws Exception {

        ApplicationFormQuestionDTO applicationFormQuestionDTO = new ApplicationFormQuestionDTO();
        when(this.applicationFormService.addQuestionToApplicationForm(eq(SAMPLE_APPLICATION_ID), eq(SAMPLE_SECTION_ID),
                eq(applicationFormQuestionDTO), any(HttpSession.class)))
                        .thenThrow(new AccessDeniedException("Error message"));

        this.mockMvc.perform(
                post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID + "/questions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(applicationFormQuestionDTO)))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    void addQuestionWithOptionsHappyPathTest() throws Exception {
        ApplicationFormQuestionDTO applicationFormQuestionDTO = ApplicationFormQuestionDTO.builder()
                .fieldTitle("What is the question?").hintText("Enter a smart question")
                .responseType(ResponseTypeEnum.Dropdown).options(SAMPLE_QUESTION_OPTIONS).build();

        when(this.applicationFormService.addQuestionToApplicationForm(eq(SAMPLE_APPLICATION_ID), eq(SAMPLE_SECTION_ID),
                eq(applicationFormQuestionDTO), any(HttpSession.class))).thenReturn(SAMPLE_QUESTION_ID);

        this.mockMvc.perform(
                post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID + "/questions/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HelperUtils.asJsonString(applicationFormQuestionDTO)))
                .andExpect(status().isOk()).andExpect(content().json("{id: " + SAMPLE_QUESTION_ID + "}"));
    }

    @Test
    void deleteQuestionHappyPathTest() throws Exception {

        doNothing().when(this.applicationFormService).deleteQuestionFromSection(SAMPLE_APPLICATION_ID,
                SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID);

        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                + "/questions/" + SAMPLE_QUESTION_ID)).andExpect(status().isOk());
    }

    @Test
    void deleteQuestionDeleteFromMandatorySectionTest() throws Exception {

        this.mockMvc.perform(delete(
                "/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/ESSENTIAL/questions/" + SAMPLE_QUESTION_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteQuestionOrSectionDoesntExistTest() throws Exception {

        doThrow(new NotFoundException("Error message")).when(this.applicationFormService)
                .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, "incorrectId");

        this.mockMvc
                .perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                        + "/questions/incorrectId"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
    }

    @Test
    void deleteQuestion_AccessDeniedTest() throws Exception {

        doThrow(new AccessDeniedException("Error message")).when(this.applicationFormService)
                .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, "incorrectId");

        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                + "/questions/incorrectId")).andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    void getQuestionHappyPathTest() throws Exception {

        when(this.applicationFormService.retrieveQuestion(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID))
                .thenReturn(SAMPLE_QUESTION);

        this.mockMvc
                .perform(get("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                        + "/questions/" + SAMPLE_QUESTION_ID))
                .andExpect(status().isOk()).andExpect(content().json(HelperUtils.asJsonString(SAMPLE_QUESTION)));
    }

    @Test
    void getQuestionApplicationDoesntExistTest() throws Exception {

        when(this.applicationFormService.retrieveQuestion(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID))
                .thenThrow(new NotFoundException("Error message"));

        this.mockMvc
                .perform(get("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                        + "/questions/" + SAMPLE_QUESTION_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
    }

    @Test
    void getQuestion_AccessDeniedTest() throws Exception {

        when(this.applicationFormService.retrieveQuestion(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID))
                .thenThrow(new AccessDeniedException("Error message"));

        this.mockMvc
                .perform(get("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                        + "/questions/" + SAMPLE_QUESTION_ID))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    void getQuestionGenericErrorTest() throws Exception {

        when(this.applicationFormService.retrieveQuestion(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID))
                .thenThrow(new RuntimeException("Error message"));

        this.mockMvc
                .perform(get("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID
                        + "/questions/" + SAMPLE_QUESTION_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
    }

}
