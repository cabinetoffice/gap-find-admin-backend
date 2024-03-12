package gov.cabinetoffice.gap.adminbackend.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.MigrateUserDto;
import gov.cabinetoffice.gap.adminbackend.dtos.UpdateFundingOrgDto;
import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.UserMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import gov.cabinetoffice.gap.adminbackend.services.SchemeService;
import gov.cabinetoffice.gap.adminbackend.services.TechSupportUserService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import gov.cabinetoffice.gap.adminbackend.utils.TestDecodedJwt;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { UserController.class, ControllerExceptionHandler.class })
@TestPropertySource(properties = { "feature.onelogin.enabled=true" })
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

    @MockBean
    private UserServiceConfig userServiceConfig;

    @MockBean
    private SchemeService schemeService;

    @MockBean
    private TechSupportUserService techSupportService;

    @Nested
    class MigrateUser {

        @Test
        void HappyPath() throws Exception {
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
        void NoJwt() throws Exception {
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
        void InvalidJwt() throws Exception {
            final MigrateUserDto migrateUserDto = MigrateUserDto.builder().colaSub(UUID.randomUUID())
                    .oneLoginSub("oneLoginSub").build();
            doThrow(new UnauthorizedException("Invalid JWT")).when(jwtService).verifyToken("jwt");

            mockMvc.perform(MockMvcRequestBuilders.patch("/users/migrate").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonString(migrateUserDto)).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isUnauthorized()).andReturn();
            verify(userService, times(0)).migrateUser("oneLoginSub", migrateUserDto.getColaSub());
        }

        @Test
        void JwtDoesNotMatchMUserToMigrate() throws Exception {
            final MigrateUserDto migrateUserDto = MigrateUserDto.builder().colaSub(UUID.randomUUID())
                    .oneLoginSub("oneLoginSub").build();
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("anotherUsersOneLoginSub").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);

            mockMvc.perform(MockMvcRequestBuilders.patch("/users/migrate").contentType(MediaType.APPLICATION_JSON)
                    .content(HelperUtils.asJsonString(migrateUserDto)).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isForbidden()).andReturn();
            verify(userService, times(0)).migrateUser("oneLoginSub", migrateUserDto.getColaSub());
        }

    }

    @Nested
    class DeleteUser {

        @Test
        void NoColaSubHappyPath() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete?oneLoginSub=oneLoginSub")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isOk()).andReturn();

            verify(userService, times(1)).deleteUser(Optional.of("oneLoginSub"), Optional.empty());
        }

        @Test
        void EmptyColaSubHappyPath() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete?oneLoginSub=oneLoginSub&colaSub=")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isOk()).andReturn();

            verify(userService, times(1)).deleteUser(Optional.of("oneLoginSub"), Optional.empty());
        }

        @Test
        void ColaSubHappyPath() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            final UUID colaSub = UUID.randomUUID();
            final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete?colaSub=072351bf-0789-42bb-8000-2c61b7e90d48")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isOk()).andReturn();

            verify(userService, times(1)).deleteUser(Optional.empty(),
                    Optional.of(UUID.fromString("072351bf-0789-42bb-8000-2c61b7e90d48")));
        }

        @Test
        void InvalidColaSub() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            final String colaSub = "not-a-uuid";
            final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete/?colaSub=" + colaSub)
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isBadRequest()).andReturn();

            verify(userService, times(0)).deleteUser(any(), any());
        }

        @Test
        void NoJwt() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete?oneLoginSub=oneLoginSub")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ""))
                    .andExpect(status().isUnauthorized()).andReturn();
            verify(userService, times(0)).deleteUser(Optional.of("oneLoginSub"), Optional.empty());
        }

        @Test
        void InvalidJwt() throws Exception {
            doThrow(new UnauthorizedException("Invalid JWT")).when(jwtService).verifyToken("jwt");

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete?oneLoginSub=oneLoginSub")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isUnauthorized()).andReturn();
            verify(userService, times(0)).deleteUser(Optional.of("oneLoginSub"), Optional.empty());
        }

        @Test
        void NotSuperAdmin() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("anotherUsersOneLoginSub").build();
            final JwtPayload jwtPayload = JwtPayload.builder().roles("ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete?oneLoginSub=oneLoginSub")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isForbidden()).andReturn();
            verify(userService, times(0)).deleteUser(Optional.of("oneLoginSub"), Optional.empty());
        }

    }

    @Test
    void testValidateAdminSession() throws Exception {
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
    void testValidateAdminSessionAuthenticationNotAuthenticated() throws Exception {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/validateAdminSession")).andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testValidateAdminSessionRolesDoNotMatch() throws Exception {
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

    @Test
    void shouldChangeFundingOrgWhenValidSubIsGiven() throws Exception {
        final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
        final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
        when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
        when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);
        when(userService.getGrantAdminIdFromSub(anyString())).thenReturn(
                Optional.of(GrantAdmin.builder().id(1).funder(FundingOrganisation.builder().id(1).build()).build()));
        Mockito.doNothing().when(userService).updateFundingOrganisation(any(GrantAdmin.class), anyString());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writeValueAsString(new UpdateFundingOrgDto("oneLoginSub", "test@email.gov", "newFundingOrg"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/funding-organisation").content(requestBody)
                .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                .andExpect(status().isOk()).andReturn();

        verify(userService, times(1)).getGrantAdminIdFromSub(anyString());
        verify(userService, times(1)).updateFundingOrganisation(any(GrantAdmin.class), anyString());

    }

    @Test
    void shouldReturn404WhenInvalidSubIsGiven() throws Exception {
        final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
        final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
        when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
        when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);
        when(userService.getGrantAdminIdFromSub(anyString())).thenReturn(Optional.empty());
        Mockito.doNothing().when(userService).updateFundingOrganisation(any(GrantAdmin.class), anyString());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
                .writeValueAsString(new UpdateFundingOrgDto("oneLoginSub", "test@email.gov", "newFundingOrg"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/funding-organisation").content(requestBody)
                .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                .andExpect(status().isNotFound()).andReturn();
    }

}
