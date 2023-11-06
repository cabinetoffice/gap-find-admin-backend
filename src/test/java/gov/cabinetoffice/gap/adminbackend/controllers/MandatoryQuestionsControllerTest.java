package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GrantMandatoryQuestionsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { GrantMandatoryQuestionsController.class, ControllerExceptionHandler.class })
class MandatoryQuestionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrantMandatoryQuestionService grantMandatoryQuestionService;

    @Value("classpath:spotlight/XLSX_Spotlight_Template.xlsx")
    Resource exampleFile;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    private final Integer SCHEME_ID = 1;

    @Nested
    class doesSchemeHaveCompletedMandatoryQuestions {

        @Test
        void doesSchemeHaveCompletedMandatoryQuestionsReturnsTrue() throws Exception {
            when(grantMandatoryQuestionService.doesSchemeHaveCompletedMandatoryQuestions(SCHEME_ID)).thenReturn(true);
            mockMvc.perform(get("/mandatory-questions/does-scheme-have-completed-mandatory-questions/" + SCHEME_ID))
                    .andExpect(status().isOk()).andExpect(content().string("true"));
        }

        @Test
        void doesSchemeHaveCompletedMandatoryQuestionsReturnsFalse() throws Exception {
            when(grantMandatoryQuestionService.doesSchemeHaveCompletedMandatoryQuestions(SCHEME_ID)).thenReturn(false);
            mockMvc.perform(get("/mandatory-questions/does-scheme-have-completed-mandatory-questions/" + SCHEME_ID))
                    .andExpect(status().isOk()).andExpect(content().string("false"));
        }

    }

    @Nested
    class exportSpotlightChecks {

        @Test
        void exportSpotlightChecksHappyPathTest() throws Exception {
            doReturn("test_file_name").when(grantMandatoryQuestionService).generateExportFileName(SCHEME_ID);
            final byte[] data = exampleFile.getInputStream().readAllBytes();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(data);
            when(grantMandatoryQuestionService.exportSpotlightChecks(SCHEME_ID)).thenReturn(outputStream);

            mockMvc.perform(get("/mandatory-questions/spotlight-export/" + SCHEME_ID)).andExpect(status().isOk())
                    .andExpect(
                            header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test_file_name\""))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, EXPORT_CONTENT_TYPE))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length)))
                    .andExpect(content().bytes(data));
        }

        @Test
        void exportSpotlightChecksWrongAdminTest() throws Exception {
            when(grantMandatoryQuestionService.exportSpotlightChecks(SCHEME_ID))
                    .thenThrow(new AccessDeniedException("Admin 1 is unable to access application with id 1"));

            mockMvc.perform(get("/mandatory-questions/spotlight-export/" + SCHEME_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        void exportSpotlightChecksGenericErrorTest() throws Exception {
            when(grantMandatoryQuestionService.exportSpotlightChecks(SCHEME_ID)).thenThrow(new RuntimeException());

            mockMvc.perform(get("/mandatory-questions/spotlight-export/" + SCHEME_ID))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void createTemporaryFileThrowsError() throws Exception {
            doReturn(null).when(grantMandatoryQuestionService).generateExportFileName(1);
            final byte[] data = exampleFile.getInputStream().readAllBytes();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(data);
            when(grantMandatoryQuestionService.exportSpotlightChecks(SCHEME_ID)).thenReturn(outputStream);

            mockMvc.perform(get("/mandatory-questions/spotlight-export/" + SCHEME_ID))
                    .andExpect(status().isInternalServerError());
        }

    }

}