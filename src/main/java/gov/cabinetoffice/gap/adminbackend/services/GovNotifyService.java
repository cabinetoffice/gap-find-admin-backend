package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.GovNotifyConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.SendLambdaExportEmailDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class GovNotifyService {

    private final NotificationClient notificationClient;

    private final GovNotifyConfig govNotifyConfig;

    private final GrantExportRepository grantExportRepository;

    public boolean sendLambdaExportEmail(final SendLambdaExportEmailDTO lambdaExportEmailDTO) {
        if (!grantExportRepository.existsById(GrantExportId.builder().exportBatchId(lambdaExportEmailDTO.getExportId())
                .submissionId(lambdaExportEmailDTO.getSubmissionId()).build())) {
            log.error(String.format(
                    "Failed to send lambda export email, no export record exists with the exportBatchId %s & submissionId %s",
                    lambdaExportEmailDTO.getExportId(), lambdaExportEmailDTO.getSubmissionId()));
            return false;
        }
        return sendEmail(govNotifyConfig.getLambdaExportTemplateId(), lambdaExportEmailDTO.getEmailAddress(),
                lambdaExportEmailDTO.getPersonalisation(), lambdaExportEmailDTO.getExportId().toString());
    }

    public boolean sendEmail(final String templateId, final String emailAddress,
            final Map<String, String> personalisation, final String reference) {
        try {
            notificationClient.sendEmail(templateId, emailAddress, personalisation, reference);
        }
        catch (NotificationClientException e) {
            log.error(String.format("Failed to send email with reference: %s", reference), e);
            return false;
        }
        return true;
    }

}
