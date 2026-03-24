package gov.cabinetoffice.gap.adminbackend.schedulers;

import gov.cabinetoffice.gap.adminbackend.config.SubmissionAnonymisationConfigProperties;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.SubmissionRepository;
import gov.cabinetoffice.gap.adminbackend.services.SubmissionAnonymisationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "submission-anonymisation-scheduler.enabled", havingValue = "true")
public class SubmissionAnonymisationScheduler {

    private final SubmissionRepository submissionRepository;

    private final SubmissionAnonymisationService submissionAnonymisationService;

    private final SubmissionAnonymisationConfigProperties config;

    @Scheduled(cron = "${submission-anonymisation-scheduler.cronExpression:0 0 3 * * ?}", zone = "UTC")
    @SchedulerLock(name = "submissionAnonymisation_anonymiseInactiveSubmissions",
            lockAtMostFor = "${submission-anonymisation-scheduler.lock.atMostFor:30m}",
            lockAtLeastFor = "${submission-anonymisation-scheduler.lock.atLeastFor:5m}")
    public void anonymiseInactiveSubmissions() {
        final LocalDateTime cutoff = LocalDateTime.now().minusDays(config.getDaysBeforeExpiry());

        log.info("Submission anonymisation scheduler started. Anonymising IN_PROGRESS submissions last updated before {}",
                cutoff);

        final List<Submission> dueForAnonymisation = submissionRepository
                .findByStatusAndLastUpdatedBefore(SubmissionStatus.IN_PROGRESS, cutoff,
                        PageRequest.of(0, config.getBatchSize()));

        log.info("Found {} submission(s) to anonymise", dueForAnonymisation.size());

        dueForAnonymisation
                .forEach(submission -> submissionAnonymisationService.anonymiseSubmission(submission.getId()));

        log.info("Submission anonymisation scheduler completed successfully.");
    }

}
