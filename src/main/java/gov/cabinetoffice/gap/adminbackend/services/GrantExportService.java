package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsListDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.CustomGrantExportMapperImpl;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrantExportService {

    private final GrantExportRepository exportRepository;
    private final CustomGrantExportMapperImpl customGrantExportMapper;

    public Long getOutstandingExportCount(UUID exportId) {
        return exportRepository.countByIdExportBatchIdAndStatusNot(exportId, GrantExportStatus.COMPLETE);
    }

    public GrantExportListDTO getGrantExportsByIdAndStatus(UUID exportId, GrantExportStatus status) {
        final List<GrantExportDTO> grantExportEntityList = exportRepository.findById_ExportBatchIdAndStatus(exportId, status)
                .stream().map(customGrantExportMapper::grantExportEntityToGrantExportDTO).toList();
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

    public ExportedSubmissionsListDto generateExportedSubmissionsListDto(UUID exportId, GrantExportStatus status, Pageable pagination) {

        final List<GrantExportEntity> grantExports = exportRepository.getById_ExportBatchIdAndStatus(exportId, status, pagination );

        return ExportedSubmissionsListDto.builder()
            .grantExportId(exportId)
            .exportedSubmissionDtos(
                    grantExports.stream()
                            .map(customGrantExportMapper::grantExportEntityToExportedSubmissions)
                            .sorted(comparing(ExportedSubmissionsDto::getName))
                            .toList())
            .build();

    }
}
