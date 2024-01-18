package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByApplicationGrantApplicationIdAndStatus(Integer applicationId, SubmissionStatus status);

    @Query("select s from Submission s where s.id = ?1")
    @EntityGraph(attributePaths = { "applicant" })
    Optional<Submission> findByIdWithApplicant(UUID uuid);

    @Transactional
    @Modifying
    @Query("""
            update Submission s set s.lastRequiredChecksExport = ?1
            where s.application.grantApplicationId = ?2 and s.status = ?3""")
    void updateLastRequiredChecksExportByGrantApplicationIdAndStatus(Instant lastRequiredChecksExport, Integer grantApplicationId, SubmissionStatus status);

    @Transactional
    @Modifying
    @Query("update Submission s set s.lastRequiredChecksExport = ?1 where s.scheme.id = ?2 and s.status = ?3")
    void updateLastRequiredChecksExportBySchemeIdAndStatus(Instant lastRequiredChecksExport, Integer id, SubmissionStatus status);


}
