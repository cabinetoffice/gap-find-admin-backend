package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.security.AuthManagerV2;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(value = "feature.onelogin.enabled", havingValue = "true")
@RequestMapping("v2")
public class LoginControllerV2 {

    private final AuthManagerV2 authManager;

    @Value("${spring.profiles.active:PROD}")
    private String profile;

    @Value("${debug.ignore-jwt:false}")
    private boolean ignoreJwt;

    @Value("${debug.grant-admin-id:0}")
    private Integer grantAdminId;

    @Value("${debug.funder-id:0}")
    private Integer funderId;

    @Value("${debug.funder-name:AND Digital}")
    private String funderName;

    @Value("${debug.email-address:test@domain.com}")
    private String emailAddress;

    @PostMapping("/login")
    public ResponseEntity<String> login(HttpServletRequest httpRequest) {
        // so local dev work won't be quite as miserable
        if (Objects.equals(this.profile, "LOCAL") && this.ignoreJwt) {
            if (this.grantAdminId == null || this.funderId == null) {
                return ResponseEntity.internalServerError().build();
            }
            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(
                            new AdminSession(this.grantAdminId, this.funderId, "Test", "User", this.funderName,
                                    emailAddress),
                            null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            return ResponseEntity.ok().build();
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("",
                httpRequest.getHeader(HttpHeaders.AUTHORIZATION));

        Authentication authenticate = this.authManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        return ResponseEntity.ok().build();
    }

}
