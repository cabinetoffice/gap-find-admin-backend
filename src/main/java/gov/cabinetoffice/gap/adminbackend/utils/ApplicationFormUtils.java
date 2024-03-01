package gov.cabinetoffice.gap.adminbackend.utils;

import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;

import java.time.Instant;

public class ApplicationFormUtils {

    public static void updateAuditDetailsAfterFormChange(ApplicationFormEntity applicationFormEntity, boolean isLambdaCall) {
        applicationFormEntity.setLastUpdated(Instant.now());
        if (!isLambdaCall) {
            AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
            applicationFormEntity.setLastUpdateBy(session.getGrantAdminId());
        }
        applicationFormEntity.setVersion(applicationFormEntity.getVersion() + 1);
    }

}
