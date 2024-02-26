package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.services.SchemeEditorService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SchemeEditorControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SchemeEditorService schemeEditorService;

    @InjectMocks
    private SchemeEditorController schemeEditorController;

    @Mock
    private UserServiceConfig userServiceConfig;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIsSchemeOwner_UserNotAdmin_ThrowsUnauthorizedException() {
        when(userServiceConfig.getCookieName()).thenReturn("cookieName");
        Integer schemeId = 1;
        AdminSession session = new AdminSession();
        when(userService.getGrantAdminIdFromSub(session.getUserSub())).thenReturn(Optional.empty());

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        AdminSession adminSession = new AdminSession(1, 1, JwtPayload.builder().build());
        when(authentication.getPrincipal()).thenReturn(adminSession);
        assertThrows(UnauthorizedException.class, () -> schemeEditorController.isSchemeOwner(schemeId));
        verify(schemeEditorService, never()).doesAdminOwnScheme(anyInt(), anyInt());
    }

    @Test
    public void testIsSchemeOwner_UserIsAdmin_ReturnsTrue() {
        SecurityContext securityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);
        Authentication authentication = mock(Authentication.class);
        AdminSession adminSession = new AdminSession(1, 1, JwtPayload.builder().build());
        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        Integer schemeId = 1;
        Integer adminId = 1;
        AdminSession session = new AdminSession();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(userService.getGrantAdminIdFromSub(session.getUserSub()))
                .thenReturn(Optional.of(GrantAdmin.builder().id(adminId).build()));
        when(schemeEditorService.doesAdminOwnScheme(schemeId, adminId)).thenReturn(true);

        ResponseEntity<Boolean> responseEntity = schemeEditorController.isSchemeOwner(schemeId);
        assertEquals(Boolean.TRUE, responseEntity.getBody());
    }

    @Test
    public void testIsSchemeOwner_UserIsAdmin_ReturnsFalse() {
        SecurityContext securityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);
        Authentication authentication = mock(Authentication.class);
        AdminSession adminSession = new AdminSession(1, 1, JwtPayload.builder().build());
        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        Integer schemeId = 1;
        Integer adminId = 1;
        AdminSession session = new AdminSession();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(userService.getGrantAdminIdFromSub(session.getUserSub()))
                .thenReturn(Optional.of(GrantAdmin.builder().id(adminId).build()));
        when(schemeEditorService.doesAdminOwnScheme(schemeId, adminId)).thenReturn(true);

        ResponseEntity<Boolean> responseEntity = schemeEditorController.isSchemeOwner(schemeId);
        assertEquals(Boolean.TRUE, responseEntity.getBody());
    }

    @Test
    public void testGetSchemeEditors_UserIsAdmin_ReturnsEditorDto() {
        Authentication authentication = mock(Authentication.class);
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("cookieName", "cookieValue") });
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userServiceConfig.getCookieName()).thenReturn("cookieName");
        AdminSession adminSession = new AdminSession(1, 1, JwtPayload.builder().build());
        GrantAdmin grantAdmin = GrantAdmin.builder().gapUser(
                GapUser.builder().userSub("sub").build()).id(1).build();
        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(userService.getGrantAdminIdFromSub(any())).thenReturn(Optional.of(grantAdmin));
        List<SchemeEditorsDTO> expectedResponse = List.of(SchemeEditorsDTO.builder().build());
        when(schemeEditorService.getEditorsFromSchemeId(anyInt(), anyString())).thenReturn(expectedResponse);
        ResponseEntity<List<SchemeEditorsDTO>> responseEntity = schemeEditorController
                .getSchemeEditors(1, request);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    public void testGetSchemeEditors_UserIsNotAdmin_ThrowsUnauthorizedException() {
        Authentication authentication = mock(Authentication.class);
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("cookieName", "cookieValue") });
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userServiceConfig.getCookieName()).thenReturn("cookieName");
        AdminSession adminSession = new AdminSession(1, 1, JwtPayload.builder().build());
        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(userService.getGrantAdminIdFromSub(any())).thenReturn(Optional.empty());

        Assertions.assertThrows(UnauthorizedException.class, () -> {
            schemeEditorController.getSchemeEditors(1, request);
        });
    }

    @Test
    public void testGetSchemeEditors_SchemeEntityException() {
        Authentication authentication = mock(Authentication.class);
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("cookieName", "cookieValue") });
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userServiceConfig.getCookieName()).thenReturn("cookieName");
        AdminSession adminSession = new AdminSession(1, 1, JwtPayload.builder().build());
        when(authentication.getPrincipal()).thenReturn(adminSession);
        GrantAdmin grantAdmin = GrantAdmin.builder().gapUser(
                GapUser.builder().userSub("sub").build()).id(1).build();
        when(userService.getGrantAdminIdFromSub(any())).thenReturn(Optional.of(grantAdmin));
        when(schemeEditorService.getEditorsFromSchemeId(anyInt(), anyString()))
                .thenThrow(new SchemeEntityException("Test SchemeEntityException"));

        Assertions.assertThrows(SchemeEntityException.class, () -> {
            schemeEditorController.getSchemeEditors(1, request);
        });
    }

    @Test
    public void testGetSchemeEditors_RestClientException() {
        Authentication authentication = mock(Authentication.class);
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("cookieName", "cookieValue") });
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userServiceConfig.getCookieName()).thenReturn("cookieName");
        AdminSession adminSession = new AdminSession(1, 1, JwtPayload.builder().build());
        when(authentication.getPrincipal()).thenReturn(adminSession);
        GrantAdmin grantAdmin = GrantAdmin.builder().gapUser(
                GapUser.builder().userSub("sub").build()).id(1).build();
        when(userService.getGrantAdminIdFromSub(any())).thenReturn(Optional.of(grantAdmin));
        when(schemeEditorService.getEditorsFromSchemeId(anyInt(), anyString()))
                .thenThrow(new RestClientException(""));

        Assertions.assertThrows(RestClientException.class, () -> {
            schemeEditorController.getSchemeEditors(1, request);
        });
    }

    @Test
    public void testGetSchemeEditors_Illegal_state() {
        Authentication authentication = mock(Authentication.class);
        when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("cookieName", "cookieValue") });
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userServiceConfig.getCookieName()).thenReturn("cookieName");
        AdminSession adminSession = new AdminSession(1, 1, JwtPayload.builder().build());
        when(authentication.getPrincipal()).thenReturn(adminSession);
        GrantAdmin grantAdmin = GrantAdmin.builder().gapUser(
                GapUser.builder().userSub("sub").build()).id(1).build();
        when(userService.getGrantAdminIdFromSub(any())).thenReturn(Optional.of(grantAdmin));
        when(schemeEditorService.getEditorsFromSchemeId(anyInt(), anyString()))
                .thenThrow(new IllegalStateException(""));

        Assertions.assertThrows(IllegalStateException.class, () -> {
            schemeEditorController.getSchemeEditors(1, request);
        });
    }


}
