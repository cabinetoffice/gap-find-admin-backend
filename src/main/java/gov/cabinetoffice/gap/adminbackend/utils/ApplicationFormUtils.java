package gov.cabinetoffice.gap.adminbackend.utils;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.ConflictException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
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
    }

    public static void verifyApplicationFormVersion(Integer version, ApplicationFormEntity applicationFormEntity) {
        if (!Objects.equals(version, applicationFormEntity.getVersion())) {
            throw new ConflictException("MULTIPLE_EDITORS");
        }
    }

    public static ApplicationFormSectionDTO verifyAndGetApplicationFormSection(ApplicationFormEntity applicationForm, String sectionId) {
        ApplicationFormSectionDTO sectionDTO;
        try {
            sectionDTO = applicationForm.getDefinition().getSectionById(sectionId);
        } catch (NotFoundException e) {
            // If the section is not found it must have recently been deleted by another editor.
            throw new ConflictException("MULTIPLE_EDITORS_SECTION_DELETED");
        }
        return sectionDTO;
    }
}
