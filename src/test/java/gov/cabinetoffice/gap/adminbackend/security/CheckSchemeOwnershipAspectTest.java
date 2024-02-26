package gov.cabinetoffice.gap.adminbackend.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.config.JwtTokenFilterConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdvertRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormService;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.TestDecodedJwt;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckSchemeOwnershipAspectTest {

    private CheckSchemeOwnershipAspect checkSchemeOwnershipAspect;

    private @Mock GrantAdvertService grantAdvertService;
    private @Mock SchemeRepository schemeRepository;
    private @Mock GrantAdvertRepository grantAdvertRepository;
    private @Mock ApplicationFormRepository applicationFormRepository;

    @BeforeEach
    void setup() {
        checkSchemeOwnershipAspect = new CheckSchemeOwnershipAspect(schemeRepository, grantAdvertRepository,
                applicationFormRepository);
    }

    @Test
    void shouldReturnSchemeIdWhenSchemeIdIsInRequest() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        AdminSession adminSession = mock(AdminSession.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        SchemePatchDTO schemePatchDTO = new SchemePatchDTO("schemeName", "1234",
                "test@email.uk");
        int schemeId = 1;

        Object[] methodArgs = new Object[] { schemeId, schemePatchDTO };

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "schemeId", "scheme" });
        when(schemeRepository.findByIdAndCreatedBy(anyInt(), anyInt())).thenReturn(List.of(new SchemeEntity()));



        assertDoesNotThrow(()
                -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }
}
