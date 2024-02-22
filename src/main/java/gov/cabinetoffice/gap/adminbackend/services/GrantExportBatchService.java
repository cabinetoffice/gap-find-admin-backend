package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportBatchDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportBatchEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrantExportBatchService {

    private final GrantExportBatchRepository grantExportBatchRepository;

    public void updateExportBatchStatusById(UUID exportId, GrantExportStatus status) {
        final Integer result = grantExportBatchRepository.updateStatusById(exportId.toString(), status.toString());

        if (result == 1) {
            log.info(String.format("Updated entry in grant_export_batch table to %s\nexportBatchId: %s", status, exportId));
        }
        else {
            log.error(String.format("Could not update entry in grant_export_batch table to %s\nexportBatchId: %s", status,
                    exportId));
            throw new RuntimeException("Could not update entry in grant_export_batch table to " + status);
        }
    }

    public void addS3ObjectKeyToGrantExportBatch(UUID exportId, String s3ObjectKey) {
        final Integer result = grantExportBatchRepository.updateLocationById(exportId, s3ObjectKey);
        if (result == 1) {
            log.info(String.format("Updated entry in grant_export_batch table to %s\nexportBatchId: %s", s3ObjectKey, exportId));
        }
        else {
            log.error(String.format("Could not update entry in grant_export_batch table to %s\nexportBatchId: %s", s3ObjectKey,
                    exportId));
            throw new RuntimeException("Could not update entry in grant_export_batch table to " + s3ObjectKey);
        }
    }

    public GrantExportBatchEntity getGrantExportBatch(UUID exportId) {
        return grantExportBatchRepository.getReferenceById(exportId);
    }

}
