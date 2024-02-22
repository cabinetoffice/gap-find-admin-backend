package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.LambdasInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportBatchDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportBatchEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.GrantExportMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportBatchService;
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
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GrantExportBatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(
        classes = {GrantExportBatchController.class, GrantExportMapper.class, ControllerExceptionHandler.class, LambdasInterceptor.class})
public class GrantExportBatchControllerTest {

    private final String LAMBDA_AUTH_HEADER = "topSecretKey";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrantExportBatchService grantExportBatchService;

    @MockBean
    @Qualifier("submissionExportAndScheduledPublishingLambdasInterceptor")
    private AuthorizationHeaderInterceptor mockAuthorizationHeaderInterceptor;

    @MockBean
    private LambdasInterceptor mockLambdasInterceptor;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @MockBean
    private GrantExportMapper grantExportMapper;

    final UUID mockExportId = UUID.randomUUID();

    @Nested
    class updateGrantExportBatchStatus {

        @Test
        void successfullyUpdateGrantExportBatchStatus() throws Exception {
            doNothing().when(grantExportBatchService).updateExportBatchStatusById(mockExportId, GrantExportStatus.COMPLETE);
            mockMvc.perform(patch("/grant-export-batch/" + mockExportId + "/status").contentType(MediaType.APPLICATION_JSON)
                            .content("\"COMPLETE\"").header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isNoContent());
        }

        @Test
        void errorThrown() throws Exception {
            doThrow(new RuntimeException()).when(grantExportBatchService).updateExportBatchStatusById(mockExportId, GrantExportStatus.COMPLETE);

            mockMvc.perform(patch("/grant-export-batch/" + mockExportId + "/status").contentType(MediaType.APPLICATION_JSON)
                            .content("\"COMPLETE\"").header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class updateGrantExportBatchLocation {
        final String s3ObjectKey = "s3ObjectKey";

        @Test
        void successfullyUpdateGrantExportBatchLocation() throws Exception {
            mockMvc.perform(patch("/grant-export-batch/" + mockExportId + "/s3-object-key").contentType(MediaType.APPLICATION_JSON)
                            .content("\"" + s3ObjectKey + "\"")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isNoContent());
        }

        @Test
        void errorThrown() throws Exception {
            doThrow(new RuntimeException()).when(grantExportBatchService).addS3ObjectKeyToGrantExportBatch(mockExportId, s3ObjectKey);

            mockMvc.perform(patch("/grant-export-batch/" + mockExportId + "/s3-object-key").contentType(MediaType.APPLICATION_JSON)
                            .content("\"" + s3ObjectKey + "\"")
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

    @Nested
    class getExportBatchInfo {

        @Test
        void successfullyGetsBatchInfo() throws Exception {
            UUID randomUUID = UUID.randomUUID();
            final GrantExportBatchEntity grantExportBatchEntity = GrantExportBatchEntity.builder()
                    .id(randomUUID)
                    .applicationId(1)
                    .createdBy(1)
                    .build();
            final GrantExportBatchDTO grantExportBatchDTO = GrantExportBatchDTO.builder()
                    .exportBatchId(randomUUID)
                    .applicationId(1)
                    .createdBy(1)
                    .status(GrantExportStatus.COMPLETE)
                    .emailAddress("test123@test.com")
                    .created(Instant.now())
                    .lastUpdated(Instant.now())
                    .location("here")
                    .build();

            when(grantExportBatchService.getGrantExportBatch(mockExportId))
                    .thenReturn(grantExportBatchEntity);
            when(grantExportMapper.grantExportBatchEntityToGrantExportBatchDTO(grantExportBatchEntity)).thenReturn(grantExportBatchDTO);

            mockMvc.perform(get("/grant-export-batch/" + mockExportId).header(HttpHeaders.AUTHORIZATION,
                            LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(content().string(HelperUtils.asJsonString(grantExportBatchDTO)));
        }

        @Test
        void exceptionThrown() throws Exception {
            when(grantExportBatchService.getGrantExportBatch(mockExportId))
                    .thenThrow(RuntimeException.class);
            mockMvc.perform(get("/grant-export-batch/" + mockExportId)
                            .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isInternalServerError());
        }

    }

}
