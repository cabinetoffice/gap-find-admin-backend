package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Primary
@Slf4j
public class CustomMandatoryQuestionMapper implements MandatoryQuestionsMapper {

    private final UserService userService;

    public DraftAssessmentDto mandatoryQuestionsToDraftAssessmentDto(GrantMandatoryQuestions mandatoryQuestions) {
        if (mandatoryQuestions == null) {
            return null;
        }

        final DraftAssessmentDto.DraftAssessmentDtoBuilder draftAssessmentDto = DraftAssessmentDto.builder();

        draftAssessmentDto.organisationName(mandatoryQuestions.getName());
        draftAssessmentDto.addressPostcode(mandatoryQuestions.getPostcode());
        draftAssessmentDto.country("United Kingdom");
        draftAssessmentDto.cityTown(mandatoryQuestions.getCity());
        draftAssessmentDto.addressLine1(mandatoryQuestions.getAddressLine1());
        draftAssessmentDto.charityCommissionRegNo(mandatoryQuestions.getCharityCommissionNumber());
        draftAssessmentDto.companiesHouseRegNo(mandatoryQuestions.getCompaniesHouseNumber());
        draftAssessmentDto.ggisSchemeId(mandatoryQuestionsSchemeEntityGgisIdentifier(mandatoryQuestions));

        if (mandatoryQuestions.getFundingAmount() != null) {
            draftAssessmentDto.applicationAmount(mandatoryQuestions.getFundingAmount().toString());
        }

        if (mandatoryQuestions.getOrgType() != null) {
            draftAssessmentDto.organisationType(mandatoryQuestions.getOrgType().name());
        }

        final UUID id = mandatoryQuestionsSubmissionId(mandatoryQuestions);

        if (id != null) {
            draftAssessmentDto.applicationNumber(id.toString());
        }

        draftAssessmentDto.funderID(getFunderID(getSchemeCreatorId(mandatoryQuestions)));

        return draftAssessmentDto.build();
    }

    private Integer getSchemeCreatorId(GrantMandatoryQuestions mandatoryQuestions) {
        if (mandatoryQuestions == null) {
            return null;
        }

        final SchemeEntity schemeEntity = mandatoryQuestions.getSchemeEntity();

        if (schemeEntity == null) {
            return null;
        }

        return schemeEntity.getCreatedBy();
    }

    private String mandatoryQuestionsSchemeEntityGgisIdentifier(GrantMandatoryQuestions grantMandatoryQuestions) {
        if (grantMandatoryQuestions == null) {
            return null;
        }

        final SchemeEntity schemeEntity = grantMandatoryQuestions.getSchemeEntity();

        if (schemeEntity == null) {
            return null;
        }

        return schemeEntity.getGgisIdentifier();
    }

    private UUID mandatoryQuestionsSubmissionId(GrantMandatoryQuestions grantMandatoryQuestions) {
        if (grantMandatoryQuestions == null) {
            return null;
        }

        final Submission submission = grantMandatoryQuestions.getSubmission();

        if (submission == null) {
            return null;
        }

        return submission.getId();
    }

    protected String getFunderID(Integer adminId) {

        if (adminId == null) {
            // TODO throw an exception
        }

        return userService.getDepartmentGGISId(adminId);
    }

}
