package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditor.SchemeEditorPostDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.services.SchemeEditorService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Optional;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SCHEME_ENTITY_EXAMPLE;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchemeEditorController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SchemeEditorController.class, ControllerExceptionHandler.class })
public class SchemeEditorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchemeEditorService schemeEditorService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserServiceConfig userServiceConfig;


    @Test
    public void testIsSchemeOwner_UserIsAdmin_ReturnsTrue() throws Exception {
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
        when(userService.getGrantAdminIdFromSub(session.getUserSub()))
                .thenReturn(Optional.of(GrantAdmin.builder().id(adminId).build()));
        when(schemeEditorService.doesAdminOwnScheme(schemeId, adminId)).thenReturn(true);
        mockMvc.perform(get("/schemes/" + schemeId + "/editors/isOwner"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsSchemeOwner_UserIsAdmin_ReturnsFalse() throws Exception {
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
        when(userService.getGrantAdminIdFromSub(session.getUserSub()))
                .thenReturn(Optional.of(GrantAdmin.builder().id(adminId).build()));
        when(schemeEditorService.doesAdminOwnScheme(schemeId, adminId)).thenReturn(false);
        mockMvc.perform(get("/schemes/" + schemeId + "/editors/isOwner"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testGetSchemeEditors_UserIsAdmin_ReturnsEditorDto() throws Exception {
        Authentication authentication = mock(Authentication.class);
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
        mockMvc.perform(get("/schemes/1/editors").cookie(new Cookie("cookieName", "jwt")))
                .andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(expectedResponse)));

    }

    @Test
    public void testGetSchemeEditors_SchemeEntityException() throws Exception {
        Authentication authentication = mock(Authentication.class);
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

        mockMvc.perform(get("/schemes/1/editors").cookie(new Cookie("cookieName", "jwt")))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetSchemeEditors_RestClientException() throws Exception {
        Authentication authentication = mock(Authentication.class);
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

        mockMvc.perform(get("/schemes/1/editors").cookie(new Cookie("cookieName", "jwt")))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetSchemeEditors_Illegal_state() throws Exception {
        Authentication authentication = mock(Authentication.class);
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

        mockMvc.perform(get("/schemes/1/editors").cookie(new Cookie("cookieName", "jwt")))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addEditorToScheme_HappyPath() throws Exception {
        SchemeEditorPostDTO schemeEditorPostDTO = SchemeEditorPostDTO.builder().editorEmailAddress("test@test.gov").build();
        when(userServiceConfig.getCookieName()).thenReturn("user-service-token");
        when(schemeEditorService.addEditorToScheme(1, "test@test.gov", "jwt")).thenReturn(SCHEME_ENTITY_EXAMPLE);
        mockMvc.perform(post("/schemes/1/editors")
                        .content(HelperUtils.asJsonString(schemeEditorPostDTO))
                        .cookie(new Cookie("user-service-token", "jwt"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void addEditorToScheme_isBadRequestWhenServiceMethodThrows() throws Exception {
        SchemeEditorPostDTO schemeEditorPostDTO = SchemeEditorPostDTO.builder().editorEmailAddress("test@test.gov").build();
        when(userServiceConfig.getCookieName()).thenReturn("user-service-token");
        when(schemeEditorService.addEditorToScheme(1, "test@test.gov", "jwt")).thenThrow(new FieldViolationException("editorEmailAddress", "editorEmailAddress is already an editor of this scheme"));
        mockMvc.perform(post("/schemes/1/editors")
                        .content(HelperUtils.asJsonString(schemeEditorPostDTO))
                        .cookie(new Cookie("user-service-token", "jwt"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
