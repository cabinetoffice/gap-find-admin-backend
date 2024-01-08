package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrantExportService {

    private final GrantExportRepository exportRepository;

    public Long getOutstandingExportCount(UUID exportId) {

        return exportRepository.countByIdExportBatchIdAndStatusNot(exportId, GrantExportStatus.COMPLETE);
    }

}
