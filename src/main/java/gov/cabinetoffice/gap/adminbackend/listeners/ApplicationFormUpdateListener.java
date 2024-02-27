package gov.cabinetoffice.gap.adminbackend.listeners;

import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class ApplicationFormUpdateListener {

    @PrePersist
    @PreUpdate
    private void beforeAnyUpdate(ApplicationFormEntity applicationForm) {
        log.info("Setting last updated dates and last updated by values for application form with ID " + applicationForm.getGrantApplicationId());
        final AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();

        applicationForm.setLastUpdated(Instant.now());
        applicationForm.setLastUpdateBy(adminSession.getGrantAdminId());
    }
}
