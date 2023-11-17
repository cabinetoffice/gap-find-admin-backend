package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import gov.cabinetoffice.gap.adminbackend.config.SpotlightPublisherInterceptor;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapper;
import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private final Integer SCHEME_ID = 1;

    private final String DATE = "25 September 2023";

    @Test
    void getSpotlightSubmissionCount() throws Exception {
        when(mockSpotlightSubmissionService.getCountBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                .thenReturn(Long.valueOf(2));
        mockMvc.perform(get("/spotlight-submissions/count/{schemeId}", SCHEME_ID)).andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void getLastUpdatedDate() throws Exception {
        when(mockSpotlightSubmissionService.getLastSubmissionDate(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                .thenReturn(DATE);
        mockMvc.perform(get("/spotlight-submissions/last-updated/{schemeId}", SCHEME_ID)).andExpect(status().isOk())
                .andExpect(content().string(DATE));
    }

    @Nested
    class getSpotlightSubmissionById {

        @Test
        void successfullyRetrieveSpotlightSubmission() throws Exception {
            final UUID spotlightSubmissionId = UUID.randomUUID();
            final Instant now = Instant.now();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                    .created(now).build();

            when(mockSpotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId))
                    .thenReturn(spotlightSubmission);

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

}