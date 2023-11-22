package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.SpotlightPublisherInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SendToSpotlightDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SpotlightSchemeDto;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
            final SpotlightSubmission spotlightSubmission = new SpotlightSubmission();
            final SpotlightBatch spotlightBatch = new SpotlightBatch();

            when(mockSpotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId))
                    .thenReturn(spotlightSubmission);
            when(mockSpotlightBatchService.addSpotlightSubmissionToSpotlightBatch(spotlightSubmission,
                    spotlightBatchId)).thenReturn(spotlightBatch);

            mockMvc.perform(
                    patch("/spotlight-batch/{spotlightBatchId}/add-spotlight-submission/{spotlightSubmissionId}",
                            spotlightBatchId, spotlightSubmissionId).header(HttpHeaders.AUTHORIZATION,
                                    LAMBDA_AUTH_HEADER))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Successfully added spotlight submission to spotlight batch"));
        }

        @Test
        void notFoundAddSpotlightSubmissionToSpotlightBatch() throws Exception {
            final UUID spotlightBatchId = UUID.randomUUID();
            final UUID spotlightSubmissionId = UUID.randomUUID();

            when(mockSpotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId))
                    .thenThrow(NotFoundException.class);

            mockMvc.perform(
                    patch("/spotlight-batch/{spotlightBatchId}/add-spotlight-submission/{spotlightSubmissionId}",
                            spotlightBatchId, spotlightSubmissionId).header(HttpHeaders.AUTHORIZATION,
                                    LAMBDA_AUTH_HEADER))
                    .andExpect(status().isNotFound());
        }

        @Test
        void badRequestAddSpotlightSubmissionToSpotlightBatch() throws Exception {
            mockMvc.perform(patch("/spotlight-batch/INVALID_PATH/add-spotlight-submission/INVALID_PATH"))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class generateDataForSpotlightForBatchesWithStatus {

        @Test
        void successfullyGenerateDataForSpotlightForBatchesWithStatus() throws Exception {
            final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().addressLine1("address1")
                    .ggisSchemeId("id1").build();

            final DraftAssessmentDto draftAssessmentDto2 = DraftAssessmentDto.builder().addressLine1("address2")
                    .ggisSchemeId("id2").build();

            final DraftAssessmentDto draftAssessmentDto3 = DraftAssessmentDto.builder().addressLine1("address3")
                    .ggisSchemeId("id1").build();
            final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder().ggisSchemeId("id1")
                    .draftAssessments(List.of(draftAssessmentDto, draftAssessmentDto3)).build();
            final SpotlightSchemeDto spotlightSchemeDto2 = SpotlightSchemeDto.builder().ggisSchemeId("id2")
                    .draftAssessments(List.of(draftAssessmentDto2)).build();
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder()
                    .schemes(List.of(spotlightSchemeDto2, spotlightSchemeDto)).build();
            final List<SendToSpotlightDto> sendToSpotlightDtos = List.of(sendToSpotlightDto);

            final SpotlightBatchStatus status = SpotlightBatchStatus.QUEUED;

            when(mockSpotlightBatchService.generateSendToSpotlightDtosList(status)).thenReturn(sendToSpotlightDtos);

            mockMvc.perform(get("/spotlight-batch/status/{status}/generate-data-for-spotlight", status)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].Schemes").exists());
        }

        @Test
        void returnEmptyListWhenNoBatchesWithStatusAreFound() throws Exception {
            final SpotlightBatchStatus status = SpotlightBatchStatus.QUEUED;

            when(mockSpotlightBatchService.generateSendToSpotlightDtosList(status)).thenReturn(List.of());

            mockMvc.perform(get("/spotlight-batch/status/{status}/generate-data-for-spotlight", status)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER)).andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0))).andExpect(jsonPath("$[0].Schemes").doesNotExist());
        }

    }

}
