package gov.cabinetoffice.gap.adminbackend.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.dtos.MigrateUserDto;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.UserMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import gov.cabinetoffice.gap.adminbackend.utils.TestDecodedJwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { UserController.class, ControllerExceptionHandler.class })
@TestPropertySource(properties = { "feature.onelogin.enabled=true" }) // Set the value to
                                                                      // true or false as
                                                                      // needed
class UserControllerTest {

    @Resource
    private WebApplicationContext context;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void migrateUser_HappyPath() throws Exception {
        final MigrateUserDto migrateUserDto = MigrateUserDto.builder().colaSub(UUID.randomUUID())
                .oneLoginSub("oneLoginSub").build();
        final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
        when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/migrate").contentType(MediaType.APPLICATION_JSON)
                .content(HelperUtils.asJsonString(migrateUserDto)).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                .andExpect(status().isOk()).andReturn();
        verify(userService).migrateUser("oneLoginSub", migrateUserDto.getColaSub());
    }

    @Test
    void migrateUser_NoJwt() throws Exception {
        final MigrateUserDto migrateUserDto = MigrateUserDto.builder().colaSub(UUID.randomUUID())
                .oneLoginSub("oneLoginSub").build();
        final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
        when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/migrate").contentType(MediaType.APPLICATION_JSON)
                .content(HelperUtils.asJsonString(migrateUserDto)).header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isUnauthorized()).andReturn();
        verify(userService, times(0)).migrateUser("oneLoginSub", migrateUserDto.getColaSub());
    }

    @Test
    void migrateUser_InvalidJwt() throws Exception {
        final MigrateUserDto migrateUserDto = MigrateUserDto.builder().colaSub(UUID.randomUUID())
                .oneLoginSub("oneLoginSub").build();
        doThrow(new UnauthorizedException("Invalid JWT")).when(jwtService).verifyToken("jwt");

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/migrate").contentType(MediaType.APPLICATION_JSON)
                .content(HelperUtils.asJsonString(migrateUserDto)).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                .andExpect(status().isUnauthorized()).andReturn();
        verify(userService, times(0)).migrateUser("oneLoginSub", migrateUserDto.getColaSub());
    }

    @Test
    void migrateUser_JwtDoesNotMatchMUserToMigrate() throws Exception {
        final MigrateUserDto migrateUserDto = MigrateUserDto.builder().colaSub(UUID.randomUUID())
                .oneLoginSub("oneLoginSub").build();
        final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("anotherUsersOneLoginSub").build();
        when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/migrate").contentType(MediaType.APPLICATION_JSON)
                .content(HelperUtils.asJsonString(migrateUserDto)).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                .andExpect(status().isForbidden()).andReturn();
        verify(userService, times(0)).migrateUser("oneLoginSub", migrateUserDto.getColaSub());
    }

    @Test
    public void testValidateAdminSession() throws Exception {
        AdminSession adminSession = new AdminSession();
        adminSession.setEmailAddress("admin@example.com");
        adminSession.setRoles("[FIND, APPLY, ADMIN]");

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminSession);

        when(userService.verifyAdminRoles("admin@example.com", "[FIND, APPLY, ADMIN]")).thenReturn(Boolean.TRUE);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/validateAdminSession")).andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).verifyAdminRoles("admin@example.com", "[FIND, APPLY, ADMIN]");
    }

    @Test
    public void testValidateAdminSessionAuthenticationNotAuthenticated() throws Exception {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/validateAdminSession")).andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testValidateAdminSessionRolesDoNotMatch() throws Exception {
        AdminSession adminSession = new AdminSession();
        adminSession.setEmailAddress("admin@example.com");
        adminSession.setRoles("[FIND, APPLY, ADMIN]");

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminSession);

        doThrow(new UnauthorizedException("Roles do not match")).when(userService).verifyAdminRoles("admin@example.com",
                "[FIND, APPLY, ADMIN]");

        mockMvc.perform(MockMvcRequestBuilders.get("/users/validateAdminSession")).andExpect(status().isUnauthorized());
    }
}
