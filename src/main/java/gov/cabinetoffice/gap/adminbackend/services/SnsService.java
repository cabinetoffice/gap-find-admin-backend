package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import gov.cabinetoffice.gap.adminbackend.config.SnsConfigProperties;
import lombok.RequiredArgsConstructor;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.AmazonSNSException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SnsService {

    private final AmazonSNSClient snsClient;

    private final SnsConfigProperties snsConfigProperties;

    private String publishMessageToTopic(String subject, String body) {
        try {
            final PublishRequest request = new PublishRequest(snsConfigProperties.getTopicArn(), body, subject);
            final PublishResult result = snsClient.publish(request);
            return "Message with message id:" + result.getMessageId() + " sent.";

        }
        catch (AmazonSNSException e) {
            return e.getErrorMessage();
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

}
