package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.LambdasInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.GetSpotlightBatchErrorCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.SpotlightBatchDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.SpotlightBatchMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapper;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightBatchService;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpotlightBatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(
        classes = { SpotlightBatchController.class, ControllerExceptionHandler.class, LambdasInterceptor.class })
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
    @Qualifier("spotlightPublisherLambdaInterceptor")
    private AuthorizationHeaderInterceptor mockAuthorizationHeaderInterceptor;

    @MockBean
    private ValidationErrorMapper mockValidationErrorMapper;

    @MockBean
    private LambdasInterceptor mockLambdasInterceptor;

    @MockBean
    private SpotlightBatchMapper mockSpotlightBatchMapper;

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
    class getSpotlightBatchById {

        @Test
        void successfullyGetSpotlightBatchById() throws Exception {
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
        void notFoundExceptionGetSpotlightBatchById() throws Exception {
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

            when(mockSpotlightBatchService.getMostRecentSpotlightBatchWithStatus(status,
                    Integer.parseInt(batchSizeLimit))).thenReturn(spotlightBatch);
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

            when(mockSpotlightBatchService.getMostRecentSpotlightBatchWithStatus(status,
                    Integer.parseInt(batchSizeLimit))).thenThrow(NotFoundException.class);

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
                    .andExpect(content().string(containsString("id\":\"" + spotlightBatchId)));

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
    class retrieveSpotlightBatchErrors {

        @Test
        void retrieveSpotlightBatchErrors_success() throws Exception {
            final int schemeId = 1;
            final GetSpotlightBatchErrorCountDTO expectedResult = GetSpotlightBatchErrorCountDTO.builder()
                    .errorStatus("ERROR").errorCount(1).errorFound(true).build();

            when(mockSpotlightBatchService.getSpotlightBatchErrorCount(schemeId)).thenReturn(expectedResult);

            mockMvc.perform(get("/spotlight-batch/scheme/{schemeId}/spotlight-errors", schemeId))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.errorStatus").value("ERROR"))
                    .andExpect(jsonPath("$.errorCount").value(1)).andExpect(jsonPath("$.errorFound").value(true));

        }

    }

}
