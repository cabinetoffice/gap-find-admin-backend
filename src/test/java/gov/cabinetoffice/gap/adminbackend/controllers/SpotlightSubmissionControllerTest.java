package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.SpotlightPublisherInterceptor;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightSubmissionDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.SpotlightSubmissionMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapper;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
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

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Nested
    class getSpotlightSubmissionById {

        @Test
        void successfullyRetrieveSpotlightSubmission() throws Exception {
            final UUID spotlightSubmissionId = UUID.randomUUID();
            final Instant now = Instant.now();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                .id(spotlightSubmissionId)
                .created(now)
                .build();
            final SpotlightSubmissionDto expectedResult = SpotlightSubmissionDto.builder()
                .id(spotlightSubmissionId)
                .created(now)
                .build();

            when(mockSpotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId))
                .thenReturn(spotlightSubmission);
            when(spotlightSubmissionMapper.spotlightSubmissionToSpotlightSubmissionDto(spotlightSubmission))
                .thenReturn(expectedResult);

            mockMvc
                .perform(get("/spotlight-submissions/{spotlightSubmissionId}", spotlightSubmissionId)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        void notFoundAddSpotlightSubmissionToSpotlightBatch() throws Exception {

            final UUID spotlightSubmissionId = UUID.randomUUID();

            when(mockSpotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId))
                .thenThrow(NotFoundException.class);

            mockMvc
                .perform(get("/spotlight-submissions/{spotlightSubmissionId}", spotlightSubmissionId)
                    .header(HttpHeaders.AUTHORIZATION, LAMBDA_AUTH_HEADER))
                .andExpect(status().isNotFound());
        }

    }

}