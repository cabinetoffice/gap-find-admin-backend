package gov.cabinetoffice.gap.adminbackend.schedulers;

import com.amazonaws.services.sqs.AmazonSQS;
import gov.cabinetoffice.gap.adminbackend.config.CompletionStatisticsSchedulerConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CompletionStatisticsScheduler {

    private final AmazonSQS amazonSQS;

    private final CompletionStatisticsSchedulerConfigProperties completionStatisticsSchedulerConfigProperties;

    @Scheduled(cron = "${completion-statistics-scheduler.cronExpression:0 6 * * * ?}", zone = "UTC")
    @SchedulerLock(name = "completionStatistics_triggerCalculationsScheduler",
            lockAtMostFor = "${completion-statistics-scheduler.lock.atMostFor:30m}",
            lockAtLeastFor = "${completion-statistics-scheduler.lock.atLeastFor:5m}")
    public void sendCompletionStatisticsQueueTrigger() {

        log.info("Completion Statistics Scheduler has started.");

        this.amazonSQS
                .sendMessage(completionStatisticsSchedulerConfigProperties.getQueue(), "Run calculations from admin-backend");

        log.info("Grant Adverts Scheduler has completed successfully.");

    }


}