package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.constants.SpotlightExports;
import gov.cabinetoffice.gap.adminbackend.dtos.S3ObjectKeyDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.UrlDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.LambdaSubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionExportsDTO;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.*;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSubmissionGenerator;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubmissionsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SubmissionsController.class, ControllerExceptionHandler.class })
class SubmissionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubmissionsService submissionsService;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private ApplicationFormService applicationFormService;

    @MockBean
    private SchemeService schemeService;

    @MockBean
    private SecretAuthService secretAuthService;

    @MockBean
    private FileService fileService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Value("classpath:spotlight/XLSX_Spotlight_Template.xlsx")
    Resource exampleFile;

    private final String LAMBDA_AUTH_HEADER = "topSecretKey";

    @Nested
    class exportSpotlightChecks {

        @Test
        void exportSpotlightChecksHappyPathTest() throws Exception {
            final ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(zipStream)) {
                final ZipEntry entry = new ZipEntry("mock_excel_file.xlsx");
                zipOut.putNextEntry(entry);
                zipOut.write("Mock Excel File Content".getBytes());
                zipOut.closeEntry();
            }

            when(fileService.createTemporaryFile(zipStream, SpotlightExports.REQUIRED_CHECKS_FILENAME))
                    .thenReturn(new InputStreamResource(new ByteArrayInputStream(zipStream.toByteArray()))

                    );
            when(submissionsService.exportSpotlightChecks(1)).thenReturn(zipStream);
            doNothing().when(submissionsService).updateSubmissionLastRequiredChecksExport(1);

            mockMvc.perform(get("/submissions/spotlight-export/" + 1)).andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"required_checks.zip\""))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, EXPORT_CONTENT_TYPE))
                    .andExpect(
                            header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipStream.toByteArray().length)))
                    .andExpect(content().bytes(zipStream.toByteArray()));
        }

        @Test
        void exportSpotlightChecksApplicationNotFoundTest() throws Exception {
            when(submissionsService.exportSpotlightChecks(1))
                    .thenThrow(new ApplicationFormException("Application form not found with id 1"));

            mockMvc.perform(get("/submissions/spotlight-export/" + 1)).andExpect(status().isInternalServerError());
        }

        @Test
        void exportSpotlightChecksWrongAdminTest() throws Exception {
            when(submissionsService.exportSpotlightChecks(1))
                    .thenThrow(new AccessDeniedException("Admin 1 is unable to access application with id 1"));

            mockMvc.perform(get("/submissions/spotlight-export/" + 1)).andExpect(status().isForbidden());
        }

        @Test
        void exportSpotlightChecksGenericErrorTest() throws Exception {
            when(submissionsService.exportSpotlightChecks(1)).thenThrow(new RuntimeException());

            mockMvc.perform(get("/submissions/spotlight-export/" + 1)).andExpect(status().isInternalServerError());
        }

        @Test
        void exportSpotlightChecksUpdateApplicationSpotlightExportFailsTest() throws Exception {
            final byte[] data = exampleFile.getInputStream().readAllBytes();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(data);
            when(submissionsService.exportSpotlightChecks(1)).thenReturn(outputStream);
            when(submissionsService.generateExportFileName(1, 1)).thenReturn("test_file_name");
            doThrow(new RuntimeException("forced service error")).when(submissionsService)
                    .updateSubmissionLastRequiredChecksExport(1);

            mockMvc.perform(get("/submissions/spotlight-export/" + 1)).andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class exportAllSubmissions {

        @Test
        void exportAllSubmissions_HappyPathTest() throws Exception {
            doNothing().when(submissionsService).triggerSubmissionsExport(1);

            mockMvc.perform(post("/submissions/export-all/" + 1)).andExpect(status().isOk());
        }

        @Test
        void exportAllSubmissions_GenericErrorTest() throws Exception {
            doThrow(new RuntimeException("Error message")).when(submissionsService).triggerSubmissionsExport(1);

            mockMvc.perform(post("/submissions/export-all/" + 1)).andExpect(status().isInternalServerError());
        }

        @Test
        void exportAllSubmissions_BadRequestTest() throws Exception {
            mockMvc.perform(post("/submissions/export-all/string")).andExpect(status().isBadRequest());
        }

        @Test
        void exportAllSubmissions_NoPathVariableTest() throws Exception {
            mockMvc.perform(post("/submissions/export-all")).andExpect(status().isNotFound());
        }

    }

    @Nested
    class getExportStatus {

        @Test
        void getExportStatus_HappyPath() throws Exception {
            when(submissionsService.getExportStatus(1)).thenReturn(GrantExportStatus.COMPLETE);

            mockMvc.perform(get("/submissions/status/1")).andExpect(status().isOk())
                    .andExpect(content().string(GrantExportStatus.COMPLETE.toString()));
        }

    }

    @Nested
    class getSubmissionInfo {

        @BeforeEach
        void beforeEach() {
            doNothing().when(secretAuthService).authenticateSecret(LAMBDA_AUTH_HEADER);
        }

        @Test
        void happyPath() throws Exception {
            final LambdaSubmissionDefinition lambdaSubmissionDefinition = LambdaSubmissionDefinition.builder().build();
            when(submissionsService.getSubmissionInfo(any(UUID.class), any(UUID.class), anyString()))
                    .thenReturn(lambdaSubmissionDefinition);

            mockMvc.perform(
                    get("/submissions/" + UUID.randomUUID() + "/export-batch/" + UUID.randomUUID() + "/submission")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonStringWithNulls(lambdaSubmissionDefinition)));
        }

        @Test
        void unauthorisedPath() throws Exception {
            when(submissionsService.getSubmissionInfo(any(UUID.class), any(UUID.class), anyString()))
                    .thenThrow(new UnauthorizedException());

            mockMvc.perform(
                    get("/submissions/" + UUID.randomUUID() + "/export-batch/" + UUID.randomUUID() + "/submission")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().is(401));
        }

        @Test
        void resourceNotFoundPath() throws Exception {
            when(submissionsService.getSubmissionInfo(any(UUID.class), any(UUID.class), anyString()))
                    .thenThrow(new NotFoundException());

            mockMvc.perform(
                    get("/submissions/" + UUID.randomUUID() + "/export-batch/" + UUID.randomUUID() + "/submission")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    class getCompletedSubmissionsExport {

        @Test
        void getCompletedSubmissionExports_FindsCompletedSubmissionExports() throws Exception {
            UUID testUUID = UUID.randomUUID();
            SubmissionExportsDTO mockSubmissionExportsResponse = RandomSubmissionGenerator.randomSubmissionDTOBuilder()
                    .build();
            List<SubmissionExportsDTO> mockListResponse = Collections.singletonList(mockSubmissionExportsResponse);

            when(submissionsService.getCompletedSubmissionExportsForBatch(testUUID)).thenReturn(mockListResponse);

            mockMvc.perform(get("/submissions/exports/" + testUUID)).andExpect(status().isOk())
                    .andExpect(content().json(HelperUtils.asJsonString(mockListResponse)));
        }

        @Test
        void getCompletedSubmissionExports_NoCompletedSubmissionExportsFound() throws Exception {
            UUID testUUID = UUID.randomUUID();
            when(submissionsService.getCompletedSubmissionExportsForBatch(testUUID))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/submissions/exports/" + testUUID)).andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void getCompletedSubmissionExports_UnexpectedErrorOccurred() throws Exception {
            doThrow(new RuntimeException()).when(submissionsService).getCompletedSubmissionExportsForBatch(any());

            mockMvc.perform(get("/submissions/exports/" + UUID.randomUUID()))
                    .andExpect(status().isInternalServerError());

        }

    }

    @Nested
    class updateExportRecordStatus {

        @Test
        void updateExportRecordStatus_SuccessfulUpdate() throws Exception {
            doNothing().when(submissionsService).updateExportStatus("1234", "5678", GrantExportStatus.COMPLETE);

            mockMvc.perform(post("/submissions/1234/export-batch/5678/status").contentType(MediaType.APPLICATION_JSON)
                    .content("\"COMPLETE\"").header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isNoContent());
        }

        @Test
        void updateExportRecordStatus_UnexpectedErrorOccurred() throws Exception {
            doThrow(new RuntimeException()).when(submissionsService).updateExportStatus(any(), any(), any());

            mockMvc.perform(post("/submissions/1234/export-batch/5678/status").contentType(MediaType.APPLICATION_JSON)
                    .content("\"COMPLETE\"").header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void updateExportRecordLocation_SuccessfullyUpdate() throws Exception {
            S3ObjectKeyDTO mockRequest = new S3ObjectKeyDTO("link_to_aws.com/path/filename.zip");

            doNothing().when(submissionsService).addS3ObjectKeyToSubmissionExport(any(), any(), anyString());

            MvcResult res = mockMvc.perform(
                    patch("/submissions/" + UUID.randomUUID() + "/export-batch/" + UUID.randomUUID() + "/s3-object-key")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(mockRequest))
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isNoContent()).andReturn();

            System.out.println(res);
        }

    }

    @Nested
    class getPresignedUrl {

        void getPresignedUrl_HappyPath() throws Exception {
            doReturn("www.fakeamazon.com/test_file_name").when(s3Service)
                    .generateExportDocSignedUrl("path/filename.zip");
            S3ObjectKeyDTO mockRequest = new S3ObjectKeyDTO("path/filename.zip");
            UrlDTO mockResponse = new UrlDTO("www.fakeamazon.com/test_file_name");

            mockMvc.perform(post("/submissions/signed-url").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonString(mockRequest))).andExpect(status().isOk())
                    .andExpect(content().json(HelperUtils.asJsonString(mockResponse)));
        }

        void getPresignedUrl_BadRequest_Body() throws Exception {
            UrlDTO mockRequest = new UrlDTO("www.doesntmatter.com");
            mockMvc.perform(post("/submissions/signed-url").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonString(mockRequest))).andExpect(status().isBadRequest());
        }

    }

    @Nested
    class updateExportRecordLocation {

        @BeforeEach
        void beforeEach() {
            doNothing().when(secretAuthService).authenticateSecret(LAMBDA_AUTH_HEADER);
        }

        @Test
        void updateExportRecordLocation_BadRequest_PathVariables() throws Exception {
            S3ObjectKeyDTO mockRequest = new S3ObjectKeyDTO("path/filename.zip");

            mockMvc.perform(patch("/submissions/1234/export-batch/12345/s3-object-key")
                    .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(mockRequest))
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isBadRequest());
        }

        @Test
        void updateExportRecordLocation_BadRequest_RequestBody() throws Exception {
            mockMvc.perform(patch("/submissions/1234/export-batch/12345/s3-object-key")
                    .contentType(MediaType.APPLICATION_JSON).content("\"link_to_aws.com/path/filename.zip\"")
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isBadRequest());
        }

        @Test
        void updateExportRecordLocation_UnexpectedErrorOccurs() throws Exception {
            S3ObjectKeyDTO mockRequest = new S3ObjectKeyDTO("path/filename.zip");

            doThrow(new RuntimeException()).when(submissionsService).addS3ObjectKeyToSubmissionExport(any(), any(),
                    anyString());

            mockMvc.perform(
                    patch("/submissions/" + UUID.randomUUID() + "/export-batch/" + UUID.randomUUID() + "/s3-object-key")
                            .contentType(MediaType.APPLICATION_JSON).content(HelperUtils.asJsonString(mockRequest))
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

}