package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrantExportService {

    private final GrantExportRepository exportRepository;

    public Long getOutstandingExportCount(UUID exportId) {
        return exportRepository.countByIdExportBatchIdAndStatusNot(exportId, GrantExportStatus.COMPLETE);
    }

    public GrantExportListDTO getGrantExportsByIdAndStatus(UUID exportId, GrantExportStatus status) {
        final List<GrantExportDTO> grantExportEntityList = exportRepository.findById_ExportBatchIdAndStatus(exportId, status)
                .stream().map(this::mapGrantExportEntityToGrantExportDto).toList();
        return GrantExportListDTO.builder().exportBatchId(exportId).grantExports(grantExportEntityList).build();
    }

    private GrantExportDTO mapGrantExportEntityToGrantExportDto(GrantExportEntity grantExportEntity) {
        return GrantExportDTO.builder()
                .exportBatchId(grantExportEntity.getId().getExportBatchId())
                .submissionId(grantExportEntity.getId().getSubmissionId())
                .applicationId(grantExportEntity.getApplicationId())
                .status(grantExportEntity.getStatus())
                .emailAddress(grantExportEntity.getEmailAddress())
                .created(grantExportEntity.getCreated())
                .createdBy(grantExportEntity.getCreatedBy())
                .lastUpdated(grantExportEntity.getLastUpdated())
                .location(grantExportEntity.getLocation())
                .build();
    }

}
