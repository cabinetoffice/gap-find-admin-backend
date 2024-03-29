package gov.cabinetoffice.gap.adminbackend.security;

import gov.cabinetoffice.gap.adminbackend.config.JwtTokenFilterConfig;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class cannot be a Spring bean, otherwise Spring will automatically apply it to all
 * requests, regardless of whether they've been specifically ignored
 */
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UserService userService;

    private final JwtTokenFilterConfig jwtTokenFilterConfig;

    @Override
    protected void doFilterInternal(final @NotNull HttpServletRequest request,
            final @NotNull HttpServletResponse response, final @NotNull FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean skipSessionValidation = authentication == null || !authentication.isAuthenticated()
                || !jwtTokenFilterConfig.oneLoginEnabled || !jwtTokenFilterConfig.validateUserRolesInMiddleware;

        if (skipSessionValidation) {
            chain.doFilter(request, response);
            return;
        }

        AdminSession adminSession = ((AdminSession) authentication.getPrincipal());

        String emailAddress = adminSession.getEmailAddress();
        String roles = adminSession.getRoles();
        try {
            userService.verifyAdminRoles(emailAddress, roles);
            chain.doFilter(request, response);
        }
        catch (RestClientException | UnauthorizedException error) {
            SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
            securityContextLogoutHandler.logout(request, null, authentication);
            throw new UnauthorizedException("Payload is out of date");
        }
    }

}
