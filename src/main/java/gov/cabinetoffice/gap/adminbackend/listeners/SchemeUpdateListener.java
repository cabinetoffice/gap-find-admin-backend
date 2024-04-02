package gov.cabinetoffice.gap.adminbackend.listeners;

import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;
import java.util.Optional;

@Slf4j
public class SchemeUpdateListener {

    @PrePersist
    @PreUpdate
    private void beforeAnyUpdate(SchemeEntity scheme) {
        log.info("Setting last updated dates and last updated by values for grant scheme with ID " + scheme.getId());

        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .ifPresent(authentication -> {
                    if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
                        final AdminSession adminSession = (AdminSession) authentication.getPrincipal();

                        // if updates were performed by the system or a super admin then don't populate these fields
                        scheme.getGrantAdmins()
                                .stream()
                                .filter(grantAdmin -> grantAdmin.getId().equals(adminSession.getGrantAdminId()))
                                .findAny()
                                .ifPresent(grantAdmin -> {
                                    scheme.setLastUpdated(Instant.now());
                                    scheme.setLastUpdatedBy(grantAdmin.getId());
                                });
                    }
                });
    }
}
