package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.GrantAttachment;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAttachmentRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantMandatoryQuestionRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class SubmissionAnonymisationService {

    private final SubmissionRepository submissionRepository;

    private final GrantAttachmentRepository grantAttachmentRepository;

    private final GrantMandatoryQuestionRepository grantMandatoryQuestionRepository;

    private final S3Service s3Service;

    @Transactional
    public void anonymiseSubmission(UUID submissionId) {
        log.info("Anonymising submission {}", submissionId);

        // 1. Delete S3 objects — if any fail, abort and leave the submission in
        // IN_PROGRESS so the scheduler retries it on the next run
        final List<GrantAttachment> attachments = grantAttachmentRepository.findBySubmission_Id(submissionId);
        for (GrantAttachment attachment : attachments) {
            try {
                s3Service.deleteAttachment(attachment.getLocation());
                log.debug("Deleted S3 object {} for submission {}", attachment.getLocation(), submissionId);
            }
            catch (Exception e) {
                log.warn(
                        "Aborting anonymisation of submission {} — failed to delete S3 object {}: {}. "
                                + "Submission will be retried on the next scheduler run.",
                        submissionId, attachment.getLocation(), e.getMessage());
                return;
            }
        }

        // 2. Delete diligence_check rows (no cascade on this FK)
        submissionRepository.deleteDiligenceCheckRowsBySubmissionIds(List.of(submissionId));

        // 3. Delete grant_beneficiary rows
        submissionRepository.deleteBeneficiaryRowsBySubmissionIds(List.of(submissionId));

        // 4. Delete mandatory question rows
        grantMandatoryQuestionRepository.deleteBySubmission_Id(submissionId);

        // 5. Delete attachment DB rows
        grantAttachmentRepository.deleteBySubmission_Id(submissionId);

        // 6. Null out personal data on the submission row and mark EXPIRED
        submissionRepository.anonymiseSubmissions(List.of(submissionId), LocalDateTime.now());

        log.info("Anonymisation complete for submission {}", submissionId);
    }

}
