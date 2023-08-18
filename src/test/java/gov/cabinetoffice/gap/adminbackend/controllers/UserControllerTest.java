package gov.cabinetoffice.gap.adminbackend.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.dtos.MigrateUserDto;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.UserMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import gov.cabinetoffice.gap.adminbackend.utils.TestDecodedJwt;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { UserController.class, ControllerExceptionHandler.class })
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

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete/oneLoginSub")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isOk()).andReturn();

            verify(userService, times(1)).deleteUser("oneLoginSub", Optional.empty());
        }

        @Test
        void EmptyColaSubHappyPath() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete/oneLoginSub?colaSub=")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isOk()).andReturn();

            verify(userService, times(1)).deleteUser("oneLoginSub", Optional.empty());
        }

        @Test
        void ColaSubHappyPath() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            final UUID colaSub = UUID.randomUUID();
            final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete/oneLoginSub?colaSub=" + colaSub)
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isOk()).andReturn();

            verify(userService, times(1)).deleteUser("oneLoginSub", Optional.of(colaSub));
        }

        @Test
        void InvalidColaSub() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            final String colaSub = "not-a-uuid";
            final JwtPayload jwtPayload = JwtPayload.builder().roles("SUPER_ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete/oneLoginSub?colaSub=" + colaSub)
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isBadRequest()).andReturn();

            verify(userService, times(0)).deleteUser(anyString(), any());
        }

        @Test
        void NoJwt() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("oneLoginSub").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete/oneLoginSub")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ""))
                    .andExpect(status().isUnauthorized()).andReturn();
            verify(userService, times(0)).deleteUser("oneLoginSub", Optional.empty());
        }

        @Test
        void InvalidJwt() throws Exception {
            doThrow(new UnauthorizedException("Invalid JWT")).when(jwtService).verifyToken("jwt");

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete/oneLoginSub")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isUnauthorized()).andReturn();
            verify(userService, times(0)).deleteUser("oneLoginSub", Optional.empty());
        }

        @Test
        void NotSuperAdmin() throws Exception {
            final DecodedJWT decodedJWT = TestDecodedJwt.builder().subject("anotherUsersOneLoginSub").build();
            final JwtPayload jwtPayload = JwtPayload.builder().roles("ADMIN").build();
            when(jwtService.verifyToken("jwt")).thenReturn(decodedJWT);
            when(jwtService.getPayloadFromJwtV2(decodedJWT)).thenReturn(jwtPayload);

            mockMvc.perform(MockMvcRequestBuilders.delete("/users/delete/oneLoginSub")
                    .contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer jwt"))
                    .andExpect(status().isForbidden()).andReturn();
            verify(userService, times(0)).deleteUser("oneLoginSub", Optional.empty());
        }

    }

}
