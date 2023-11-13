package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
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

@WebMvcTest(SpotlightSubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SpotlightSubmissionController.class, ControllerExceptionHandler.class })
class SpotlightSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpotlightSubmissionService spotlightSubmissionService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    private final Integer SCHEME_ID = 1;

    private final String DATE = "25 September 2023";

    @Test
    void getSpotlightSubmissionCount() throws Exception {
        when(spotlightSubmissionService.getCountBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                .thenReturn(Long.valueOf(2));
        mockMvc.perform(get("/spotlight-submissions/count/{schemeId}", SCHEME_ID)).andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void getLastUpdatedDate() throws Exception {
        when(spotlightSubmissionService.getLastSubmissionDate(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                .thenReturn(DATE);
        mockMvc.perform(get("/spotlight-submissions/last-updated/{schemeId}", SCHEME_ID)).andExpect(status().isOk())
                .andExpect(content().string(DATE));
    }

}
