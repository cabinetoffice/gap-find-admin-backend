package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightSubmissionDto;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionFundingLocation;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SpotlightSubmissionMapperTest {

    private static final Instant now = Instant.now();

    private static final UUID mandatoryQuestionId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final UUID spotlightSubmissionId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID submissionId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final GrantApplicant applicant = GrantApplicant.builder().id(1).build();

    private final SchemeEntity schemeEntity = SchemeEntity.builder().id(1).name("Test Scheme").build();

    private final Submission submission = Submission.builder().id(submissionId).scheme(schemeEntity).build();

    private final GrantMandatoryQuestions mandatoryQuestions = GrantMandatoryQuestions.builder()
        .id(mandatoryQuestionId)
        .schemeEntity(schemeEntity)
        .submission(submission)
        .name("Sample Question")
        .addressLine1("123 Street")
        .addressLine2("Apt 456")
        .city("Cityville")
        .county("County")
        .postcode("12345")
        .orgType(GrantMandatoryQuestionOrgType.LIMITED_COMPANY)
        .companiesHouseNumber("ABC123")
        .charityCommissionNumber("XYZ789")
        .fundingAmount(BigDecimal.TEN)
        .fundingLocation(new GrantMandatoryQuestionFundingLocation[] { GrantMandatoryQuestionFundingLocation.LONDON,
                GrantMandatoryQuestionFundingLocation.EAST_ENGLAND })
        .status(GrantMandatoryQuestionStatus.IN_PROGRESS)
        .version(1)
        .created(now)
        .lastUpdated(now)
        .createdBy(applicant)
        .lastUpdatedBy(applicant)
        .gapId("GAP123")
        .build();

    private final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
        .id(spotlightSubmissionId)
        .mandatoryQuestions(mandatoryQuestions)
        .grantScheme(schemeEntity)
        .status(SpotlightSubmissionStatus.QUEUED.toString())
        .lastSendAttempt(now)
        .version(1)
        .created(now)
        .lastUpdated(now)
        .build();

    @InjectMocks
    private SpotlightSubmissionMapper spotlightSubmissionMapper = Mappers.getMapper(SpotlightSubmissionMapper.class);

    @Test
    public void testSpotlighSubmissionToSpotlightSubmissionDto() {
        final SpotlightSubmissionDto spotlightSubmissionDto = spotlightSubmissionMapper
            .spotlightSubmissionToSpotlightSubmissionDto(spotlightSubmission);

        assertThat(spotlightSubmissionDto).isNotNull();
        assertThat(spotlightSubmissionDto.getId()).isEqualTo(spotlightSubmission.getId());
        assertThat(spotlightSubmissionDto.getStatus()).isEqualTo(spotlightSubmission.getStatus());
        assertThat(spotlightSubmissionDto.getGrantScheme()).isEqualTo(schemeEntity);
        assertThat(spotlightSubmissionDto.getCreated()).isEqualTo(spotlightSubmission.getCreated());
        assertThat(spotlightSubmissionDto.getLastUpdated()).isEqualTo(spotlightSubmission.getLastUpdated());
        assertThat(spotlightSubmissionDto.getLastSendAttempt()).isEqualTo(spotlightSubmission.getLastSendAttempt());
        assertThat(spotlightSubmissionDto.getVersion()).isEqualTo(spotlightSubmission.getVersion());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getId()).isEqualTo(mandatoryQuestions.getId());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getName()).isEqualTo(mandatoryQuestions.getName());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getAddressLine1())
            .isEqualTo(mandatoryQuestions.getAddressLine1());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getAddressLine2())
            .isEqualTo(mandatoryQuestions.getAddressLine2());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getCity()).isEqualTo(mandatoryQuestions.getCity());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getCounty())
            .isEqualTo(mandatoryQuestions.getCounty());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getPostcode())
            .isEqualTo(mandatoryQuestions.getPostcode());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getOrgType())
            .isEqualTo(mandatoryQuestions.getOrgType().name());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getCompaniesHouseNumber())
            .isEqualTo(mandatoryQuestions.getCompaniesHouseNumber());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getCharityCommissionNumber())
            .isEqualTo(mandatoryQuestions.getCharityCommissionNumber());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getFundingAmount())
            .isEqualTo(mandatoryQuestions.getFundingAmount());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getFundingLocation())
            .isEqualTo(new String[] { GrantMandatoryQuestionFundingLocation.LONDON.toString(),
                    GrantMandatoryQuestionFundingLocation.EAST_ENGLAND.toString() });
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getStatus())
            .isEqualTo(mandatoryQuestions.getStatus().toString());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getVersion())
            .isEqualTo(mandatoryQuestions.getVersion());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getCreated())
            .isEqualTo(mandatoryQuestions.getCreated());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getCreatedBy())
            .isEqualTo(mandatoryQuestions.getCreatedBy().getId());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getGapId()).isEqualTo(mandatoryQuestions.getGapId());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getSchemeId())
            .isEqualTo(mandatoryQuestions.getSchemeEntity().getId());
        assertThat(spotlightSubmissionDto.getMandatoryQuestions().getSubmissionId())
            .isEqualTo(mandatoryQuestions.getSubmission().getId());
    }

}