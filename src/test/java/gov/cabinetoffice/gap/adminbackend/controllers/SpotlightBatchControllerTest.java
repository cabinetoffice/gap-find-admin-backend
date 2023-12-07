package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.SpotlightPublisherInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.GetSpotlightBatchErrorCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.SpotlightBatchDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.SpotlightBatchMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapper;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.FileService;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightBatchService;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotlightBatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SpotlightBatchController.class, ControllerExceptionHandler.class,
        SpotlightPublisherInterceptor.class })
public class SpotlightBatchControllerTest {

    private final String LAMBDA_AUTH_HEADER = "topSecretKey";

    private final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpotlightBatchService mockSpotlightBatchService;

    @MockBean
    private SpotlightSubmissionService mockSpotlightSubmissionService;

    @MockBean
    private AuthorizationHeaderInterceptor mockAuthorizationHeaderInterceptor;

    @MockBean
    private ValidationErrorMapper mockValidationErrorMapper;

    @MockBean
    private SpotlightPublisherInterceptor mockSpotlightPublisherInterceptor;

    @MockBean
    private SpotlightBatchMapper mockSpotlightBatchMapper;

    @MockBean
    private FileService fileService;

    @Nested
    class spotlightBatchWithStatusExist {

        @Test
        void successfullySpotlightBatchWithStatusExist() throws Exception {
            final SpotlightBatchStatus status = SpotlightBatchStatus.QUEUED;
            final String batchSizeLimit = "150";
            final Boolean expectedResult = true;

            when(mockSpotlightBatchService.existsByStatusAndMaxBatchSize(status, Integer.parseInt(batchSizeLimit)))
                    .thenReturn(expectedResult);

            mockMvc.perform(get("/spotlight-batch/status/{status}/exists", status)
                    .param("batchSizeLimit", batchSizeLimit).header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk()).andExpect(content().string("true"));
        }

        @Test
        void badRequestSpotlightBatchWithStatusExist() throws Exception {
            mockMvc.perform(get("/spotlight-batch/status/INVALID_STATUS/exists")).andExpect(status().isBadRequest());
        }

    }

    @Nested
    class retrieveSpotlightBatchWithStatus {

        @Test
        void successfullyRetrieveSpotlightBatchWithStatus() throws Exception {
            final SpotlightBatchStatus status = SpotlightBatchStatus.QUEUED;
            final String batchSizeLimit = "150";
            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(uuid).build();
            final SpotlightBatchDto expectedResult = SpotlightBatchDto.builder().id(uuid).build();

            when(mockSpotlightBatchService.getSpotlightBatchWithStatus(status, Integer.parseInt(batchSizeLimit)))
                    .thenReturn(spotlightBatch);
            when(mockSpotlightBatchMapper.spotlightBatchToGetSpotlightBatchDto(spotlightBatch))
                    .thenReturn(expectedResult);

            mockMvc.perform(get("/spotlight-batch/status/{status}", status).param("batchSizeLimit", batchSizeLimit)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());
        }

        @Test
        void notFoundRetrieveSpotlightBatchWithStatus() throws Exception {
            final SpotlightBatchStatus status = SpotlightBatchStatus.QUEUED;
            final String batchSizeLimit = "150";

            when(mockSpotlightBatchService.getSpotlightBatchWithStatus(status, Integer.parseInt(batchSizeLimit)))
                    .thenThrow(NotFoundException.class);

            mockMvc.perform(get("/spotlight-batch/status/{status}", status).param("batchSizeLimit", batchSizeLimit)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isNotFound());
        }

        @Test
        void badRequestRetrieveSpotlightBatchWithStatus() throws Exception {
            mockMvc.perform(get("/spotlight-batch/status/INVALID_STATUS")).andExpect(status().isBadRequest());
        }

    }

    @Nested
    class createSpotlightBatch {

        @Test
        void successfullyCreateSpotlightBatch() throws Exception {
            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(uuid).build();
            final SpotlightBatchDto expectedResult = SpotlightBatchDto.builder().id(uuid).build();

            when(mockSpotlightBatchService.createSpotlightBatch()).thenReturn(spotlightBatch);
            when(mockSpotlightBatchMapper.spotlightBatchToGetSpotlightBatchDto(spotlightBatch))
                    .thenReturn(expectedResult);

            mockMvc.perform(post("/spotlight-batch").header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.id").exists());
        }

        @Test
        void badRequestCreateSpotlightBatch() throws Exception {
            mockMvc.perform(post("/spotlight-batch/INVALID_PATH")).andExpect(status().isNotFound());
        }

    }

    @Nested
    class addSpotlightSubmissionToSpotlightBatch {

        @Test
        void successfullyAddSpotlightSubmissionToSpotlightBatch() throws Exception {
            final UUID spotlightBatchId = UUID.randomUUID();
            final UUID spotlightSubmissionId = UUID.randomUUID();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                    .build();
            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(spotlightBatchId).build();
            final SpotlightBatch spotlightBatchWithSpotlightSubmission = SpotlightBatch.builder().id(spotlightBatchId)
                    .spotlightSubmissions(List.of(spotlightSubmission)).build();
            final SpotlightBatchDto expectedResult = SpotlightBatchDto.builder().id(spotlightBatchId).build();

            when(mockSpotlightSubmissionService.getSpotlightSubmissionById(spotlightSubmissionId))
                    .thenReturn(Optional.of(spotlightSubmission));
            when(mockSpotlightBatchService.addSpotlightSubmissionToSpotlightBatch(spotlightSubmission,
                    spotlightBatchId)).thenReturn(spotlightBatch);

            when(mockSpotlightBatchService.getSpotlightBatchById(spotlightBatchId))
                    .thenReturn(spotlightBatchWithSpotlightSubmission);
            when(mockSpotlightBatchMapper.spotlightBatchToGetSpotlightBatchDto(spotlightBatchWithSpotlightSubmission))
                    .thenReturn(expectedResult);

            mockMvc.perform(
                    patch("/spotlight-batch/{spotlightBatchId}/add-spotlight-submission/{spotlightSubmissionId}",
                            spotlightBatchId, spotlightSubmissionId).header(HttpHeaders.AUTHORIZATION,
                                    LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("id\":\"" + spotlightBatchId.toString())));

        }

        @Test
        void notFoundAddSpotlightSubmissionToSpotlightBatch() throws Exception {
            final UUID spotlightBatchId = UUID.randomUUID();
            final UUID spotlightSubmissionId = UUID.randomUUID();

            when(mockSpotlightSubmissionService.getSpotlightSubmissionById(spotlightSubmissionId))
                    .thenReturn(Optional.empty());

            mockMvc.perform(
                    patch("/spotlight-batch/{spotlightBatchId}/add-spotlight-submission/{spotlightSubmissionId}",
                            spotlightBatchId, spotlightSubmissionId).header(HttpHeaders.AUTHORIZATION,
                                    LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk());
        }

        @Test
        void badRequestAddSpotlightSubmissionToSpotlightBatch() throws Exception {
            mockMvc.perform(patch("/spotlight-batch/INVALID_PATH/add-spotlight-submission/INVALID_PATH"))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class sendQueuedBatchesAndProcessSpotlightResponse {

        @Test
        void sendQueuedBatchesAndProcessSpotlightResponse_success() throws Exception {
            mockMvc.perform(
                    post("/spotlight-batch/send-to-spotlight").header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk()).andExpect(content().string("Success"));

            verify(mockSpotlightBatchService, times(1)).sendQueuedBatchesToSpotlightAndProcessThem();
        }

    }

    @Nested
    class exportSpotlightValidationErrorFiles {

        @Test
        void exportSpotlightValidationErrorFiles_success() throws Exception {
            final int schemeId = 1;
            final ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(zipStream)) {
                final ZipEntry entry = new ZipEntry("mock_excel_file.xlsx");
                zipOut.putNextEntry(entry);
                zipOut.write("Mock Excel File Content".getBytes());
                zipOut.closeEntry();
            }

            when(mockSpotlightBatchService.getFilteredSpotlightSubmissionsWithValidationErrors(anyInt()))
                    .thenReturn(zipStream);
            when(fileService.createTemporaryFile(zipStream, "spotlight_validation_errors.zip"))
                    .thenReturn(new InputStreamResource(new ByteArrayInputStream(zipStream.toByteArray())));

            mockMvc.perform(get("/spotlight-batch/{schemeId}/spotlight/download-validation-errors", schemeId))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"spotlight_validation_errors.zip\""))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, EXPORT_CONTENT_TYPE))
                    .andExpect(
                            header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipStream.toByteArray().length)))
                    .andExpect(content().bytes(zipStream.toByteArray()));

        }

    }

    @Nested
    class retrieveSpotlightBatchErrors {

        @Test
        void retrieveSpotlightBatchErrors_success() throws Exception {
            final int schemeId = 1;
            final GetSpotlightBatchErrorCountDTO expectedResult = GetSpotlightBatchErrorCountDTO.builder()
                    .errorStatus("ERROR").errorCount(1).errorFound(true).build();

            when(mockSpotlightBatchService.getSpotlightBatchErrorCount(schemeId)).thenReturn(expectedResult);

            mockMvc.perform(get("/spotlight-batch/{schemeId}/spotlight/get-errors", schemeId))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.errorStatus").value("ERROR"))
                    .andExpect(jsonPath("$.errorCount").value(1)).andExpect(jsonPath("$.errorFound").value(true));

        }

    }

}
