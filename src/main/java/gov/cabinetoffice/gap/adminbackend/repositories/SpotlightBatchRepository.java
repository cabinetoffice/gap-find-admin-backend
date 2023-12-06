package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpotlightBatchRepository extends JpaRepository<SpotlightBatch, UUID> {

    @Query("SELECT s FROM SpotlightBatch s WHERE s.lastSendAttempt IS NOT NULL ORDER BY s.lastSendAttempt DESC")
    List<SpotlightBatch> findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(Pageable pageable);

    @Query("SELECT (COUNT(s) > 0 ) FROM SpotlightBatch s WHERE s.status = :status AND SIZE(s.spotlightSubmissions) < :maxSize")
    boolean existsByStatusAndSpotlightSubmissionsSizeLessThan(@Param("status") SpotlightBatchStatus status,
            @Param("maxSize") int maxSize);

    @Query("SELECT s FROM SpotlightBatch s WHERE s.status = :status AND SIZE(s.spotlightSubmissions) < :maxSize")
    Optional<SpotlightBatch> findByStatusAndSpotlightSubmissionsSizeLessThan(
            @Param("status") SpotlightBatchStatus status, @Param("maxSize") int maxSize);

    Optional<List<SpotlightBatch>> findByStatus(@Param("status") SpotlightBatchStatus status);

    @Query("select s from SpotlightBatch s inner join s.spotlightSubmissions spotlightSubmissions where s.status =:status and spotlightSubmissions.mandatoryQuestions.gapId = :gapId")
    Optional<SpotlightBatch> findByStatusAndSpotlightSubmissions_MandatoryQuestions_GapId(
            @Param("status") SpotlightBatchStatus status, @Param("gapId") String gapId);

}
