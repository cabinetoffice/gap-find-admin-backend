package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.security.CognitoAuthManager;
import gov.cabinetoffice.gap.adminbackend.security.WebSecurityConfig;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@ContextConfiguration(classes = { LoginController.class, CognitoAuthManager.class, WebSecurityConfig.class,
        ControllerExceptionHandler.class })
class LoginControllerTest {

    @MockBean
    private AuthenticationManager authenticationManager;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Resource
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).apply(springSecurity()).build();
    }

    @Test
    void SuccessfulLoginTest() throws Exception {
        Mockito.when(this.authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(
                new AdminSession(1, 1, "Test", "User", "AND Digital", "test@domain.com"), null));

        this.mockMvc.perform(post("/login")).andExpect(status().isOk());
    }

    @Test
    void FailedLoginTest() throws Exception {
        Mockito.when(this.authenticationManager.authenticate(any()))
                .thenThrow(new UnauthorizedException("Token is not valid"));

        this.mockMvc.perform(post("/login")).andExpect(status().isUnauthorized())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO("Token is not valid"))));
    }

    @Test
    @WithAdminSession
    void SuccessfulLogoutTest() throws Exception {
        this.mockMvc.perform(delete("/logout")).andExpect(status().isNoContent());
    }

    @Test
    void LogoutWithNoSessionTest() throws Exception {
        this.mockMvc.perform(delete("/logout")).andExpect(status().isUnauthorized());
    }

}
