package gov.cabinetoffice.gap.adminbackend.security;

import gov.cabinetoffice.gap.adminbackend.config.JwtTokenFilterConfig;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    private JwtTokenFilter jwtTokenFilter;

    private @Mock UserService userService;

    private @Mock JwtTokenFilterConfig jwtTokenFilterConfig;

    @BeforeEach
    void setup() {
        jwtTokenFilterConfig.oneLoginEnabled = true;
        jwtTokenFilterConfig.validateUserRolesInMiddleware = true;
        jwtTokenFilter = new JwtTokenFilter(userService, jwtTokenFilterConfig);
    }

    @Test
    void Authenticates_when_TokenIsValid() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        final SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        JwtPayload payload = new JwtPayload();
        payload.setEmailAddress("test@example.com");
        payload.setRoles("ADMIN");

        AdminSession adminSession = new AdminSession(1, 1, payload);
        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(userService.verifyAdminRoles(eq("test@example.com"))).thenReturn(Boolean.TRUE);

        jwtTokenFilter.doFilterInternal(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);
        verify(userService, times(1)).verifyAdminRoles("test@example.com");
    }

    @Test
    void verifyAdminRolesThrowsUnauthorizedException_when_PayloadIsInvalid() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        JwtPayload payload = new JwtPayload();
        payload.setEmailAddress("test@example.com");
        payload.setRoles("ADMIN");

        AdminSession adminSession = new AdminSession(1, 1, payload);
        when(authentication.getPrincipal()).thenReturn(adminSession);
        doThrow(UnauthorizedException.class).when(userService).verifyAdminRoles(eq("test@example.com"));

        verify(chain, times(0)).doFilter(request, response);
        assertThrows(UnauthorizedException.class, () -> jwtTokenFilter.doFilterInternal(request, response, chain));
    }

}
