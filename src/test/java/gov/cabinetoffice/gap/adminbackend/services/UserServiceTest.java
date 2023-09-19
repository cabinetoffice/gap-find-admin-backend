package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.Spy;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
class UserServiceTest {

    @Spy
    @InjectMocks
    private UserService userService;

    @Mock
    private GapUserRepository gapUserRepository;

    @Mock
    private GrantApplicantRepository grantApplicantRepository;

    @Mock
    private UserServiceConfig userServiceConfig;

    @Mock
    private RestTemplate restTemplate;

    private final String oneLoginSub = "oneLoginSub";

    private final UUID colaSub = UUID.randomUUID();

    @Test
    void migrateUserNoMatches() {
        when(gapUserRepository.findByUserSub(any())).thenReturn(Optional.empty());
        when(grantApplicantRepository.findByUserId(any())).thenReturn(Optional.empty());

        userService.migrateUser(oneLoginSub, colaSub);

        verify(gapUserRepository, times(0)).save(any());
        verify(grantApplicantRepository, times(0)).save(any());
    }

    @Test
    void migrateUserMatchesGapUser() {
        final GapUser gapUser = GapUser.builder().build();
        when(gapUserRepository.findByUserSub(any())).thenReturn(Optional.of(gapUser));
        when(grantApplicantRepository.findByUserId(any())).thenReturn(Optional.empty());

        userService.migrateUser(oneLoginSub, colaSub);
        gapUser.setUserSub(oneLoginSub);

        verify(gapUserRepository, times(1)).save(gapUser);
        verify(grantApplicantRepository, times(0)).save(any());
    }

    @Test
    void migrateUserMatchesGrantApplicant() {
        final GrantApplicant grantApplicant = GrantApplicant.builder().build();
        when(grantApplicantRepository.findByUserId(any())).thenReturn(Optional.of(grantApplicant));
        when(gapUserRepository.findByUserSub(any())).thenReturn(Optional.empty());

        userService.migrateUser(oneLoginSub, colaSub);
        grantApplicant.setUserId(oneLoginSub);

        verify(gapUserRepository, times(0)).save(any());
        verify(grantApplicantRepository, times(1)).save(grantApplicant);
    }

    @Test
    void migrateUserMatchesGrantApplicantAndGapUser() {
        final GrantApplicant grantApplicant = GrantApplicant.builder().build();
        final GapUser gapUser = GapUser.builder().build();
        when(grantApplicantRepository.findByUserId(any())).thenReturn(Optional.of(grantApplicant));
        when(gapUserRepository.findByUserSub(any())).thenReturn(Optional.of(gapUser));

        userService.migrateUser(oneLoginSub, colaSub);
        grantApplicant.setUserId(oneLoginSub);
        gapUser.setUserSub(oneLoginSub);

        verify(gapUserRepository, times(1)).save(gapUser);
        verify(grantApplicantRepository, times(1)).save(grantApplicant);
    }

    @Test
    public void testVerifyAdminRolesValid() {
        String emailAddress = "admin@example.com";
        String roles = "[FIND, APPLY, ADMIN]";
        String url = "http://example.com/v2/validateSessionsRoles";
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(true, HttpStatus.OK);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(responseEntity);
        when(userServiceConfig.getDomain()).thenReturn("http://example.com");

        userService.verifyAdminRoles(emailAddress, roles);
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class));
    }

    @Test
    public void testVerifyAdminRolesWhenUnauthorizedResponse() {
        String emailAddress = "admin@example.com";
        String roles = "[FIND, APPLY, ADMIN]";
        String url = "http://example.com/v2/validateSessionsRoles";
        HttpHeaders requestHeaders = new HttpHeaders();
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(responseEntity);
        when(userServiceConfig.getDomain()).thenReturn("http://example.com");

        assertThrows(UnauthorizedException.class, () -> userService.verifyAdminRoles(emailAddress, roles));
    }

}