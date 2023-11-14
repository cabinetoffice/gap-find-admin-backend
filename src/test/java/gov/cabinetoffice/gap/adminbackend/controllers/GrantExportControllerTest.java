package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.OutstandingExportCountDTO;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.GrantExportService;
import gov.cabinetoffice.gap.adminbackend.services.SecretAuthService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GrantExportController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { GrantExportController.class, ControllerExceptionHandler.class })
public class GrantExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrantExportService mockGrantExportService;

    @MockBean
    private SecretAuthService secretAuthService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    private final String LAMBDA_AUTH_HEADER = "topSecretKey";

    @Nested
    class getOutstandingExportsCount {

        @Test
        void successfullyGetOutstandingExportsCount() throws Exception {
            UUID mockExportId = UUID.randomUUID();
            Long mockCount = 10L;
            OutstandingExportCountDTO expectedResponse = new OutstandingExportCountDTO(mockCount);
            doNothing().when(secretAuthService).authenticateSecret(LAMBDA_AUTH_HEADER);
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
            UUID mockExportId = UUID.randomUUID();
            doNothing().when(secretAuthService).authenticateSecret(LAMBDA_AUTH_HEADER);
            when(mockGrantExportService.getOutstandingExportCount(any())).thenThrow(RuntimeException.class);

            mockMvc.perform(get("/export-batch/" + mockExportId + "/outstandingCount").header(HttpHeaders.AUTHORIZATION,
                    LAMBDA_AUTH_HEADER)).andExpect(status().isInternalServerError());
        }

    }

}
