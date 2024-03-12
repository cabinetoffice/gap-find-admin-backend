package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationSectionOrderPatchDto;
import gov.cabinetoffice.gap.adminbackend.dtos.application.PatchSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.PostSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.FieldErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.models.ValidationError;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormSectionService;
import gov.cabinetoffice.gap.adminbackend.services.EventLogService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockBean
    private EventLogService eventLogService;

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
    @WithAdminSession
    void addSectionHappyPathTest() throws Exception {
        PostSectionDTO newSection = new PostSectionDTO("New section title");

        when(this.applicationFormSectionService.addSectionToApplicationForm(SAMPLE_APPLICATION_ID, newSection))
                .thenReturn(SAMPLE_SECTION_ID);

        this.mockMvc
                .perform(post("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections")
                        .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(newSection)))
                .andExpect(status().isOk()).andExpect(content().json("{id: " + SAMPLE_SECTION_ID + "}"));

        verify(eventLogService).logApplicationUpdatedEvent(any(), anyString(), anyLong(),
                eq(SAMPLE_APPLICATION_ID.toString()));
    }

    @Test
    @WithAdminSession
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
    @WithAdminSession
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
    @WithAdminSession
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

        @Test
        void updateSectionTitle__HappyPath() throws Exception {
            PatchSectionDTO patchSectionDTO = new PatchSectionDTO().builder().sectionTitle("sectionTitle").version(1).build();

            doNothing().when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionTitle(any(), any(), any(), any());

            ApplicationFormSectionsControllerTest.this.mockMvc.perform(
                    patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "CUSTOM_SECTION" + "/title")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(patchSectionDTO)))
                    .andExpect(status().isOk());
        }

        @Test
        void updateSectionTitle__BadRequest__NoPayload() throws Exception {
            doNothing().when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionTitle(any(), any(), any(), any());

            ApplicationFormSectionsControllerTest.this.mockMvc
                    .perform(patch(
                            "/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "CUSTOM_SECTION" + "/title"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updateSectionTitle__BadRequest__NotACustomSection() throws Exception {
            String sectionTitle = "sectionTitle";
            doNothing().when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionTitle(any(), any(), any(), any());

            ApplicationFormSectionsControllerTest.this.mockMvc.perform(
                    patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "ELIGIBILITY" + "/title")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(sectionTitle)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updateSectionTitle__NotFound() throws Exception {
            PatchSectionDTO patchSectionDTO = new PatchSectionDTO().builder().sectionTitle("sectionTitle").version(1).build();
            doThrow(NotFoundException.class)
                    .when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionTitle(any(), any(), any(), any());

            ApplicationFormSectionsControllerTest.this.mockMvc.perform(
                    patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "CUSTOM_SECTION" + "/title")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(patchSectionDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void updateSectionTitle__AccessDenied() throws Exception {
            PatchSectionDTO patchSectionDTO = new PatchSectionDTO().builder().sectionTitle("sectionTitle").version(1).build();
            doThrow(AccessDeniedException.class)
                    .when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionTitle(any(), any(), any(), any());

            ApplicationFormSectionsControllerTest.this.mockMvc.perform(
                    patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/" + "CUSTOM_SECTION" + "/title")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(patchSectionDTO)))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @WithAdminSession
    class updateSectionOrder {

        @Test
        void updateSectionOrderHappyPathTest() throws Exception {

            doNothing().when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionOrder(SAMPLE_APPLICATION_ID, "A-random-uuid", 1, SAMPLE_VERSION);

            ApplicationSectionOrderPatchDto applicationSectionOrderPatchDto = ApplicationSectionOrderPatchDto.builder()
                    .sectionId("test").increment(1).build();
            ApplicationFormSectionsControllerTest.this.mockMvc
                    .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(HelperUtils.asJsonString(applicationSectionOrderPatchDto)))
                    .andExpect(status().isOk());
        }

        @Test
        void updateSectionOrderNoBody() throws Exception {

            ApplicationFormSectionsControllerTest.this.mockMvc
                    .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/order"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updateSectionOrder_AccessDeniedTest() throws Exception {

            doThrow(new AccessDeniedException("Error message"))
                    .when(ApplicationFormSectionsControllerTest.this.applicationFormSectionService)
                    .updateSectionOrder(SAMPLE_APPLICATION_ID, "test", 1, SAMPLE_VERSION);

            ApplicationSectionOrderPatchDto applicationSectionOrderPatchDto = ApplicationSectionOrderPatchDto.builder()
                    .sectionId("test").increment(1).build();

            ApplicationFormSectionsControllerTest.this.mockMvc
                    .perform(patch("/application-forms/" + SAMPLE_APPLICATION_ID + "/sections/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(HelperUtils.asJsonString(applicationSectionOrderPatchDto)))
                    .andExpect(status().isForbidden()).andExpect(content().string(""));
        }

    }

}
