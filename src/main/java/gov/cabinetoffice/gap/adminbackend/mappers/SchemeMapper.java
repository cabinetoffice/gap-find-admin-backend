package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
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

        // Slightly strange logic here but if "last updated by" is null then we want to return "created by" instead.
        // Considering the above, it doesn't make sense to potentially return mismatched updaters/update dates
        // SO, if last updated by is not null then return this pair of values, otherwise return created by and created date
        final boolean isLastUpdatedBySet = schemeEntity.getLastUpdatedBy() != null;
        final int lastUpdatedBy  = isLastUpdatedBySet ? schemeEntity.getLastUpdatedBy() : schemeEntity.getCreatedBy();
        final String lastUpdatedByEmail = userService.getGrantAdminById(lastUpdatedBy)
                .map(admin -> {
                    final String sub = admin.getGapUser().getUserSub();
                    return "thisNeedsReplaced@email.com"; // TODO go to user service and get email from sub after John commits his changes
                })
                .get();
        final Instant lastUpdatedDate = isLastUpdatedBySet ? schemeEntity.getLastUpdated() : schemeEntity.getCreatedDate();

        schemeDTO.lastUpdatedBy(lastUpdatedByEmail);
        schemeDTO.lastUpdatedDate(lastUpdatedDate);

        return schemeDTO.build();
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
