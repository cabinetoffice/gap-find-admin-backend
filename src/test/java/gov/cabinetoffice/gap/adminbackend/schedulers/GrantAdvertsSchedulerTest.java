package gov.cabinetoffice.gap.adminbackend.schedulers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import gov.cabinetoffice.gap.adminbackend.config.GrantAdvertsSchedulerConfigProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GrantAdvertSchedulerView;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSchedulerAction;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdvertSchedulerViewRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GrantAdvertsSchedulerTest {

    @Mock
    private GrantAdvertSchedulerViewRepository advertSchedulerRepository;

    @Mock
    private AmazonSQS amazonSQS;

    @Spy
    private GrantAdvertsSchedulerConfigProperties advertsSchedulerConfigProperties = GrantAdvertsSchedulerConfigProperties
            .builder().grantAdvertSchedulerQueue("test-sqs-queue.com").build();

    @InjectMocks
    private GrantAdvertsScheduler grantAdvertsScheduler;

    @Nested
    class grantAdvertsSchedulerQueue {

        @Test
        void successfullyScheduleAdverts_singleBatch() {
            UUID advertId = UUID.randomUUID();
            GrantAdvertSchedulerView scheduleView = new GrantAdvertSchedulerView(advertId,
                    GrantAdvertSchedulerAction.PUBLISH);
            List<GrantAdvertSchedulerView> scheduleViewList = Collections.singletonList(scheduleView);

            when(advertSchedulerRepository.findAll()).thenReturn(scheduleViewList);

            grantAdvertsScheduler.getGrantAdvertsToBeActioned();

            ArgumentCaptor<SendMessageBatchRequest> argument = ArgumentCaptor.forClass(SendMessageBatchRequest.class);
            verify(amazonSQS, times(1)).sendMessageBatch(argument.capture());

            SendMessageBatchRequest batchRequest = argument.getValue();

            assertThat(batchRequest.getQueueUrl()).isEqualTo("test-sqs-queue.com");

            SendMessageBatchRequestEntry batchEntry = batchRequest.getEntries().get(0);

            assertThat(argument.getValue().getEntries()).hasSize(1);
            assertThat(batchEntry.getMessageAttributes().get("grantAdvertId").getStringValue())
                    .isEqualTo(advertId.toString());
            assertThat(batchEntry.getMessageAttributes().get("action").getStringValue())
                    .isEqualTo(GrantAdvertSchedulerAction.PUBLISH.toString());
        }

        @Test
        void successfullyScheduleAdverts_multipleBatchesOf10() {
            List<GrantAdvertSchedulerView> scheduleViewList = new ArrayList<>();

            for (int i = 0; i < 15; ++i) {
                GrantAdvertSchedulerView scheduleView = new GrantAdvertSchedulerView(UUID.randomUUID(),
                        GrantAdvertSchedulerAction.PUBLISH);
                scheduleViewList.add(scheduleView);
            }

            when(advertSchedulerRepository.findAll()).thenReturn(scheduleViewList);

            grantAdvertsScheduler.getGrantAdvertsToBeActioned();

            ArgumentCaptor<SendMessageBatchRequest> argument = ArgumentCaptor.forClass(SendMessageBatchRequest.class);
            verify(amazonSQS, times(2)).sendMessageBatch(argument.capture());

            List<SendMessageBatchRequest> capturedBatchRequests = argument.getAllValues();

            assertThat(capturedBatchRequests).hasSize(2)
                    .allMatch(batchRequest -> batchRequest.getQueueUrl().equals("test-sqs-queue.com"));
            assertThat(capturedBatchRequests.get(0).getEntries()).hasSize(10);
            assertThat(capturedBatchRequests.get(1).getEntries()).hasSize(5);
        }

        @Test
        void Successful_noAdvertsToBeScheduled() {
            when(advertSchedulerRepository.findAll()).thenReturn(Collections.emptyList());

            grantAdvertsScheduler.getGrantAdvertsToBeActioned();

            verify(amazonSQS, times(0)).sendMessageBatch(any());

        }

    }

}
