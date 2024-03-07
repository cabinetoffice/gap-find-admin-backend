package gov.cabinetoffice.gap.adminbackend.utils;

import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.ConflictException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;

import java.time.Instant;
import java.util.Objects;

public class ApplicationFormUtils {

    public static void updateAuditDetailsAfterFormChange(ApplicationFormEntity applicationFormEntity, boolean isLambdaCall) {
        applicationFormEntity.setLastUpdated(Instant.now());
        if (!isLambdaCall) {
            AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
            applicationFormEntity.setLastUpdateBy(session.getGrantAdminId());
        }
        applicationFormEntity.setRevision(applicationFormEntity.getRevision() + 1);
    }

    public static void verifyApplicationFormRevision(Integer revision, ApplicationFormEntity applicationFormEntity) {
        if (!Objects.equals(revision, applicationFormEntity.getRevision())) {
            throw new ConflictException("MULTIPLE_EDITORS");
        }
    }

}
