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

import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
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
    @Query("update Submission s set s.lastRequiredChecksExport = ?1 where s.application.grantApplicationId = ?2 and s.status = ?3")
    void updateLastRequiredChecksExportByGrantApplicationIdAndStatus(Instant lastRequiredChecksExport,
            Integer grantApplicationId, SubmissionStatus status);

    @Transactional
    @Modifying
    @Query("update Submission s set s.lastRequiredChecksExport = ?1 where s.scheme.id = ?2 and s.status = ?3")
    void updateLastRequiredChecksExportBySchemeIdAndStatus(Instant lastRequiredChecksExport, Integer id,
            SubmissionStatus status);

    @Query(value = """
            SELECT gs.* FROM grant_submission gs
            JOIN grant_advert ga ON ga.scheme_id = gs.scheme_id
            WHERE gs.status = :status
              AND gs.last_updated < :cutoff
              AND ga.closing_date < NOW()
            LIMIT :#{#pageable.pageSize}
            """, nativeQuery = true)
    List<Submission> findByStatusAndLastUpdatedBeforeAndAdvertClosed(@Param("status") String status,
            @Param("cutoff") LocalDateTime cutoff, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE grant_submission
               SET status          = 'EXPIRED',
                   definition      = NULL,
                   submission_name = NULL,
                   gap_id          = NULL,
                   applicant_id    = NULL,
                   created_by      = NULL,
                   last_updated_by = NULL,
                   last_updated    = :now
             WHERE id IN :ids
            """, nativeQuery = true)
    void anonymiseSubmissions(@Param("ids") List<UUID> ids, @Param("now") LocalDateTime now);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM grant_beneficiary WHERE submission_id IN :ids", nativeQuery = true)
    void deleteBeneficiaryRowsBySubmissionIds(@Param("ids") List<UUID> ids);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM diligence_check WHERE submission_id IN :ids", nativeQuery = true)
    void deleteDiligenceCheckRowsBySubmissionIds(@Param("ids") List<UUID> ids);

}
