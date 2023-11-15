package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SchemeMapper {

    @Mapping(target = "schemeId", source = "id")
    @Mapping(target = "ggisReference", source = "ggisIdentifier")
    @Mapping(target = "contactEmail", source = "email")
    SchemeDTO schemeEntityToDto(SchemeEntity schemeEntity);

    List<SchemeDTO> schemeEntityListtoDtoList(List<SchemeEntity> schemeEntityList);

    SchemeEntity schemeDtoToEntity(SchemeDTO schemeDto);

    List<SchemeEntity> schemeDtoListToEntityList(List<SchemeDTO> schemeDto);

    @Mapping(target = "ggisIdentifier", source = "ggisReference")
    @Mapping(target = "email", source = "contactEmail")
    SchemeEntity schemePostDtoToEntity(SchemePostDTO schemePostDto);

    List<SchemeEntity> schemePostDtoListToEntityList(List<SchemePostDTO> schemePostDtoList);

    SchemePostDTO schemeEntityToPostDto(SchemeEntity schemeEntity);

    List<SchemePostDTO> schemeEntityListToPostDtoList(List<SchemeEntity> schemeEntityList);

    @Mapping(target = "ggisIdentifier", source = "ggisReference")
    @Mapping(target = "email", source = "contactEmail")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSchemeEntityFromPatchDto(SchemePatchDTO schemePatchDto, @MappingTarget SchemeEntity schemeEntity);

}
