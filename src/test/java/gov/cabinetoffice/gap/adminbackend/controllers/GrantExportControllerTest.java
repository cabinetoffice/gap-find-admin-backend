package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.LambdasInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.OutstandingExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @Qualifier("submissionExportAndScheduledPublishingLambdasInterceptor")
    private AuthorizationHeaderInterceptor mockAuthorizationHeaderInterceptor;

    @MockBean
    private LambdasInterceptor mockLambdasInterceptor;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Nested
    class getOutstandingExportsCount {

        @Test
        void successfullyGetOutstandingExportsCount() throws Exception {
            final UUID mockExportId = UUID.randomUUID();
            final Long mockCount = 10L;
            final OutstandingExportCountDTO expectedResponse = new OutstandingExportCountDTO(mockCount);

            when(mockGrantExportService.getOutstandingExportCount(any())).thenReturn(mockCount);

            mockMvc.perform(get("/export-batch/" + mockExportId + "/outstandingCount").header(HttpHeaders.AUTHORIZATION,
                    LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(expectedResponse)));
        }

        @Test
        void badRequest_IncorrectPathVariables() throws Exception {
            mockMvc.perform(get("/export-batch/this_isnt_a_uuid/outstandingCount")).andExpect(status().isBadRequest());
        }

        @Test
        void unexpectedErrorOccurred() throws Exception {
            final UUID mockExportId = UUID.randomUUID();
            final Long mockCount = 10L;
            final OutstandingExportCountDTO expectedResponse = new OutstandingExportCountDTO(mockCount);

            when(mockGrantExportService.getOutstandingExportCount(any())).thenThrow(RuntimeException.class);

            mockMvc.perform(get("/export-batch/" + mockExportId + "/outstandingCount").header(HttpHeaders.AUTHORIZATION,
                    LAMBDA_AUTH_HEADER)).andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class getCompletedExportRecordsByBatchId {

        @Test
        void successfullyGetsCompletesExportRecords() throws Exception {
            final GrantExportId id = GrantExportId.builder()
                    .exportBatchId(UUID.randomUUID())
                    .submissionId(UUID.randomUUID())
                    .build();
            final List<GrantExportDTO> mockGrantExportDtoList = Collections.singletonList(GrantExportDTO.builder()
                    .exportBatchId(id.getExportBatchId())
                    .submissionId(id.getSubmissionId())
                    .applicationId(1)
                    .status(GrantExportStatus.COMPLETE)
                    .created(Instant.now())
                    .createdBy(1)
                    .lastUpdated(Instant.now())
                    .location("location")
                    .emailAddress("test-email@gmail.com").build());
            final GrantExportListDTO mockGrantExportList = GrantExportListDTO.builder()
                    .exportBatchId(id.getExportBatchId())
                    .grantExports(mockGrantExportDtoList)
                    .build();

            when(mockGrantExportService.getGrantExportsByIdAndStatus(id.getExportBatchId(),  GrantExportStatus.COMPLETE))
                    .thenReturn(mockGrantExportList);

            mockMvc.perform(get("/export-batch/" + id.getExportBatchId() + "/completed").header(HttpHeaders.AUTHORIZATION,
                            LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(mockGrantExportList)));
        }

    }

}
