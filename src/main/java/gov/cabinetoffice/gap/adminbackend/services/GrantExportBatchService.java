package gov.cabinetoffice.gap.adminbackend.services;

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

    public void updateExportBatchStatusById(UUID exportBatchId, GrantExportStatus status) {
        log.info(String.format("Updating grant_export_batch table to %s\nexportBatchId: %s", status, exportBatchId));
        final Integer result = grantExportBatchRepository.updateStatusById(exportBatchId.toString(), status.toString());

        if (result == 1) {
            log.info(String.format("Updated entry in grant_export_batch table to %s\nexportBatchId: %s", status, exportBatchId));
        }
        else {
            log.error(String.format("Could not update entry in export records table to %s\nexportBatchId: %s", status,
                    exportBatchId));
            throw new RuntimeException("Could not update entry in grant_export_batch table to " + status);
        }
    }

    public void addS3ObjectKeyToGrantExportBatch(UUID exportId, String s3ObjectKey) {
        grantExportBatchRepository.updateLocationById(exportId, s3ObjectKey);
    }

}
