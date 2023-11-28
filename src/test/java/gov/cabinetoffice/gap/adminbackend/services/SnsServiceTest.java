package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SnsServiceTest {

    private static AmazonSNSClient snsClient;

    private static SnsService snsService;

    @BeforeAll
    static void beforeAll() {
        snsClient = mock(AmazonSNSClient.class);
        snsService = new SnsService(snsClient);
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
            final PublishResult mockResult = new PublishResult().withMessageId("mockMessageId");
            when(snsClient.publish(any(PublishRequest.class))).thenReturn(mockResult);

            final String result = snsService.spotlightOAuthDisconnected();

            assertThat(result).isEqualTo("Message with message id:mockMessageId sent.");
        }

        @Test
        void throwsException() {
            when(snsClient.publish(any())).thenThrow(new AmazonSNSException("error publishing message"));
            final String result = snsService.spotlightOAuthDisconnected();
            assertThat(result).isEqualTo("error publishing message");
        }

    }

}