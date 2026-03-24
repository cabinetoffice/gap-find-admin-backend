package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface GrantAttachmentRepository extends JpaRepository<GrantAttachment, UUID> {

    boolean existsBySubmissionId(UUID id);

    List<GrantAttachment> findBySubmission_Id(UUID submissionId);

    @Transactional
    void deleteBySubmission_Id(UUID submissionId);

}
