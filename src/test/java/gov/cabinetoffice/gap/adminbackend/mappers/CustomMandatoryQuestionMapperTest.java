package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionFundingLocation;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.UserNotFoundException;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomMandatoryQuestionMapperTest {

    final UUID submissionId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    final UUID mandatoryQuestionId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    final Instant now = Instant.now();

    final GrantApplicant applicant = GrantApplicant.builder().id(1).build();

    private SchemeEntity schemeEntity;

    private Submission submission;

    private GrantMandatoryQuestions mandatoryQuestions;

    private DraftAssessmentDto draftAssessmentDto;

    @Mock
    UserService userService;

    @InjectMocks
    private CustomMandatoryQuestionMapper customMandatoryQuestionMapper;

    @BeforeEach
    void setUp() {
        schemeEntity = SchemeEntity.builder().id(1).name("Test Scheme").ggisIdentifier("ggisId1").createdBy(10).build();

        submission = Submission.builder().id(submissionId).scheme(schemeEntity).build();

        mandatoryQuestions = GrantMandatoryQuestions.builder().id(mandatoryQuestionId).schemeEntity(schemeEntity)
                .submission(submission).name("Sample Question").addressLine1("123 Street").addressLine2("Apt 456")
                .city("Cityville").county("County").postcode("12345")
                .orgType(GrantMandatoryQuestionOrgType.LIMITED_COMPANY).companiesHouseNumber("ABC123")
                .charityCommissionNumber("XYZ789").fundingAmount(BigDecimal.TEN)
                .fundingLocation(
                        new GrantMandatoryQuestionFundingLocation[] { GrantMandatoryQuestionFundingLocation.LONDON,
                                GrantMandatoryQuestionFundingLocation.EAST_ENGLAND })
                .status(GrantMandatoryQuestionStatus.IN_PROGRESS).version(1).created(now).lastUpdated(now)
                .createdBy(applicant).lastUpdatedBy(applicant).gapId("GAP123").build();

        draftAssessmentDto = DraftAssessmentDto.builder().addressLine1(mandatoryQuestions.getAddressLine1())
                .ggisSchemeId(mandatoryQuestions.getSchemeEntity().getGgisIdentifier())
                .addressPostcode(mandatoryQuestions.getPostcode())
                .applicationAmount(mandatoryQuestions.getFundingAmount().toString())
                .applicationNumber(mandatoryQuestions.getSubmission().getId().toString()).country("United Kingdom")
                .charityCommissionRegNo(mandatoryQuestions.getCharityCommissionNumber())
                .companiesHouseRegNo(mandatoryQuestions.getCompaniesHouseNumber())
                .organisationName(mandatoryQuestions.getName()).organisationType(mandatoryQuestions.getOrgType().name())
                .cityTown(mandatoryQuestions.getCity()).funderID("funderId").build();
    }

    @Test
    void mandatoryQuestionsToDraftAssessmentDto_success() {

        when(userService.getDepartmentGGISId(10)).thenReturn("funderId");

        final DraftAssessmentDto actual = customMandatoryQuestionMapper
                .mandatoryQuestionsToDraftAssessmentDto(mandatoryQuestions);

        assertThat(actual).isEqualTo(draftAssessmentDto);
    }

    @Test
    void mandatoryQuestionsToDraftAssessmentDto_mandatoryQuestionIsNull() {
        final DraftAssessmentDto actual = customMandatoryQuestionMapper.mandatoryQuestionsToDraftAssessmentDto(null);

        assertThat(actual).isNull();
    }

    @Test
    void mandatoryQuestionsToDraftAssessmentDto_schemeEntityIsNull() {
        mandatoryQuestions.setSchemeEntity(null);
        draftAssessmentDto.setGgisSchemeId(null);
        draftAssessmentDto.setFunderID(null);

        final DraftAssessmentDto actual = customMandatoryQuestionMapper
                .mandatoryQuestionsToDraftAssessmentDto(mandatoryQuestions);

        assertThat(actual).isEqualTo(draftAssessmentDto);
    }

    @Test
    void mandatoryQuestionsToDraftAssessmentDto_submissionIsNull() {
        mandatoryQuestions.setSubmission(null);
        draftAssessmentDto.setApplicationNumber(null);

        when(userService.getDepartmentGGISId(10)).thenReturn("funderId");

        final DraftAssessmentDto actual = customMandatoryQuestionMapper
                .mandatoryQuestionsToDraftAssessmentDto(mandatoryQuestions);

        assertThat(actual).isEqualTo(draftAssessmentDto);
    }

    @Test
    void mandatoryQuestionsToDraftAssessmentDto_getFunderIdThrowException() {
        draftAssessmentDto.setFunderID("User not found");

        doThrow(UserNotFoundException.class).when(userService).getDepartmentGGISId(10);

        final DraftAssessmentDto actual = customMandatoryQuestionMapper
                .mandatoryQuestionsToDraftAssessmentDto(mandatoryQuestions);

        assertThat(actual).isEqualTo(draftAssessmentDto);
    }

}