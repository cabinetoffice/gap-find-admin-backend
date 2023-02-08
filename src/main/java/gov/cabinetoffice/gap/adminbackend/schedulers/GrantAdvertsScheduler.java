package gov.cabinetoffice.gap.adminbackend.schedulers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.google.common.collect.Lists;
import gov.cabinetoffice.gap.adminbackend.config.GrantAdvertsSchedulerConfigProperties;
import gov.cabinetoffice.gap.adminbackend.constants.AWSConstants;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GrantAdvertSchedulerView;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdvertSchedulerViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class GrantAdvertsScheduler {

    private final GrantAdvertSchedulerViewRepository advertSchedulerRepository;

    private final AmazonSQS amazonSQS;

    private final GrantAdvertsSchedulerConfigProperties advertsSchedulerConfigProperties;

    @Scheduled(cron = "${grant-adverts-scheduler.cronExpression:0 0 0 * * ?}", zone = "Europe/London")
    @SchedulerLock(name = "grantAdverts_publishUnpublishScheduler",
            lockAtMostFor = "${grant-adverts-scheduler.lock.atMostFor:30m}",
            lockAtLeastFor = "${grant-adverts-scheduler.lock.atLeastFor:5m}")
    public void getGrantAdvertsToBeActioned() {

        log.info("Grant Adverts Scheduler has started.");

        final List<GrantAdvertSchedulerView> advertsToHandle = this.advertSchedulerRepository.findAll();

        log.debug(String.format("%s grant advert(s) to be actioned", advertsToHandle.size()));

        final List<List<GrantAdvertSchedulerView>> partitionedAdverts = Lists.partition(advertsToHandle,
                AWSConstants.MAX_ALLOWED_SQS_FIFO_BATCH_SIZE);

        log.debug(String.format("%s batch(es) to be sent to SQS", partitionedAdverts.size()));

        partitionedAdverts.forEach(schedulerViewBatch -> this.amazonSQS
                .sendMessageBatch(mapAdvertsToBatchMessageRequest(schedulerViewBatch)));

        log.info("Grant Adverts Scheduler has completed successfully.");

    }

    private SendMessageBatchRequest mapAdvertsToBatchMessageRequest(
            final List<GrantAdvertSchedulerView> grantAdvertSchedulerViewBatch) {

        final List<SendMessageBatchRequestEntry> sendMessageBatchRequestEntries = grantAdvertSchedulerViewBatch.stream()
                .map(schedulerView -> {
                    final String randomUuid = UUID.randomUUID().toString();

                    return new SendMessageBatchRequestEntry().withId(randomUuid).withMessageDeduplicationId(randomUuid)
                            .withMessageBody(randomUuid).withMessageGroupId(randomUuid)
                            .addMessageAttributesEntry("grantAdvertId",
                                    new MessageAttributeValue().withDataType("String")
                                            .withStringValue(schedulerView.getId().toString()))
                            .addMessageAttributesEntry("action", new MessageAttributeValue().withDataType("String")
                                    .withStringValue(schedulerView.getAction().toString()));
                }).toList();

        return new SendMessageBatchRequest(advertsSchedulerConfigProperties.getGrantAdvertSchedulerQueue())
                .withEntries(sendMessageBatchRequestEntries);

    }

}