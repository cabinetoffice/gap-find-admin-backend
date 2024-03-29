package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
            draftAssessmentDto.applicationAmount(addDecimalPoints(mandatoryQuestions.getFundingAmount()));
        }

        if (mandatoryQuestions.getOrgType() != null) {
            draftAssessmentDto.organisationType(convertEnumToString(mandatoryQuestions.getOrgType()));
        }

        if (mandatoryQuestions.getGapId() != null) {
            draftAssessmentDto.applicationNumber(mandatoryQuestions.getGapId());
        }

        draftAssessmentDto.funderID(getFunderID(getSchemeCreatorId(mandatoryQuestions)));

        return draftAssessmentDto.build();
    }

    private String convertEnumToString(GrantMandatoryQuestionOrgType orgType) {
        return switch (orgType) {
            case LIMITED_COMPANY -> "Company";
            case CHARITY -> "Charity";
            case SOLE_TRADER, NON_LIMITED_COMPANY -> "Sole Trader";
            default -> throw new IllegalArgumentException("Unexpected value: " + orgType);
        };
    }

    private String addDecimalPoints(BigDecimal amount) {
        BigDecimal amountWithDecimalPoints = amount.setScale(2, RoundingMode.HALF_UP);
        return amountWithDecimalPoints.toString();

    }

    private Integer getSchemeCreatorId(GrantMandatoryQuestions mandatoryQuestions) {
        final SchemeEntity schemeEntity = mandatoryQuestions.getSchemeEntity();

        if (schemeEntity == null) {
            return null;
        }

        return schemeEntity.getCreatedBy();
    }

    private String mandatoryQuestionsSchemeEntityGgisIdentifier(GrantMandatoryQuestions grantMandatoryQuestions) {
        final SchemeEntity schemeEntity = grantMandatoryQuestions.getSchemeEntity();

        if (schemeEntity == null) {
            return null;
        }

        return schemeEntity.getGgisIdentifier();
    }

    protected String getFunderID(Integer adminId) {

        if (adminId == null) {
            throw new IllegalArgumentException("A user ID must be provided");
        }

        return userService.getDepartmentGGISId(adminId);
    }

}
