package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.GovNotifyConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.SendLambdaExportEmailDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSendLambdaExportEmailGenerator.randomSendLambdaExportEmailGenerator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
public class GovNotifyServiceTest {

    @InjectMocks
    @Spy
    private GovNotifyService govNotifyService;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private GrantExportRepository grantExportRepository;

    @Mock
    private GovNotifyConfig govNotifyConfig;

    private final String mockTemplateId = "mockTemplateId";

    @Nested
    class sendLambdaExportEmail {

        @Test
        void shouldSuccessfullySendEmailThroughNotificationClient() throws Exception {
            final SendLambdaExportEmailDTO sendLambdaExportEmailDTO = randomSendLambdaExportEmailGenerator().build();
            when(grantExportRepository.existsById(any(GrantExportId.class))).thenReturn(true);
            when(govNotifyConfig.getLambdaExportTemplateId()).thenReturn(mockTemplateId);
            final boolean response = govNotifyService.sendLambdaExportEmail(sendLambdaExportEmailDTO);

            Assertions.assertTrue(response);

            verify(notificationClient).sendEmail(mockTemplateId, sendLambdaExportEmailDTO.getEmailAddress(),
                    sendLambdaExportEmailDTO.getPersonalisation(), sendLambdaExportEmailDTO.getExportId().toString());
        }

        @Test
        void shouldThrowRuntimeExceptionWhenErrorOccurs() throws Exception {
            final SendLambdaExportEmailDTO sendLambdaExportEmailDTO = randomSendLambdaExportEmailGenerator().build();
            when(grantExportRepository.existsById(any(GrantExportId.class))).thenReturn(true);
            when(govNotifyConfig.getLambdaExportTemplateId()).thenReturn(mockTemplateId);
            when(notificationClient.sendEmail(anyString(), anyString(), any(), any()))
                    .thenThrow(NotificationClientException.class);

            final boolean response = govNotifyService.sendLambdaExportEmail(sendLambdaExportEmailDTO);

            Assertions.assertFalse(response);

            verify(notificationClient).sendEmail(mockTemplateId, sendLambdaExportEmailDTO.getEmailAddress(),
                    sendLambdaExportEmailDTO.getPersonalisation(), sendLambdaExportEmailDTO.getExportId().toString());
        }

        @Test
        void shouldReturnFalseWhenExportRecordDoesNotExist() {
            when(grantExportRepository.existsById(any(GrantExportId.class))).thenReturn(false);

            final boolean response = govNotifyService
                    .sendLambdaExportEmail(randomSendLambdaExportEmailGenerator().build());

            Assertions.assertFalse(response);
        }

    }

    @Nested
    class sendEmail {

        @Test
        void shouldSuccessfullySendEmailThroughNotificationClient() throws Exception {
            final boolean response = govNotifyService.sendEmail(mockTemplateId, "test@gmail.com", null, "reference");

            Assertions.assertTrue(response);

            verify(notificationClient).sendEmail(mockTemplateId, "test@gmail.com", null, "reference");
        }

        @Test
        void shouldThrowRuntimeExceptionWhenErrorOccurs() throws Exception {
            when(notificationClient.sendEmail(anyString(), anyString(), any(), any()))
                    .thenThrow(NotificationClientException.class);

            final boolean response = govNotifyService.sendEmail(mockTemplateId, "test@gmail.com", null, "reference");

            Assertions.assertFalse(response);

            verify(notificationClient).sendEmail(mockTemplateId, "test@gmail.com", null, "reference");
        }

    }

}