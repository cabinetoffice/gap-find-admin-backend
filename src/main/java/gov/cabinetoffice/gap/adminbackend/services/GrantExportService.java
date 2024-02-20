package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.GrantExportMapper;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrantExportService {

    private final GrantExportRepository exportRepository;
    private final GrantExportMapper grantExportMapper;

    public Long getOutstandingExportCount(UUID exportId) {
        return exportRepository.countByIdExportBatchIdAndStatusNot(exportId, GrantExportStatus.COMPLETE);
    }

    public GrantExportListDTO getGrantExportsByIdAndStatus(UUID exportId, GrantExportStatus status) {
        final List<GrantExportDTO> grantExportEntityList = exportRepository.findById_ExportBatchIdAndStatus(exportId, status)
                .stream().map(grantExportMapper::grantExportEntityToGrantExportDTO).toList();
        return GrantExportListDTO.builder().exportBatchId(exportId).grantExports(grantExportEntityList).build();
    }

    public Long getFailedExportsCount(UUID exportId) {
        log.info(String.format("Getting failed export count from grant_export table for exportId: %s", exportId));
        return exportRepository.countByIdExportBatchIdAndStatus(exportId, GrantExportStatus.FAILED);
    }

    public Long getRemainingExportsCount(UUID exportId) {
        log.info(String.format("Getting remaining export count from grant_export table for exportId: %s", exportId));
        return exportRepository.countByIdExportBatchIdAndStatusIsNotIn(exportId,
                List.of(GrantExportStatus.COMPLETE, GrantExportStatus.FAILED));
    }

}
