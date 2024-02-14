package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantExportBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface GrantExportBatchRepository extends JpaRepository<GrantExportBatchEntity, UUID> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE grant_export_batch SET status = :status, last_updated = now() WHERE export_batch_id = :exportBatchId")
    Integer updateStatusById(@Param("exportBatchId") UUID exportBatchId, @Param("status") String status);

    @Transactional
    @Modifying
    @Query("update GrantExportBatchEntity g set g.location = :s3ObjectKey where g.id = :exportBatchId")
    Integer updateLocationById(@Param("exportBatchId") UUID exportBatchId, @Param("s3ObjectKey") String s3ObjectKey);

}
