package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.LambdaSecretConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringJUnitConfig
@WithAdminSession
class SchemeMapperTest {

    /*
        This looks a bit odd, but we need to implement the abstract class somewhere to have access
        to the method we want to unit test because it exists on the super class which can't be
        instantiated.
     */
    class TestSchemeMapperImpl extends SchemeMapper {

        @Override
        public List<SchemeDTO> schemeEntityListtoDtoList(List<SchemeEntity> schemeEntityList) {
            return null;
        }

        @Override
        public SchemeEntity schemeDtoToEntity(SchemeDTO schemeDto) {
            return null;
        }

        @Override
        public List<SchemeEntity> schemeDtoListToEntityList(List<SchemeDTO> schemeDto) {
            return null;
        }

        @Override
        public SchemeEntity schemePostDtoToEntity(SchemePostDTO schemePostDto) {
            return null;
        }

        @Override
        public List<SchemeEntity> schemePostDtoListToEntityList(List<SchemePostDTO> schemePostDtoList) {
            return null;
        }

        @Override
        public SchemePostDTO schemeEntityToPostDto(SchemeEntity schemeEntity) {
            return null;
        }

        @Override
        public List<SchemePostDTO> schemeEntityListToPostDtoList(List<SchemeEntity> schemeEntityList) {
            return null;
        }

        @Override
        public void updateSchemeEntityFromPatchDto(SchemePatchDTO schemePatchDto, SchemeEntity schemeEntity) {

        }
    }

    @Mock
    private UserService userService;

    @Mock
    private AwsEncryptionServiceImpl encryptionService;

    @Mock
    private LambdaSecretConfigProperties lambdaSecretConfigProperties;

    @Mock
    private UserServiceConfig userServiceConfig;

    @Mock
    private WebClient.Builder webClientBuilder;

    private TestSchemeMapperImpl schemeMapper;

    @BeforeEach
    void setup() {
        schemeMapper = new TestSchemeMapperImpl();

        schemeMapper.setUserService(userService);
        schemeMapper.setUserServiceConfig(userServiceConfig);
        schemeMapper.setEncryptionService(encryptionService);
        schemeMapper.setLambdaSecretConfigProperties(lambdaSecretConfigProperties);
        schemeMapper.setWebClientBuilder(webClientBuilder);
    }

    @Test
    void schemeEntityToDto_HandlesNullCreatedByFields() {
        final int grantAdminId = 1;
        final int createdBy = 50;
        final SchemeEntity scheme = SchemeEntity.builder()
                .id(grantAdminId)
                .ggisIdentifier("G2-12345-6789")
                .email("support@cabinetoffice.gov.uk")
                .name("Test Scheme 1")
                .funderId(2)
                .version(2)
                .createdBy(createdBy)
                .createdDate(Instant.now())
                .build();

        final GapUser user = GapUser.builder()
                .userSub("56743-12345-66543-1111")
                .build();

        final GrantAdmin grantAdmin = GrantAdmin.builder()
                .id(createdBy)
                .gapUser(user)
                .build();

        final String updatedBy = "thisNeedsReplaced@email.com";
        final byte[] encryptedEmail = updatedBy.getBytes();

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

        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<UserEmailResponseDto>>() {}))
                .thenReturn(
                        Mono.just(
                                Arrays.asList(UserEmailResponseDto.builder()
                                        .emailAddress(encryptedEmail)
                                        .build()
                                )
                        )
                );

        when(userService.getGrantAdminById(grantAdminId))
                .thenReturn(Optional.of(grantAdmin));

        when(encryptionService.decryptField(encryptedEmail))
                .thenReturn(updatedBy);

        when(userServiceConfig.getDomain())
                .thenReturn("https://user-service-domain");

        when(lambdaSecretConfigProperties.getSecret())
                .thenReturn("secret");

        // make sure we will end up going down the null "updated by" path
        assertThat(scheme.getLastUpdatedBy()).isNull();
        assertThat(scheme.getLastUpdatedBy()).isNull();

        final SchemeDTO schemeDto = schemeMapper.schemeEntityToDto(scheme);

        assertThat(schemeDto.getLastUpdatedBy()).isEqualTo(updatedBy);
        assertThat(schemeDto.getLastUpdatedDate()).isEqualTo(scheme.getCreatedDate());

        assertCommonFields(schemeDto, scheme);
    }

    @Test
    void schemeEntityToDto_HandlesExistingCreatedByFields() {
        final int grantAdminId = 1;
        final int createdBy = 50;
        final SchemeEntity scheme = SchemeEntity.builder()
                .id(grantAdminId)
                .ggisIdentifier("G2-12345-6789")
                .email("support@cabinetoffice.gov.uk")
                .name("Test Scheme 1")
                .funderId(2)
                .version(2)
                .createdBy(createdBy)
                .createdDate(Instant.now())
                .lastUpdatedBy(grantAdminId)
                .lastUpdated(Instant.now())
                .build();

        final GapUser user = GapUser.builder()
                .userSub("56743-12345-66543-1111")
                .build();

        final GrantAdmin grantAdmin = GrantAdmin.builder()
                .id(createdBy)
                .gapUser(user)
                .build();

        final String updatedBy = "thisNeedsReplaced@email.com";
        final byte[] encryptedEmail = updatedBy.getBytes();

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

        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<UserEmailResponseDto>>() {}))
                .thenReturn(
                        Mono.just(
                                Arrays.asList(UserEmailResponseDto.builder()
                                        .emailAddress(encryptedEmail)
                                        .build()
                                )
                        )
                );

        when(userService.getGrantAdminById(grantAdminId))
                .thenReturn(Optional.of(grantAdmin));

        when(encryptionService.decryptField(encryptedEmail))
                .thenReturn(updatedBy);

        when(userServiceConfig.getDomain())
                .thenReturn("https://user-service-domain");

        when(lambdaSecretConfigProperties.getSecret())
                .thenReturn("secret");

        // make sure we won't end up going down the null "updated by" path
        assertThat(scheme.getLastUpdatedBy()).isNotNull();
        assertThat(scheme.getLastUpdatedBy()).isNotNull();

        final SchemeDTO schemeDto = schemeMapper.schemeEntityToDto(scheme);

        assertThat(schemeDto.getLastUpdatedBy()).isEqualTo(updatedBy);
        assertThat(schemeDto.getLastUpdatedDate()).isEqualTo(scheme.getLastUpdated());

        assertCommonFields(schemeDto, scheme);
    }

    @Test
    void returnsNull_IfSchemeIsNull() {
        assertThat(schemeMapper.schemeEntityToDto(null)).isNull();
    }

    private void assertCommonFields(SchemeDTO dto,  SchemeEntity entity) {
        assertThat(dto.getSchemeId()).isEqualTo(entity.getId());
        assertThat(dto.getGgisReference()).isEqualTo(entity.getGgisIdentifier());
        assertThat(dto.getContactEmail()).isEqualTo(entity.getEmail());
        assertThat(dto.getName()).isEqualTo(entity.getName());
        assertThat(dto.getFunderId()).isEqualTo(entity.getFunderId());
        assertThat(dto.getVersion()).isEqualTo(entity.getVersion().toString());
        assertThat(dto.getCreatedBy()).isEqualTo(entity.getCreatedBy());
        assertThat(dto.getCreatedDate()).isEqualTo(entity.getCreatedDate());
    }
}