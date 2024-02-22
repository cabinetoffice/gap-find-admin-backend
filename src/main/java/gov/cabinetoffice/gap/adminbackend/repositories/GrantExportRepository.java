package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface GrantExportRepository extends JpaRepository<GrantExportEntity, GrantExportId> {

    boolean existsByApplicationIdAndStatus(Integer applicationId, GrantExportStatus status);

    boolean existsByApplicationId(Integer applicationId);

    List<GrantExportEntity> findAllByIdExportBatchIdAndStatusAndCreatedBy(UUID exportGrantId, GrantExportStatus status,
            Integer createdBy);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE grant_export SET status = :status, last_updated = now() WHERE export_batch_id = cast(:exportBatchId AS UUID) AND submission_id = cast(:submissionId AS UUID)")
    Integer updateExportRecordStatus(@Param("submissionId") String submissionId,
            @Param("exportBatchId") String exportBatchId, @Param("status") String status);

    @Transactional
    @Modifying
    @Query("UPDATE GrantExportEntity e SET e.location = :s3ObjectKey WHERE e.id.exportBatchId = :exportBatchId AND e.id.submissionId = :submissionId")
    void updateExportRecordLocation(@Param("submissionId") UUID submissionId,
            @Param("exportBatchId") UUID exportBatchId, @Param("s3ObjectKey") String s3ObjectKey);

    Long countByIdExportBatchIdAndStatusNot(UUID exportGrantId, GrantExportStatus status);

    List<GrantExportEntity> findById_ExportBatchIdAndStatus(UUID exportBatchId, GrantExportStatus status);

    long countByIdExportBatchIdAndStatus(UUID exportBatchId, GrantExportStatus status);

    long countByIdExportBatchIdAndStatusIsNotIn(UUID exportBatchId, Collection<GrantExportStatus> statuses);

    List<GrantExportEntity> getById_ExportBatchIdAndStatus(UUID exportBatchId, GrantExportStatus status,
            Pageable pageable);

}
