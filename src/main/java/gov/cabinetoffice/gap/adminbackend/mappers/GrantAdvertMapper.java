package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.CreateGrantAdvertResponseDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPublishingInformationResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertStatusResponseDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.models.AdvertBuilderQuestionView;
import gov.cabinetoffice.gap.adminbackend.models.AdvertDefinitionQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GrantAdvertMapper {

    @Mapping(target = "schemeId", source = "schemeEntity.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "lastUpdatedById", source = "lastUpdatedBy.id")
    CreateGrantAdvertResponseDto grantAdvertToCreateGrantAdvertResponseDto(GrantAdvert grantAdvert);

    @Mapping(target = "questionId", source = "id")
    @Mapping(target = "questionTitle", source = "title")
    @Mapping(target = "questionValidation", source = "validation")
    AdvertBuilderQuestionView advertBuilderQuestionViewFromDefintionQuestion(
            AdvertDefinitionQuestion definitionQuestion);

    List<AdvertBuilderQuestionView> advertBuilderQuestionViewListFromDefintionQuestionList(
            List<AdvertDefinitionQuestion> definitionQuestion);

    @Mapping(target = "grantAdvertId", source = "id")
    @Mapping(target = "grantAdvertStatus", source = "status")
    GetGrantAdvertStatusResponseDTO grantAdvertStatusResponseDtoFromGrantAdvert(GrantAdvert grantAdvert);

    @Mapping(target = "grantAdvertId", source = "id")
    @Mapping(target = "grantAdvertStatus", source = "status")
    GetGrantAdvertPublishingInformationResponseDTO grantAdvertPublishInformationResponseDtoFromGrantAdvert(
            GrantAdvert grantAdvert);

}