package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.UserV2DTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Mock
    private AwsEncryptionServiceImpl encryptionService;

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
        void shouldDeleteAdminFromGapUserAndGrantAdminRepositories() {
            userService.deleteAdminUser(oneLoginSub);

            verify(grantAdminRepository, times(1)).deleteByGapUserUserSub(any());
            verify(gapUserRepository, times(1)).deleteByUserSub(any());
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
    void testVerifyAdminRolesValid() {
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
    void testVerifyAdminRolesWhenUnauthorizedResponse() {
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
    void getGrantAdminIdFromEmailReturnsAValidGrantAdminId() {
        String email = "test@test.com";
        UserV2DTO response = UserV2DTO.builder().sub("1").emailAddress(email).build();

        final WebClient mockWebClient = mock(WebClient.class);
        final WebClient.RequestHeadersUriSpec mockRequestHeaderUriSpec = Mockito
                .mock(WebClient.RequestHeadersUriSpec.class);
        final WebClient.RequestHeadersSpec mockRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final WebClient.ResponseSpec mockResponseSpec = mock(WebClient.ResponseSpec.class);

        when(userServiceConfig.getDomain()).thenReturn("http://localhost:8080");
        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.get()).thenReturn(mockRequestHeaderUriSpec);
        when(mockRequestHeaderUriSpec.uri("http://localhost:8080/user/email/" + email + "?role=ADMIN"))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.cookie(anyString(), anyString())).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(UserV2DTO.class)).thenReturn(Mono.just(response));
        when(userServiceConfig.getCookieName()).thenReturn("user-service-token");

        when(grantAdminRepository.findByGapUserUserSub(response.sub())).thenReturn(
                Optional.of(GrantAdmin.builder().id(1).funder(FundingOrganisation.builder().id(1).build()).build()));

        GrantAdmin grantAdminId = userService.getGrantAdminIdFromUserServiceEmail(email, "jwt");

        assertThat(grantAdminId.getFunder().getId()).isEqualTo(1);
    }

    @Test
    void getGrantAdminById_Success() {
        final Integer userId = 1;
        final GrantAdmin admin = GrantAdmin.builder()
                .id(userId)
                .build();

        when(grantAdminRepository.findById(userId))
                .thenReturn(Optional.of(admin));

        final Optional<GrantAdmin> result = userService.getGrantAdminById(userId);

        verify(grantAdminRepository).findById(userId);
        result.ifPresentOrElse(
                grantAdmin -> assertThat(grantAdmin).isEqualTo(admin),
                () -> fail("Grant Admin not present, something is broken")
        );
    }

    @Test
    void getEmailAddressForSub_Success() {
        final String userSub = "56743-12345-66543-1111";
        final byte[] encryptedEmail = "an-encrypted-email".getBytes();

        final WebClient webClient = mock(WebClient.class);
        final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<UserEmailResponseDto>>() {}))
                .thenReturn(
                        Mono.just(
                                Arrays.asList(UserEmailResponseDto.builder()
                                        .emailAddress(encryptedEmail)
                                        .build()
                                )
                        )
                );

        final byte[] emailAddress = userService.getEmailAddressForSub(userSub);

        assertThat(emailAddress).isEqualTo(encryptedEmail);
    }
}