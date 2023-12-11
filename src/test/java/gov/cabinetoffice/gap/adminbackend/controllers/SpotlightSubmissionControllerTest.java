package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.SpotlightPublisherInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightSubmissionDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.SpotlightSubmissionMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapper;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.FileService;
import gov.cabinetoffice.gap.adminbackend.services.SchemeService;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchemeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SpotlightSubmissionController.class, ControllerExceptionHandler.class,
        SpotlightPublisherInterceptor.class })
class SpotlightSubmissionControllerTest {

    private final String LAMBDA_AUTH_HEADER = "topSecretKey";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpotlightSubmissionService mockSpotlightSubmissionService;

    @MockBean
    private AuthorizationHeaderInterceptor mockAuthorizationHeaderInterceptor;

    @MockBean
    private ValidationErrorMapper mockValidationErrorMapper;

    @MockBean
    private SpotlightPublisherInterceptor mockSpotlightPublisherInterceptor;

    @MockBean
    private SpotlightSubmissionMapper spotlightSubmissionMapper;

    @MockBean
    private SchemeService schemeService;

    @MockBean
    private FileService fileService;

    private final Integer SCHEME_ID = 1;

    private final String DATE = "25 September 2023";

    @Nested
    class getSpotlightSubmissionById {

        @Test
        void successfullyRetrieveSpotlightSubmission() throws Exception {
            final UUID spotlightSubmissionId = UUID.randomUUID();
            final Instant now = Instant.now();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                    .created(now).build();
            final SpotlightSubmissionDto expectedResult = SpotlightSubmissionDto.builder().id(spotlightSubmissionId)
                    .created(now).build();

            when(mockSpotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId))
                    .thenReturn(spotlightSubmission);
            when(spotlightSubmissionMapper.spotlightSubmissionToSpotlightSubmissionDto(spotlightSubmission))
                    .thenReturn(expectedResult);

            mockMvc.perform(get("/spotlight-submissions/{spotlightSubmissionId}", spotlightSubmissionId)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());
        }

        @Test
        void notFoundAddSpotlightSubmissionToSpotlightBatch() throws Exception {

            final UUID spotlightSubmissionId = UUID.randomUUID();

            when(mockSpotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId))
                    .thenThrow(NotFoundException.class);

            mockMvc.perform(get("/spotlight-submissions/{spotlightSubmissionId}", spotlightSubmissionId)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isNotFound());
        }

    }

    @Nested
    class getSpotlightSubmissionDataBySchemeId {

        @Test
        void noSpotlightSubmissionsFound() throws Exception {
            final boolean hasSpotlightSubmissions = false;

            when(mockSpotlightSubmissionService.doesSchemeHaveSpotlightSubmission(SCHEME_ID))
                    .thenReturn(hasSpotlightSubmissions);

            mockMvc.perform(get("/spotlight-submissions/scheme/{schemeId}/get-due-diligence-data", SCHEME_ID)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasSpotlightSubmissions").value(hasSpotlightSubmissions));

            verify(mockSpotlightSubmissionService, times(1)).doesSchemeHaveSpotlightSubmission(SCHEME_ID);
            verify(mockSpotlightSubmissionService, times(0)).getCountBySchemeIdAndStatus(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
            verify(mockSpotlightSubmissionService, times(0)).getLastSubmissionDate(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
        }

        @Test
        void spotlightSubmissionsFound() throws Exception {
            final boolean hasSpotlightSubmissions = true;
            final Long count = 2L;
            final String date = "25 September 2023";

            when(mockSpotlightSubmissionService.doesSchemeHaveSpotlightSubmission(SCHEME_ID))
                    .thenReturn(hasSpotlightSubmissions);
            when(mockSpotlightSubmissionService.getCountBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                    .thenReturn(count);
            when(mockSpotlightSubmissionService.getLastSubmissionDate(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                    .thenReturn(date);

            mockMvc.perform(get("/spotlight-submissions/scheme/{schemeId}/get-due-diligence-data", SCHEME_ID)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasSpotlightSubmissions").value(hasSpotlightSubmissions))
                    .andExpect(jsonPath("$.sentCount").value(count))
                    .andExpect(jsonPath("$.sentLastUpdatedDate").value(date));

            verify(mockSpotlightSubmissionService, times(1)).doesSchemeHaveSpotlightSubmission(SCHEME_ID);
            verify(mockSpotlightSubmissionService, times(1)).getCountBySchemeIdAndStatus(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
            verify(mockSpotlightSubmissionService, times(1)).getLastSubmissionDate(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
        }

    }

    @Nested
    class downloadDueDiligenceChecks {

        @Test
        void successfullyDownloadDueDiligenceChecks() throws Exception {
            final ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(zipStream)) {
                final ZipEntry entry = new ZipEntry("mock_excel_file.xlsx");
                zipOut.putNextEntry(entry);
                zipOut.write("Mock Excel File Content".getBytes());
                zipOut.closeEntry();
            }
            final String exportFileName = "spotlight_checks.zip";
            final SchemeDTO scheme = SchemeDTO.builder().schemeId(SCHEME_ID).build();

            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenReturn(scheme);
            when(mockSpotlightSubmissionService.generateCharitiesAndLimitedCompanyDownloadFile(scheme))
                    .thenReturn(zipStream);

            when(fileService.createTemporaryFile(zipStream, exportFileName))
                    .thenReturn(new InputStreamResource(new ByteArrayInputStream(zipStream.toByteArray())));

            mockMvc.perform(get("/spotlight-submissions/scheme/{schemeId}/download", SCHEME_ID)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"spotlight_checks.zip\""))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, EXPORT_CONTENT_TYPE))
                    .andExpect(
                            header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipStream.toByteArray().length)))
                    .andExpect(content().bytes(zipStream.toByteArray()));

            verify(mockSpotlightSubmissionService, times(1)).generateCharitiesAndLimitedCompanyDownloadFile(scheme);
        }

    }

}