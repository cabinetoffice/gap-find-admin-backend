package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import gov.cabinetoffice.gap.adminbackend.config.SnsConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class SnsService {

    private final AmazonSNSClient snsClient;

    private final SnsConfigProperties snsConfigProperties;

    private String publishMessageToTopic(String subject, String body) {
        try {
            final PublishRequest request = new PublishRequest(snsConfigProperties.getTopicArn(), body, subject);
            final PublishResult result = snsClient.publish(request);
            log.info("Message published to SNS topic");
            return "Message with message id:" + result.getMessageId() + " sent.";
        }
        catch (AmazonSNSException e) {
            return "Error publishing message to SNS topic with error: " + e.getErrorMessage();
        }

    }

    public String spotlightOAuthDisconnected() {
        final String subject = "Spotlight API has disconnected";
        final String body = """
                What do you need to do?

                Go into the Super Admin Dashboard and reconnect it. Go into the integration tab and click ‘Reconnect’.

                If the ‘Reconnect’ button fails to get the connection working, contact the Spotlight support team to investigate.

                Once fixed, the data will be sent to Spotlight again automatically.\s""";

        return publishMessageToTopic(subject, body);
    }

    public String spotlightValidationError() {
        final String subject = "Can’t send data to Spotlight";
        final String body = """
                There is a validation issue between Find and Spotlight.\s

                What do you need to do?

                Speak to the Spotlight team to understand if they have made any changes to validation. It is likely that they have, and they will need to fix the problem.

                No data can be sent to Spotlight until this issue is fixed. It is important to fix it quickly.

                Be mindful of any applications that are live and consider contacting the administrator.\s

                Once fixed, data received while the link was broken will not be sent to Spotlight automatically.\s""";

        return publishMessageToTopic(subject, body);
    }

}
