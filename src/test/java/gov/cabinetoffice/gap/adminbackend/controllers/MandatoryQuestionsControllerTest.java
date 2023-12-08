package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.FileService;
import gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;
import static org.mockito.Mockito.*;
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

    @MockBean
    private FileService fileService;

    @Value("classpath:spotlight/XLSX_Spotlight_Template.xlsx")
    Resource exampleFile;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    private final Integer SCHEME_ID = 1;

    @Nested
    class hasCompletedMandatoryQuestions {

        @Test
        void hasCompletedMandatoryQuestionsReturnsTrue() throws Exception {
            when(grantMandatoryQuestionService.hasCompletedMandatoryQuestions(SCHEME_ID, true)).thenReturn(true);
            mockMvc.perform(get("/mandatory-questions/scheme/" + SCHEME_ID + "/isCompleted?isInternal=true"))
                    .andExpect(status().isOk()).andExpect(content().string("true"));
        }

        @Test
        void hasCompletedMandatoryQuestionsReturnsFalse() throws Exception {
            when(grantMandatoryQuestionService.hasCompletedMandatoryQuestions(SCHEME_ID, false)).thenReturn(false);
            mockMvc.perform(get("/mandatory-questions/scheme/" + SCHEME_ID + "/isCompleted?isInternal=false"))
                    .andExpect(status().isOk()).andExpect(content().string("false"));
        }

    }

    @Nested
    class exportDueDiligenceData {

        @Test
        void exportDueDiligenceDataHappyPathTest() throws Exception {
            doReturn("test_file_name").when(grantMandatoryQuestionService).generateExportFileName(SCHEME_ID, null);
            final byte[] data = exampleFile.getInputStream().readAllBytes();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(data);
            final InputStreamResource inputStream = new InputStreamResource(
                    new ByteArrayInputStream(outputStream.toByteArray()));

            when(fileService.createTemporaryFile(outputStream, "test_file_name")).thenReturn(inputStream);
            when(grantMandatoryQuestionService.getDueDiligenceData(SCHEME_ID, true)).thenReturn(outputStream);

            mockMvc.perform(get("/mandatory-questions/due-diligence/" + SCHEME_ID + "?internal=true"))
                    .andExpect(status().isOk())
                    .andExpect(
                            header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test_file_name\""))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, EXPORT_CONTENT_TYPE))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length)))
                    .andExpect(content().bytes(data));
        }

        @Test
        void exportDueDiligenceDataWrongAdminTest() throws Exception {
            when(grantMandatoryQuestionService.getDueDiligenceData(SCHEME_ID, false)).thenThrow(
                    new AccessDeniedException("Admin 1 is unable to access mandatory questions with scheme id 1"));

            mockMvc.perform(get("/mandatory-questions/due-diligence/" + SCHEME_ID + "?internal=false"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void exportDueDiligenceDataGenericErrorTest() throws Exception {
            when(grantMandatoryQuestionService.getDueDiligenceData(SCHEME_ID, false)).thenThrow(new RuntimeException());

            mockMvc.perform(get("/mandatory-questions/due-diligence/" + SCHEME_ID + "?internal=false"))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class exportSpotlightChecks {

        @Test
        void exportSpotlightChecks() throws Exception {
            final ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(zipStream)) {
                final ZipEntry entry = new ZipEntry("mock_excel_file.xlsx");
                zipOut.putNextEntry(entry);
                zipOut.write("Mock Excel File Content".getBytes());
                zipOut.closeEntry();
            }

            when(grantMandatoryQuestionService.getSpotlightChecks(anyInt())).thenReturn(zipStream);
            when(fileService.createTemporaryFile(zipStream, "spotlight_checks.zip"))
                    .thenReturn(new InputStreamResource(new ByteArrayInputStream(zipStream.toByteArray())));

            mockMvc.perform(get("/mandatory-questions/spotlight-export/" + SCHEME_ID)).andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"spotlight_checks.zip\""))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, EXPORT_CONTENT_TYPE))
                    .andExpect(
                            header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipStream.toByteArray().length)))
                    .andExpect(content().bytes(zipStream.toByteArray()));
        }

        @Test
        void exportSpotlightChecksDataWrongAdminTest() throws Exception {
            when(grantMandatoryQuestionService.getSpotlightChecks(SCHEME_ID)).thenThrow(
                    new AccessDeniedException("Admin 1 is unable to access mandatory questions with scheme id 1"));

            mockMvc.perform(get("/mandatory-questions/spotlight-export/" + SCHEME_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        void exportSpotlightChecksGenericErrorTest() throws Exception {
            when(grantMandatoryQuestionService.getSpotlightChecks(SCHEME_ID)).thenThrow(new RuntimeException());

            mockMvc.perform(get("/mandatory-questions/spotlight-export/" + SCHEME_ID))
                    .andExpect(status().isInternalServerError());
        }

    }

}