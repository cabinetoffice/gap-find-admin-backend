package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import gov.cabinetoffice.gap.adminbackend.config.SnsConfigProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SnsServiceTest {

    private static AmazonSNSClient snsClient;

    private static SnsService snsService;

    private static final String MESSAGE_ID = "mockMessageId";

    private static final String ERROR = "error publishing message";

    @BeforeAll
    static void beforeAll() {
        SnsConfigProperties snsConfigProperties = SnsConfigProperties.builder().topicArn("topicArn").build();
        snsClient = mock(AmazonSNSClient.class);
        snsService = new SnsService(snsClient, snsConfigProperties);
        ReflectionTestUtils.setField(snsService, "snsClient", snsClient);
    }

    @BeforeEach
    void resetMocks() {
        reset(snsClient);
    }

    @Nested
    class spotlightOAuthDisconnected {

        @Test
        void successfullyPublishesMessage() {
            final PublishResult mockResult = new PublishResult().withMessageId(MESSAGE_ID);
            when(snsClient.publish(any(PublishRequest.class))).thenReturn(mockResult);

            final String result = snsService.spotlightOAuthDisconnected();

            assertThat(result).isEqualTo("Message with message id:" + MESSAGE_ID + " sent.");
        }

        @Test
        void throwsException() {
            when(snsClient.publish(any())).thenThrow(new AmazonSNSException(ERROR));
            final String result = snsService.spotlightOAuthDisconnected();
            assertThat(result).isEqualTo("Error publishing message to SNS topic with error: " + ERROR);
        }

    }

    @Nested
    class spotlightApiError {

        @Test
        void successfullyPublishesMessage() {
            final PublishResult mockResult = new PublishResult().withMessageId(MESSAGE_ID);
            when(snsClient.publish(any(PublishRequest.class))).thenReturn(mockResult);

            final String result = snsService.spotlightApiError();

            assertThat(result).isEqualTo("Message with message id:" + MESSAGE_ID + " sent.");
        }

        @Test
        void throwsException() {
            when(snsClient.publish(any())).thenThrow(new AmazonSNSException(ERROR));
            final String result = snsService.spotlightApiError();
            assertThat(result).isEqualTo("Error publishing message to SNS topic with error: " + ERROR);
        }

    }

    @Nested
    class spotlightValidationError {

        @Test
        void successfullyPublishesMessage() {
            final PublishResult mockResult = new PublishResult().withMessageId(MESSAGE_ID);
            when(snsClient.publish(any(PublishRequest.class))).thenReturn(mockResult);

            final String result = snsService.spotlightValidationError();

            assertThat(result).isEqualTo("Message with message id:" + MESSAGE_ID + " sent.");
        }

        @Test
        void throwsException() {
            when(snsClient.publish(any())).thenThrow(new AmazonSNSException(ERROR));
            final String result = snsService.spotlightValidationError();
            assertThat(result).isEqualTo("Error publishing message to SNS topic with error: " + ERROR);
        }

    }

}