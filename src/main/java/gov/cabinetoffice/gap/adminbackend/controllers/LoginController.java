package gov.cabinetoffice.gap.adminbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authManager;

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

    @Value("${user-service.domain}")
    private String userServiceDomain;

    @PostMapping("/login")
    public ResponseEntity<String> login(HttpServletRequest httpRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("",
                httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
        Authentication authenticate = this.authManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest httpRequest) {
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        securityContextLogoutHandler.logout(httpRequest, null, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v2/logout")
    public RedirectView logoutV2(HttpServletRequest httpRequest) {
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        securityContextLogoutHandler.logout(httpRequest, null, authentication);
        return new RedirectView(userServiceDomain + "/v2/logout");
    }

}
