package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.LambdaSecretConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@SpringJUnitConfig
@WithAdminSession
class SchemeMapperTest {

    // Need to implement the abstract class somewhere to test the manually overriden method schemeEntityToDto
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

    private TestSchemeMapperImpl schemeMapper;

    @BeforeEach
    void setup() {
        schemeMapper = new TestSchemeMapperImpl();

        schemeMapper.setUserService(userService);
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

        when(userService.getGrantAdminById(createdBy))
                .thenReturn(Optional.of(grantAdmin));

        when(encryptionService.decryptField(encryptedEmail))
                .thenReturn(updatedBy);

        when(userServiceConfig.getDomain())
                .thenReturn("https://user-service-domain");

        when(lambdaSecretConfigProperties.getSecret())
                .thenReturn("secret");

        when(userService.getEmailAddressForSub(user.getUserSub()))
                .thenReturn(updatedBy);

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

        when(userService.getGrantAdminById(grantAdminId))
                .thenReturn(Optional.of(grantAdmin));

        when(encryptionService.decryptField(encryptedEmail))
                .thenReturn(updatedBy);

        when(userServiceConfig.getDomain())
                .thenReturn("https://user-service-domain");

        when(lambdaSecretConfigProperties.getSecret())
                .thenReturn("secret");

        when(userService.getEmailAddressForSub(user.getUserSub()))
                .thenReturn(updatedBy);

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