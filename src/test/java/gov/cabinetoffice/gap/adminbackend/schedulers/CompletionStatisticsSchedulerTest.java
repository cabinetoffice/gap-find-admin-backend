package gov.cabinetoffice.gap.adminbackend.schedulers;

import com.amazonaws.services.sqs.AmazonSQS;
import gov.cabinetoffice.gap.adminbackend.config.CompletionStatisticsSchedulerConfigProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompletionStatisticsSchedulerTest {

    @Mock
    private AmazonSQS amazonSQS;

    @Spy
    private CompletionStatisticsSchedulerConfigProperties completionStatisticsSchedulerConfigProperties = CompletionStatisticsSchedulerConfigProperties
            .builder().queue("test-sqs-queue.com").build();

    @InjectMocks
    private CompletionStatisticsScheduler completionStatisticsScheduler;

    @Nested
    class completionStatisticsSchedulerQueue {

        @Test
        void successfullyScheduleAdverts_singleBatch() {

            completionStatisticsScheduler.sendCompletionStatisticsQueueTrigger();

            ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
            verify(amazonSQS, times(1)).sendMessage(eq(completionStatisticsSchedulerConfigProperties.getQueue()),
                    argument.capture());
            String sentMessage = argument.getValue();

            assertThat(sentMessage).isEqualTo("Run calculations from admin-backend");

            verifyNoMoreInteractions(amazonSQS);
        }

    }

}
