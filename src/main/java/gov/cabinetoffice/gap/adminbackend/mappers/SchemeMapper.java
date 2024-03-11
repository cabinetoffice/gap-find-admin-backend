package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
@Slf4j
@Mapper(componentModel = "spring")
public abstract class SchemeMapper {

    private UserService userService;

    @Autowired
    public final void setUserService(final UserService userService) {
        this.userService = userService;
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

        setLastUpdatedByValues(schemeEntity, schemeDTO);

        return schemeDTO.build();
    }

    private void setLastUpdatedByValues(SchemeEntity schemeEntity, SchemeDTO.SchemeDTOBuilder schemeDTO) {
        final boolean isLastUpdatedBySet = schemeEntity.getLastUpdatedBy() != null && schemeEntity.getLastUpdated() != null;
        if (isLastUpdatedBySet) {
            final byte[] lastUpdatedByEmail = userService.getGrantAdminById(schemeEntity.getLastUpdatedBy())
                    .map(admin -> {
                        final String sub = admin.getGapUser().getUserSub();
                        return userService.getEmailAddressForSub(sub);
                    }).orElse(null);

            schemeDTO.encryptedLastUpdatedBy(lastUpdatedByEmail);
            schemeDTO.lastUpdatedDate(schemeEntity.getLastUpdated());
        }
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
