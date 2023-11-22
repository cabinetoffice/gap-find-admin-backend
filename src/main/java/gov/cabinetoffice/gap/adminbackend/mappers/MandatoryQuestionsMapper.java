package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { CustomMandatoryQuestionMapper.class })
public interface MandatoryQuestionsMapper {

    DraftAssessmentDto mandatoryQuestionsToDraftAssessmentDto(GrantMandatoryQuestions mandatoryQuestions);

}
