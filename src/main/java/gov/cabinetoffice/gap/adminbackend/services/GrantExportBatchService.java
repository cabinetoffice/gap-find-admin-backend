package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.GrantExportBatchEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.GrantExportBatchException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrantExportBatchService {

    private final GrantExportBatchRepository grantExportBatchRepository;

    public void updateExportBatchStatusById(UUID exportId, GrantExportStatus status) {
        final Integer result = grantExportBatchRepository.updateStatusById(exportId.toString(), status.toString());

        if (result == 1) {
            log.info("Updated entry in grant_export_batch table to {} exportBatchId: {}", status, exportId);
        }
        else {
            log.error("Could not update entry in grant_export_batch table to {} exportBatchId: {}", status,
                    exportId);
            throw new GrantExportBatchException("Could not update entry in grant_export_batch table to " + status);
        }
    }

    public void addS3ObjectKeyToGrantExportBatch(UUID exportId, String s3ObjectKey) {
        final Integer result = grantExportBatchRepository.updateLocationById(exportId, s3ObjectKey);
        if (result == 1) {
            log.info("Updated entry in grant_export_batch table to {} exportBatchId: {}", s3ObjectKey, exportId);
        }
        else {
            log.error("Could not update entry in grant_export_batch table to {} exportBatchId: {}", s3ObjectKey,
                    exportId);
            throw new GrantExportBatchException("Could not update entry in grant_export_batch table to " + s3ObjectKey);
        }
    }

    public String getSuperZipLocation(UUID exportId) {
        log.info("Getting super zip location for exportId: {}", exportId);
        final Optional<GrantExportBatchEntity> result = grantExportBatchRepository.findById(exportId);

        if(result.isEmpty()){
            log.error("Could not find entry in grant_export_batch table for exportId: {}", exportId);
            throw new NotFoundException("Could not find entry in grant_export_batch table for exportId: " + exportId);
        }
        else {
            log.info("Found entry in grant_export_batch table for exportId: {}", exportId);
            return result.get().getLocation();
        }


    }

}
