package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.LambdasInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.FailedExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.OutstandingExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsListDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.FailedSubmissionExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicantOrganisationProfile;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportBatchService;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportService;
import gov.cabinetoffice.gap.adminbackend.services.SubmissionsService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GrantExportController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(
        classes = { GrantExportController.class, ControllerExceptionHandler.class, LambdasInterceptor.class })
public class GrantExportControllerTest {

    private final String LAMBDA_AUTH_HEADER = "topSecretKey";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrantExportService mockGrantExportService;

    @MockBean
    private GrantExportBatchService mockGrantExportBatchService;

    @MockBean
    private SubmissionsService mockSubmissionsService;

    @MockBean
    @Qualifier("submissionExportAndScheduledPublishingLambdasInterceptor")
    private AuthorizationHeaderInterceptor mockAuthorizationHeaderInterceptor;

    @MockBean
    private LambdasInterceptor mockLambdasInterceptor;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    private final UUID mockExportId = UUID.randomUUID();

    @Nested
    class getOutstandingExportsCount {

        @Test
        void successfullyGetOutstandingExportsCount() throws Exception {
            final Long mockCount = 10L;
            final OutstandingExportCountDTO expectedResponse = new OutstandingExportCountDTO(mockCount);

            when(mockGrantExportService.getOutstandingExportCount(any())).thenReturn(mockCount);

            mockMvc
                .perform(get("/grant-export/" + mockExportId + "/outstandingCount")
                        .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(content().string(HelperUtils.asJsonString(expectedResponse)));
        }

        @Test
        void badRequest_IncorrectPathVariables() throws Exception {
            mockMvc.perform(get("/grant-export/this_isnt_a_uuid/outstandingCount")).andExpect(status().isBadRequest());
        }

        @Test
        void unexpectedErrorOccurred() throws Exception {
            final Long mockCount = 10L;
            final OutstandingExportCountDTO expectedResponse = new OutstandingExportCountDTO(mockCount);

            when(mockGrantExportService.getOutstandingExportCount(any())).thenThrow(RuntimeException.class);

            mockMvc
                .perform(get("/grant-export/" + mockExportId + "/outstandingCount")
                        .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class getCompletedExportRecordsByBatchId {

        @Test
        void successfullyGetsCompletesExportRecords() throws Exception {
            final UUID submissionId = UUID.randomUUID();
            final List<GrantExportDTO> mockGrantExportDtoList = Collections.singletonList(GrantExportDTO.builder()
                .exportBatchId(mockExportId)
                .submissionId(submissionId)
                .applicationId(1)
                .status(GrantExportStatus.COMPLETE)
                .created(Instant.now())
                .createdBy(1)
                .lastUpdated(Instant.now())
                .location("location")
                .emailAddress("test-email@gmail.com")
                .build());
            final GrantExportListDTO mockGrantExportList = GrantExportListDTO.builder()
                .exportBatchId(mockExportId)
                .grantExports(mockGrantExportDtoList)
                .build();

            when(mockGrantExportService.getGrantExportsByIdAndStatus(mockExportId, GrantExportStatus.COMPLETE))
                .thenReturn(mockGrantExportList);

            mockMvc
                .perform(get("/grant-export/" + mockExportId + "/completed")
                        .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(content().string(HelperUtils.asJsonString(mockGrantExportList)));
        }

        @Test
        void exceptionThrown() throws Exception {
            when(mockGrantExportService.getGrantExportsByIdAndStatus(mockExportId, GrantExportStatus.COMPLETE))
                    .thenThrow(RuntimeException.class);
            mockMvc.perform(get("/grant-export/" + mockExportId + "/completed")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class getFailedExportsCount {

        final UUID mockExportId = UUID.randomUUID();
        final Long mockCount = 2L;
        final FailedExportCountDTO expectedResponse = new FailedExportCountDTO(mockCount);

        @Test
        void successfullyGetFailedExportsCount() throws Exception {
            when(mockGrantExportService.getExportCountByStatus(any(), any())).thenReturn(mockCount);

            mockMvc.perform(get("/grant-export/" + mockExportId + "/failedCount")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(expectedResponse)));
        }

        @Test
        void badRequest_IncorrectPathVariables() throws Exception {
            mockMvc.perform(get("/grant-export/this_isnt_a_uuid/failedCount"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void unexpectedErrorOccurred() throws Exception {
            when(mockGrantExportService.getExportCountByStatus(any(), any())).thenThrow(RuntimeException.class);

            mockMvc.perform(get("/grant-export/" + mockExportId + "/failedCount")
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class getRemainingExportsCount {

        final UUID mockExportId = UUID.randomUUID();
        final Long mockCount = 10L;
        final OutstandingExportCountDTO expectedResponse = new OutstandingExportCountDTO(mockCount);

        @Test
        void successfullyGetRemainingExportsCount() throws Exception {
            when(mockGrantExportService.getRemainingExportsCount(any())).thenReturn(mockCount);

            mockMvc.perform(get("/grant-export/" + mockExportId + "/remainingCount")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(expectedResponse)));
        }

        @Test
        void badRequest_IncorrectPathVariables() throws Exception {
            mockMvc.perform(get("/grant-export/this_isnt_a_uuid/remainingCount"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void unexpectedErrorOccurred() throws Exception {
            when(mockGrantExportService.getRemainingExportsCount(any())).thenThrow(RuntimeException.class);

            mockMvc.perform(get("/grant-export/" + mockExportId + "/remainingCount")
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class getExportedSubmissionsListDto {

        final UUID exportId = UUID.fromString("a3f3e3e3-3e3e-3e3e-3e3e-3e3e3e3e3e3e");

        @Test
        void successfullyGetExportedSubmissionsListDto() throws Exception {
            final ExportedSubmissionsListDto expectedResponse = ExportedSubmissionsListDto.builder()
                    .grantExportId(exportId)
                    .superZipFileLocation("superZipLocation")
                    .build();
            when(mockGrantExportBatchService.getSuperZipLocation(any())).thenReturn("superZipLocation");
            when(mockGrantExportService.generateExportedSubmissionsListDto(any(), any(), any(), any()))
                .thenReturn(expectedResponse);


            mockMvc.perform(get("/grant-export/" + mockExportId + "/details"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(expectedResponse)));
        }

        @Test
        void badRequest_IncorrectPathVariables() throws Exception {
            mockMvc.perform(get("/grant-export/this_isnt_a_uuid/details"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void unexpectedErrorOccurred() throws Exception {
            when(mockGrantExportBatchService.getSuperZipLocation(exportId)).thenReturn("superZipLocation");
            when(mockGrantExportService.generateExportedSubmissionsListDto(any(), any(), any(), any()))
                    .thenThrow(RuntimeException.class);

            mockMvc.perform(get("/grant-export/" + mockExportId + "/details"))
                    .andExpect(status().isInternalServerError());
        }
    }
    @Nested
    class updateGrantExportBatchStatus {

        @Test
        void successfullyUpdateGrantExportBatchStatus() throws Exception {
            doNothing().when(mockGrantExportBatchService).updateExportBatchStatusById(mockExportId, GrantExportStatus.COMPLETE);
            mockMvc.perform(patch("/grant-export/" + mockExportId + "/batch/status").contentType(MediaType.APPLICATION_JSON)
                            .content("\"COMPLETE\"").header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isNoContent());
        }

        @Test
        void errorThrown() throws Exception {
            doThrow(new RuntimeException()).when(mockGrantExportBatchService).updateExportBatchStatusById(mockExportId, GrantExportStatus.COMPLETE);

            mockMvc.perform(patch("/grant-export/" + mockExportId + "/batch/status").contentType(MediaType.APPLICATION_JSON)
                            .content("\"COMPLETE\"").header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class updateGrantExportBatchLocation {
        final String s3ObjectKey = "s3ObjectKey";

        @Test
        void successfullyUpdateGrantExportBatchLocation() throws Exception {
            mockMvc.perform(patch("/grant-export/" + mockExportId + "/batch/s3-object-key").contentType(MediaType.APPLICATION_JSON)
                            .content("\"" + s3ObjectKey + "\"")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isNoContent());
        }

        @Test
        void errorThrown() throws Exception {
            doThrow(new RuntimeException()).when(mockGrantExportBatchService).addS3ObjectKeyToGrantExportBatch(mockExportId, s3ObjectKey);

            mockMvc.perform(patch("/grant-export/" + mockExportId + "/batch/s3-object-key").contentType(MediaType.APPLICATION_JSON)
                            .content("\"" + s3ObjectKey + "\"")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class getFailedSubmissionExportById {
        final UUID submissionId = UUID.randomUUID();

        @Test
        void successfullyGetFailedSubmissionExportById() throws Exception {
            final GrantApplicantOrganisationProfile organisationProfile = GrantApplicantOrganisationProfile.builder().legalName("legalName").build();
            final Submission submission = Submission.builder()
                    .id(submissionId)
                    .scheme(SchemeEntity.builder().id(1).name("schemeName").build())
                    .applicant(GrantApplicant.builder().organisationProfile(organisationProfile).build())
                    .applicationName("applicationName")
                    .definition(SubmissionDefinition.builder().sections(Collections.emptyList()).build())
                    .build();
            final GrantExportEntity grantExport = GrantExportEntity.builder().location("location").build();
            final FailedSubmissionExportDTO failedSubmissionExportDTO = FailedSubmissionExportDTO.builder()
                    .submissionId(submissionId)
                    .schemeId(1)
                    .schemeName("schemeName")
                    .legalName("legalName")
                    .applicationName("applicationName")
                    .sections(Collections.emptyList())
                    .attachmentsZipLocation("location")
                    .build();

            when(mockSubmissionsService.getSubmissionById(submissionId)).thenReturn(submission);
            when(mockGrantExportService.getGrantExportById(mockExportId, submissionId)).thenReturn(grantExport);

            mockMvc.perform(get("/grant-export/" + mockExportId + "/submissions/" + submissionId + "/details"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(failedSubmissionExportDTO)));
        }

        @Test
        void notFoundExceptionThrown() throws Exception {
            doThrow(new NotFoundException()).when(mockSubmissionsService).getSubmissionById(submissionId);
            mockMvc.perform(get("/grant-export/" + mockExportId + "/submissions/" + submissionId + "/details"))
                    .andExpect(status().isNotFound());
        }

    }

}