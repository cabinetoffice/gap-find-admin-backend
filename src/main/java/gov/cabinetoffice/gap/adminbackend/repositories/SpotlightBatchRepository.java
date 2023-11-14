package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpotlightBatchRepository extends JpaRepository<SpotlightBatch, UUID> {

    @Query("select (count(s) > 0) from SpotlightBatch s where s.status = ?1 AND SIZE(s.spotlightSubmissions) < ?2")
    Boolean existsByStatus(SpotlightBatchStatus status, int maxSize);

    @Query("SELECT sb FROM SpotlightBatch sb WHERE sb.status = ?1 AND SIZE(sb.spotlightSubmissions) < ?2")
    Optional<SpotlightBatch> findByStatusAndSpotlightSubmissionsSizeLessThan(SpotlightBatchStatus status, int maxSize);

}
