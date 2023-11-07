package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.UserV2DTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private GrantAdminRepository grantAdminRepository;

    private final String oneLoginSub = "oneLoginSub";

    private final UUID colaSub = UUID.randomUUID();

    @Nested
    class MigrateUser {

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

    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUserNoColaSub() {
            userService.deleteUser(Optional.of(oneLoginSub), Optional.empty());

            verify(grantApplicantRepository, times(1)).deleteByUserId(oneLoginSub);
            verify(grantApplicantRepository, times(0)).deleteByUserId(colaSub.toString());
        }

        @Test
        void deleteUserColaSubAndOLSub() {
            userService.deleteUser(Optional.of(oneLoginSub), Optional.of(colaSub));

            verify(grantApplicantRepository, times(1)).deleteByUserId(oneLoginSub);
            verify(grantApplicantRepository, times(1)).deleteByUserId(colaSub.toString());
        }

        @Test
        void deleteUserNoOLSub() {
            userService.deleteUser(Optional.empty(), Optional.of(colaSub));

            verify(grantApplicantRepository, times(0)).deleteByUserId(oneLoginSub);
            verify(grantApplicantRepository, times(1)).deleteByUserId(colaSub.toString());
        }

        @Test
        void deleteUserNoColaSubOrOLSub() {
            userService.deleteUser(Optional.empty(), Optional.empty());

            verify(grantApplicantRepository, times(0)).deleteByUserId(oneLoginSub);
            verify(grantApplicantRepository, times(0)).deleteByUserId(colaSub.toString());
        }

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

    @Test
    public void getGrantAdminIdFromEmailReturnsAValidGrantAdminId() {
        String email = "test@test.com";
        String userServiceUrl = "http://localhost:8082";
        UserV2DTO response = UserV2DTO.builder().sub("1").emailAddress(email).build();

        final WebClient mockWebClient = mock(WebClient.class);
        final WebClient.RequestHeadersUriSpec mockRequestHeaderUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        final WebClient.RequestHeadersSpec mockRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final WebClient.ResponseSpec mockResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.get()).thenReturn(mockRequestHeaderUriSpec);
        when(mockRequestHeaderUriSpec.uri(userServiceUrl + "/user/email/" + email)).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(UserV2DTO.class)).thenReturn(Mono.just(response));

        when(grantAdminRepository.findByGapUserUserSub(response.sub())).thenReturn(Optional.of(GrantAdmin.builder().id(1).build()));

        int grantAdminId = userService.getGrantAdminIdFromUserServiceEmail(email);
        assert(grantAdminId == 1);
    }

}