package gov.cabinetoffice.gap.adminbackend.security;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

public class WithAdminSessionSecurityContextFactory implements WithSecurityContextFactory<WithAdminSession> {

    @Override
    public SecurityContext createSecurityContext(WithAdminSession adminSession) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        AdminSession principal = new AdminSession(adminSession.grantAdminId(), adminSession.funderId(), "Test", "User",
                "AND Digital", "test@domain.com");
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        context.setAuthentication(auth);
        return context;
    }

}
