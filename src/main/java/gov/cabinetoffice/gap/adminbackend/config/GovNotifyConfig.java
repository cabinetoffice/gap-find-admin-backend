package gov.cabinetoffice.gap.adminbackend.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
@RequiredArgsConstructor
@Data
public class GovNotifyConfig {

    @Value("${gov-notify-api-key}")
    private String govNotifyAPIKey;

    @Value("${gov-notify-lambda-export-template-id}")
    private String lambdaExportTemplateId;

    @Bean
    public NotificationClient notificationClient() {
        return new NotificationClient(govNotifyAPIKey);
    }

}
