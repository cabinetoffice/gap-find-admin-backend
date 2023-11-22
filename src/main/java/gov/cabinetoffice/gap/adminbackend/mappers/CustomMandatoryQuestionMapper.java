package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.exceptions.UserNotFoundException;
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

    private UserService userService;

    public DraftAssessmentDto mandatoryQuestionsToDraftAssessmentDto(GrantMandatoryQuestions mandatoryQuestions) {
        if (mandatoryQuestions == null) {
            return null;
        }

        DraftAssessmentDto.DraftAssessmentDtoBuilder draftAssessmentDto = DraftAssessmentDto.builder();

        draftAssessmentDto.OrganisationName(mandatoryQuestions.getName());
        draftAssessmentDto.AddressPostcode(mandatoryQuestions.getPostcode());
        if (mandatoryQuestions.getFundingAmount() != null) {
            draftAssessmentDto.ApplicationAmount(mandatoryQuestions.getFundingAmount().toString());
        }
        draftAssessmentDto.Country(mandatoryQuestions.getCounty());
        draftAssessmentDto.CityTown(mandatoryQuestions.getCity());
        draftAssessmentDto.AddressLine1(mandatoryQuestions.getAddressLine1());
        draftAssessmentDto.CharityCommissionRegNo(mandatoryQuestions.getCharityCommissionNumber());
        draftAssessmentDto.CompaniesHouseRegNo(mandatoryQuestions.getCompaniesHouseNumber());
        if (mandatoryQuestions.getOrgType() != null) {
            draftAssessmentDto.OrganisationType(mandatoryQuestions.getOrgType().name());
        }
        draftAssessmentDto.GGISSchemeId(mandatoryQuestionsSchemeEntityGgisIdentifier(mandatoryQuestions));

        UUID id = mandatoryQuestionsSubmissionId(mandatoryQuestions);
        if (id != null) {
            draftAssessmentDto.ApplicationNumber(id.toString());
        }

        draftAssessmentDto.FunderID(getFunderID(mandatoryQuestions.getSchemeEntity().getCreatedBy()));

        return draftAssessmentDto.build();
    }

    private String mandatoryQuestionsSchemeEntityGgisIdentifier(GrantMandatoryQuestions grantMandatoryQuestions) {
        if (grantMandatoryQuestions == null) {
            return null;
        }
        SchemeEntity schemeEntity = grantMandatoryQuestions.getSchemeEntity();
        if (schemeEntity == null) {
            return null;
        }
        String ggisIdentifier = schemeEntity.getGgisIdentifier();
        return ggisIdentifier;
    }

    private UUID mandatoryQuestionsSubmissionId(GrantMandatoryQuestions grantMandatoryQuestions) {
        if (grantMandatoryQuestions == null) {
            return null;
        }
        Submission submission = grantMandatoryQuestions.getSubmission();
        if (submission == null) {
            return null;
        }
        UUID id = submission.getId();
        return id;
    }

    private String getFunderID(Integer adminId) {
        try {
            return userService.getDepartmentGGISId(adminId);
        }
        catch (UserNotFoundException e) {
            log.error("User not found");
            return "User not found";
        }
    }

}
