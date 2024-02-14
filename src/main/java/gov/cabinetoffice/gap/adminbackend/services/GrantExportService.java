package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
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

    public List<GrantExportEntity> getGrantExportsByIdAndStatus(UUID exportId, GrantExportStatus status) {
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();
        return exportRepository.findAllByIdExportBatchIdAndStatusAndCreatedBy(exportId, status, adminSession.getGrantAdminId());
    }

}
