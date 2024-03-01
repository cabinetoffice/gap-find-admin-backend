package gov.cabinetoffice.gap.adminbackend.security;

import gov.cabinetoffice.gap.adminbackend.dtos.DepartmentDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.CreateGrantAdvertDto;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdvertRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckSchemeOwnershipAspectTest {

    private CheckSchemeOwnershipAspect checkSchemeOwnershipAspect;;
    private @Mock SchemeRepository schemeRepository;
    private @Mock GrantAdvertRepository grantAdvertRepository;
    private @Mock ApplicationFormRepository applicationFormRepository;

    @BeforeEach
    void setup() {
        checkSchemeOwnershipAspect = new CheckSchemeOwnershipAspect(schemeRepository, grantAdvertRepository,
                applicationFormRepository);
    }

    @Test
    void shouldNotThrowWhenValidSchemeIdIsInRequest() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        SchemePatchDTO schemePatchDTO = new SchemePatchDTO("schemeName", "1234",
                "test@email.uk");
        int schemeId = 1;
        SchemeEntity schemeEntity = SchemeEntity.builder()
                .id(1)
                .grantAdmins(List.of(GrantAdmin.builder().id(1).build()))
                .build();

        Object[] methodArgs = new Object[] { schemeId, schemePatchDTO };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "schemeId", "scheme" });
        when(schemeRepository.findById(anyInt())).thenReturn(Optional.of(schemeEntity));

        assertDoesNotThrow(()
                -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldNotThrowWhenValidDtoIsInRequest() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        CreateGrantAdvertDto createGrantAdvertDto = CreateGrantAdvertDto.builder().grantSchemeId(1).build();

        SchemeEntity schemeEntity = SchemeEntity.builder()
                .id(1)
                .grantAdmins(List.of(GrantAdmin.builder().id(1).build()))
                .build();

        Object[] methodArgs = new Object[] { createGrantAdvertDto };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "createGrantAdvertDto" });
        when(schemeRepository.findById(anyInt())).thenReturn(Optional.of(schemeEntity));

        assertDoesNotThrow(()
                -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInvalidDtoIdIsGiven() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        DepartmentDto invalidDto = new DepartmentDto(1, "invalidDto", "123");

        Object[] methodArgs = new Object[] { invalidDto };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "invalidDto" });

        assertThrows(IllegalArgumentException.class,
                () -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInvalidSchemeIdIsGiven() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        UUID schemeId = UUID.randomUUID();
        Object[] methodArgs = new Object[] { schemeId };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "schemeId" });

        assertThrows(IllegalArgumentException.class,
                () -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldNotThrowWhenValidStringSchemeIdIsInRequest() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        SchemePatchDTO schemePatchDTO = new SchemePatchDTO("schemeName", "1234",
                "test@email.uk");
        String schemeId = "1";
        SchemeEntity schemeEntity = SchemeEntity.builder()
                .id(1)
                .grantAdmins(List.of(GrantAdmin.builder().id(1).build()))
                .build();

        Object[] methodArgs = new Object[] { schemeId, schemePatchDTO };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "schemeId", "scheme" });
        when(schemeRepository.findById(anyInt())).thenReturn(Optional.of(schemeEntity));

        assertDoesNotThrow(()
                -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldNotThrowWhenValidApplicationIdIsInRequest() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        int applicationId = 1;
        SchemeEntity schemeEntity = SchemeEntity.builder()
                .id(1)
                .grantAdmins(List.of(GrantAdmin.builder().id(1).build()))
                .build();

        Object[] methodArgs = new Object[] { applicationId };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        ApplicationFormEntity applicationFormEntity = ApplicationFormEntity.builder().grantSchemeId(1).build();
        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "applicationId" });
        when(applicationFormRepository.findById(any()))
                .thenReturn(Optional.of(applicationFormEntity));
        when(schemeRepository.findById(anyInt())).thenReturn(Optional.of(schemeEntity));

        assertDoesNotThrow(()
                -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInvalidApplicationIdIsGiven() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        UUID inValidApplicationId = UUID.randomUUID();

        Object[] methodArgs = new Object[] { inValidApplicationId };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "applicationId" });

        assertThrows(IllegalArgumentException.class,
                () -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }


    @Test
    void shouldNotThrowWhenValidAdvertIdIsInRequest() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        UUID grantAdvertId = UUID.randomUUID();
        SchemeEntity schemeEntity = SchemeEntity.builder()
                .id(1)
                .grantAdmins(List.of(GrantAdmin.builder().id(1).build()))
                .build();

        Object[] methodArgs = new Object[] { grantAdvertId };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "grantAdvertId" });
        when(grantAdvertRepository.findById(any()))
                .thenReturn(Optional.of(GrantAdvert.builder().scheme(schemeEntity).build()));
        when(schemeRepository.findById(anyInt())).thenReturn(Optional.of(schemeEntity));

        assertDoesNotThrow(()
                -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInvalidAdvertIdIsGiven() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        String grantAdvertId = "invalidAdvertId";

        Object[] methodArgs = new Object[] { grantAdvertId };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "grantAdvertId" });

        assertThrows(IllegalArgumentException.class,
                () -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }


    @Test
    void shouldThrowNotFoundExceptionWhenSchemeDoesNotExist() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        SchemePatchDTO schemePatchDTO = new SchemePatchDTO("schemeName", "1234",
                "test@email.uk");
        int schemeId = 1;

        Object[] methodArgs = new Object[] { schemeId, schemePatchDTO };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "schemeId", "scheme" });
        when(schemeRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenAdminIdDoesNotMatchScheme() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        SchemeEntity schemeEntity = SchemeEntity.builder()
                .id(1)
                .grantAdmins(List.of(GrantAdmin.builder().id(2).build()))
                .build();

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        SchemePatchDTO schemePatchDTO = new SchemePatchDTO("schemeName", "1234",
                "test@email.uk");
        int schemeId = 1;

        Object[] methodArgs = new Object[] { schemeId, schemePatchDTO };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "schemeId", "scheme" });
        when(schemeRepository.findById(anyInt())).thenReturn(Optional.of(schemeEntity));

        assertThrows(AccessDeniedException.class,
                () -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUnexpectedMethodArgIsGiven() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        JoinPoint joinPoint = mock(JoinPoint.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        CheckSchemeOwnership checkSchemeOwnership = mock(CheckSchemeOwnership.class);

        SchemePatchDTO schemePatchDTO = new SchemePatchDTO("schemeName", "1234",
                "test@email.uk");
        int schemeId = 1;

        Object[] methodArgs = new Object[] { schemeId, schemePatchDTO };

        AdminSession adminSession = new AdminSession();
        adminSession.setGrantAdminId(1);

        when(authentication.getPrincipal()).thenReturn(adminSession);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[] { "invalidArg"});

        assertThrows(IllegalArgumentException.class,
                () -> checkSchemeOwnershipAspect.checkSchemeOwnershipBefore(joinPoint, checkSchemeOwnership));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUnexpectedDtoIsGiven() {
        DepartmentDto invalidDto = new DepartmentDto(1, "invalidDto", "123");

        assertThrows(IllegalArgumentException.class,
                () -> checkSchemeOwnershipAspect.extractGrantSchemeIdFromDto(invalidDto));
    }

}
