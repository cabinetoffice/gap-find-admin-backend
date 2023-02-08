package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.SendLambdaExportEmailDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.GovNotifyService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSendLambdaExportEmailGenerator.randomSendLambdaExportEmailGenerator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GovNotifyController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { GovNotifyController.class, ControllerExceptionHandler.class })
class GovNotifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GovNotifyService govNotifyService;

    @MockBean
    private SecretAuthService secretAuthService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    private final String apiSecretKey = "topSecretKey";

    @Nested
    class sendEmail {

        @Test
        void happyPathTest() throws Exception {
            doNothing().when(secretAuthService).authenticateSecret(apiSecretKey);
            when(govNotifyService.sendLambdaExportEmail(any(SendLambdaExportEmailDTO.class))).thenReturn(true);

            mockMvc.perform(post("/emails/sendLambdaConfirmationEmail").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonString(randomSendLambdaExportEmailGenerator().build()))
                    .header(HttpHeaders.AUTHORIZATION, apiSecretKey)).andExpect(status().isOk());
        }

        @Test
        void failedToSendEmailTest() throws Exception {
            doNothing().when(secretAuthService).authenticateSecret(apiSecretKey);
            when(govNotifyService.sendLambdaExportEmail(any(SendLambdaExportEmailDTO.class))).thenReturn(false);

            mockMvc.perform(post("/emails/sendLambdaConfirmationEmail").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonString(randomSendLambdaExportEmailGenerator().build()))
                    .header(HttpHeaders.AUTHORIZATION, apiSecretKey)).andExpect(status().isInternalServerError());
        }

        @Test
        void incorrectAuthHeaderTest() throws Exception {
            doThrow(new UnauthorizedException()).when(secretAuthService).authenticateSecret(apiSecretKey);

            mockMvc.perform(post("/emails/sendLambdaConfirmationEmail").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonString(randomSendLambdaExportEmailGenerator().build()))
                    .header(HttpHeaders.AUTHORIZATION, apiSecretKey)).andExpect(status().isUnauthorized());
        }

    }

}
