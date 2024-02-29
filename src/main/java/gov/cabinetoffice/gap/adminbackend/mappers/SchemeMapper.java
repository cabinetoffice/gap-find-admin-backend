package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.config.LambdaSecretConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Primary
@Slf4j
@Mapper(componentModel = "spring")
public abstract class SchemeMapper {

    public static final String EMPTY_EMAIL_VALUE = "-";

    private UserService userService;
    private AwsEncryptionServiceImpl encryptionService;
    private LambdaSecretConfigProperties lambdaSecretConfigProperties;
    private UserServiceConfig userServiceConfig;
    private WebClient.Builder webClientBuilder;

    @Autowired
    public final void setUserService(final UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public final void setEncryptionService(AwsEncryptionServiceImpl encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Autowired
    public final void setWebClientBuilder(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Autowired
    public final void setLambdaSecretConfigProperties(LambdaSecretConfigProperties lambdaSecretConfigProperties) {
        this.lambdaSecretConfigProperties = lambdaSecretConfigProperties;
    }

    @Autowired
    public final void setUserServiceConfig(UserServiceConfig userServiceConfig) {
        this.userServiceConfig = userServiceConfig;
    }

    public SchemeDTO schemeEntityToDto(SchemeEntity schemeEntity) {
        if ( schemeEntity == null ) {
            return null;
        }

        SchemeDTO.SchemeDTOBuilder schemeDTO = SchemeDTO.builder();

        schemeDTO.schemeId( schemeEntity.getId() );
        schemeDTO.ggisReference( schemeEntity.getGgisIdentifier() );
        schemeDTO.contactEmail( schemeEntity.getEmail() );
        schemeDTO.funderId( schemeEntity.getFunderId() );
        schemeDTO.name( schemeEntity.getName() );
        if ( schemeEntity.getVersion() != null ) {
            schemeDTO.version( String.valueOf( schemeEntity.getVersion() ) );
        }
        schemeDTO.createdDate( schemeEntity.getCreatedDate() );
        schemeDTO.createdBy( schemeEntity.getCreatedBy() );

        // Slightly strange logic here but if "last updated by" is null then we want to return "created by" instead.
        // Considering the above, it doesn't make sense to potentially return mismatched updaters/update dates
        // SO, if last updated by is not null then return this pair of values, otherwise return created by and created date
        final boolean isLastUpdatedBySet = schemeEntity.getLastUpdatedBy() != null;
        final int lastUpdatedBy  = isLastUpdatedBySet ? schemeEntity.getLastUpdatedBy() : schemeEntity.getCreatedBy();
        final String lastUpdatedByEmail = userService.getGrantAdminById(lastUpdatedBy)
                .map(admin -> {
                    final String sub = admin.getGapUser().getUserSub();
                    return getEmailAddressForSub(sub);
                })
                .orElse(EMPTY_EMAIL_VALUE); // Should literally never end up in here but would rather display a blank value than throw an error
        final Instant lastUpdatedDate = isLastUpdatedBySet ? schemeEntity.getLastUpdated() : schemeEntity.getCreatedDate();

        System.out.println("last updated by: " + lastUpdatedByEmail);
        System.out.println("last updated on: " + lastUpdatedDate);

        schemeDTO.lastUpdatedBy(lastUpdatedByEmail);
        schemeDTO.lastUpdatedDate(lastUpdatedDate);

        return schemeDTO.build();
    }

    private String getEmailAddressForSub(final String sub) {
        final String url = userServiceConfig.getDomain() + "/users/emails";
        final ParameterizedTypeReference<List<UserEmailResponseDto>> responseType = new ParameterizedTypeReference<>() {};

        final List<UserEmailResponseDto> response = webClientBuilder.build()
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(List.of(sub)))
                .header("Authorization", lambdaSecretConfigProperties.getSecret())
                .retrieve()
                .bodyToMono(responseType)
                .block();

        return Optional.ofNullable(response)
                .orElse(Collections.emptyList())
                .stream()
                .map(userEmailDto -> encryptionService.decryptField(userEmailDto.emailAddress()))
                .findFirst()
                .orElse(EMPTY_EMAIL_VALUE);
    }

    public abstract List<SchemeDTO> schemeEntityListtoDtoList(List<SchemeEntity> schemeEntityList);

    public abstract SchemeEntity schemeDtoToEntity(SchemeDTO schemeDto);

    public abstract List<SchemeEntity> schemeDtoListToEntityList(List<SchemeDTO> schemeDto);

    @Mapping(target = "ggisIdentifier", source = "ggisReference")
    @Mapping(target = "email", source = "contactEmail")
    public abstract SchemeEntity schemePostDtoToEntity(SchemePostDTO schemePostDto);

    public abstract List<SchemeEntity> schemePostDtoListToEntityList(List<SchemePostDTO> schemePostDtoList);

    public abstract SchemePostDTO schemeEntityToPostDto(SchemeEntity schemeEntity);

    public abstract  List<SchemePostDTO> schemeEntityListToPostDtoList(List<SchemeEntity> schemeEntityList);

    @Mapping(target = "ggisIdentifier", source = "ggisReference")
    @Mapping(target = "email", source = "contactEmail")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateSchemeEntityFromPatchDto(SchemePatchDTO schemePatchDto, @MappingTarget SchemeEntity schemeEntity);

}
