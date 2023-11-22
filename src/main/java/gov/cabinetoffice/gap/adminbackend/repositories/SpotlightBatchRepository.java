package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpotlightBatchRepository extends JpaRepository<SpotlightBatch, UUID> {
    @Query("select s from SpotlightBatch s order by s.lastSendAttempt desc limit 1")
    SpotlightBatch findMostRecentSpotlightBatch();

    @Query("SELECT (COUNT(s) > 0 ) FROM SpotlightBatch s WHERE s.status = :status AND SIZE(s.spotlightSubmissions) < :maxSize")

    boolean existsByStatusAndSpotlightSubmissionsSizeLessThan(@Param("status") SpotlightBatchStatus status,
            @Param("maxSize") int maxSize);

    @Query("SELECT s FROM SpotlightBatch s WHERE s.status = :status AND SIZE(s.spotlightSubmissions) < :maxSize")
    Optional<SpotlightBatch> findByStatusAndSpotlightSubmissionsSizeLessThan(
            @Param("status") SpotlightBatchStatus status, @Param("maxSize") int maxSize);


}
