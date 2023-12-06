package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.SpotlightBatchDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightMandatoryQuestionDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightSubmissionDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpotlightBatchMapper {

    @Mapping(source = "spotlightSubmissions", target = "spotlightSubmissions",
            qualifiedByName = "mapSpotlightSubmissions")
    SpotlightBatchDto spotlightBatchToGetSpotlightBatchDto(SpotlightBatch spotlightBatch);

    @Named("mapSpotlightSubmissions")
    default List<SpotlightSubmissionDto> mapSpotlightSubmission(List<SpotlightSubmission> spotlightSubmissions) {
        return spotlightSubmissionListToSpotlightSubmissionDtoList(spotlightSubmissions);
    }

    @Mapping(source = "mandatoryQuestions", target = "mandatoryQuestions", qualifiedByName = "mapMandatoryQuestions")
    SpotlightSubmissionDto spotlightSubmissionToSpotlightSubmissionDto(SpotlightSubmission spotlightSubmission);

    @Named("mapMandatoryQuestions")
    default SpotlightMandatoryQuestionDto mapMandatoryQuestions(GrantMandatoryQuestions mandatoryQuestions) {
        return mandatoryQuestionsToSpotlightMandatoryQuestions(mandatoryQuestions);
    }

    @Mapping(source = "schemeEntity.id", target = "schemeId")
    @Mapping(source = "submission.id", target = "submissionId")
    @Mapping(source = "createdBy.id", target = "createdBy")
    SpotlightMandatoryQuestionDto mandatoryQuestionsToSpotlightMandatoryQuestions(
            GrantMandatoryQuestions mandatoryQuestions);

    List<SpotlightBatchDto> spotlightBatchListToGetSpotlightBatchDtoList(List<SpotlightBatch> spotlightBatches);

    default List<SpotlightSubmissionDto> spotlightSubmissionListToSpotlightSubmissionDtoList(
            List<SpotlightSubmission> submissions) {
        return submissions.stream().map(this::spotlightSubmissionToSpotlightSubmissionDto).toList();
    }

}