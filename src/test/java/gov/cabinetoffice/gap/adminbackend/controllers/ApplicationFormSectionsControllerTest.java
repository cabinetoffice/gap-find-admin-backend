package gov.cabinetoffice.gap.adminbackend.controllers;

import java.util.Collections;

import gov.cabinetoffice.gap.adminbackend.dtos.application.PostSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.FieldErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.ValidationError;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormSectionService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Nested;
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
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_SECTION;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_SECTION_ID;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

@WebMvcTest(ApplicationFormSectionsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { ApplicationFormSectionsController.class, ControllerExceptionHandler.class })
class ApplicationFormSectionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationFormSectionService applicationFormSectionService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Test
    void getApplicationFormSectionById_SuccessfullyGetSection() throws Exception {
        when(this.applicationFormSectionService.getSectionById(anyInt(), anyString(), eq(true)))
                .thenReturn(SAMPLE_SECTION);

        this.mockMvc.perform(get("/application-forms/12345/sections/SECTIONID").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(HelperUtils.asJsonString(SAMPLE_SECTION))).andExpect(status().isOk());
    }

    @Test
    void getApplicationFormSectionById_SectionNotFound() throws Exception {
        when(this.applicationFormSectionService.getSectionById(anyInt(), anyString(), eq(true)))
                .thenThrow(new NotFoundException());

        this.mockMvc.perform(get("/application-forms/12345/sections/SECTIONID").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("")).andExpect(status().isNotFound());
    }

    @Test
    void getApplicationFormSectionById_FoundMultipleSectionsWithSameId() throws Exception {
        when(this.applicationFormSectionService.getSectionById(anyInt(), anyString(), eq(true)))
                .thenThrow(new ApplicationFormException());

        this.mockMvc.perform(get("/application-forms/12345/sections/SECTIONID").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("")).andExpect(status().isBadRequest());
    }

    @Test
    void getApplicationFormSectionById_NoQuestions() throws Exception {
        when(this.applicationFormSectionService.getSectionById(anyInt(), anyString(), eq(false)))
                .thenReturn(SAMPLE_SECTION);

        this.mockMvc
                .perform(get("/application-forms/12345/sections/SECTIONID?withQuestions=false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(HelperUtils.asJsonString(SAMPLE_SECTION))).andExpect(status().isOk());
    }

    @Test
    void getApplicationFormSectionById_UnexpectedError() throws Exception {
        String expectedErrorMessage = "Uncaught exception";
        GenericErrorDTO expectedError = new GenericErrorDTO(expectedErrorMessage);

        when(this.applicationFormSectionService.getSectionById(anyInt(), anyString(), anyBoolean()))
                .thenThrow(new RuntimeException(expectedErrorMessage));

        this.mockMvc.perform(get("/application-forms/12345/sections/SECTIONID").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(HelperUtils.asJsonString(expectedError)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getApplicationFormSectionById_AccessDenied() throws Exception {

        when(this.applicationFormSectionService.getSectionById(anyInt(), anyString(), anyBoolean()))
                .thenThrow(new AccessDeniedException("Error"));

        this.mockMvc.perform(get("/application-forms/12345/sections/SECTIONID").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("")).andExpect(status().isForbidden());
    }

    @Test
    void addSectionHappyPathTest() throws Exception {
        PostSectionDTO newSection = new PostSectionDTO("New section title");

        when(this.applicationFormSectionService.addSectionToApplicationForm(SAMPLE_APPLICATION_ID, newSection))
                .thenReturn(SAMPLE_SECTION_ID);

        this.mockMvc
                .perform(post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections")
                        .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(newSection)))
                .andExpect(status().isOk()).andExpect(content().json("{id: " + SAMPLE_SECTION_ID + "}"));
    }

    @Test
    void addSectionApplicationNotFoundTest() throws Exception {
        PostSectionDTO newSection = new PostSectionDTO("New section title");
        String exceptionMessage = "Application with id " + SAMPLE_APPLICATION_ID + " does not exist";

        when(this.applicationFormSectionService.addSectionToApplicationForm(SAMPLE_APPLICATION_ID, newSection))
                .thenThrow(new NotFoundException(exceptionMessage));

        this.mockMvc
                .perform(post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections")
                        .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(newSection)))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{error: { message: \"" + exceptionMessage + "\"} }"));
    }

    @Test
    void addSectionInvalidDtoTest() throws Exception {
        PostSectionDTO newSection = new PostSectionDTO("");

        this.mockMvc
                .perform(post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections")
                        .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(newSection)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(HelperUtils.asJsonString(new FieldErrorsDTO(
                        Collections.singletonList(new ValidationError("sectionTitle", "Enter a section name"))))));
    }

    @Test
    void addSectionGenericErrorTest() throws Exception {
        PostSectionDTO newSection = new PostSectionDTO("New section title");

        when(this.applicationFormSectionService.addSectionToApplicationForm(SAMPLE_APPLICATION_ID, newSection))
                .thenThrow(new RuntimeException("Error message"));

        this.mockMvc
                .perform(post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections")
                        .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(newSection)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
    }

    @Test
    void addSection_AccessDeniedTest() throws Exception {
        PostSectionDTO newSection = new PostSectionDTO("New section title");

        when(this.applicationFormSectionService.addSectionToApplicationForm(SAMPLE_APPLICATION_ID, newSection))
                .thenThrow(new AccessDeniedException("Error message"));

        this.mockMvc
                .perform(post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections")
                        .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(newSection)))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    void deleteSectionHappyPathTest() throws Exception {

        doNothing().when(this.applicationFormSectionService).deleteSectionFromApplication(SAMPLE_APPLICATION_ID,
                SAMPLE_SECTION_ID);

        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID))
                .andExpect(status().isOk());
    }

    @Test
    void deleteSectionDeleteMandatorySectionTest() throws Exception {

        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "ESSENTIAL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSectionDoesntExistTest() throws Exception {

        doThrow(new NotFoundException("Error message")).when(this.applicationFormSectionService)
                .deleteSectionFromApplication(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID);

        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
    }

    @Test
    void deleteSectionAccessDeniedTest() throws Exception {

        doThrow(new AccessDeniedException("Error message")).when(this.applicationFormSectionService)
                .deleteSectionFromApplication(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID);

        this.mockMvc.perform(delete("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + SAMPLE_SECTION_ID))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Nested
    class updateSectionStatus {

        @Test
        void updateSectionStatusHappyPathTest() throws Exception {

            doNothing().when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionStatus(SAMPLE_APPLICATION_ID, "ESSENTIAL", SectionStatusEnum.COMPLETE);

            ApplicationFormSectionsControllerTest.this.mockMvc
                    .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "ESSENTIAL")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString("COMPLETE")))
                    .andExpect(status().isOk());
        }

        @Test
        void updateSectionStatusCustomSectionTest() throws Exception {

            ApplicationFormSectionsControllerTest.this.mockMvc
                    .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "CUSTOM SECTION"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updateSectionStatusSectionDoesntExistTest() throws Exception {

            doThrow(new NotFoundException("Error message"))
                    .when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionStatus(SAMPLE_APPLICATION_ID, "ESSENTIAL", SectionStatusEnum.COMPLETE);

            ApplicationFormSectionsControllerTest.this.mockMvc
                    .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "ESSENTIAL")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString("COMPLETE")))
                    .andExpect(status().isNotFound())
                    .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Error message"))));
        }

        @Test
        void updateSectionStatus_AccessDeniedTest() throws Exception {

            doThrow(new AccessDeniedException("Error message"))
                    .when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionStatus(SAMPLE_APPLICATION_ID, "ESSENTIAL", SectionStatusEnum.COMPLETE);

            ApplicationFormSectionsControllerTest.this.mockMvc
                    .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "ESSENTIAL")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString("COMPLETE")))
                    .andExpect(status().isForbidden()).andExpect(content().string(""));
        }

    }

}
