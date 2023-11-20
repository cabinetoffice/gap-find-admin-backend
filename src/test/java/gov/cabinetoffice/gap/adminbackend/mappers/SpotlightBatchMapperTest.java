package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.SpotlightBatchDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightSubmissionDto;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionFundingLocation;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SpotlightBatchMapperTest {

    private static final Instant now = Instant.now();

    private static final UUID mandatoryQuestionId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final UUID spotlightSubmissionId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID submissionId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID spotlightBatchId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID mandatoryQuestion2Id = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final UUID submissionId2 = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final UUID spotlightSubmissionId2 = UUID.fromString("66666666-6666-6666-6666-666666666666");

    private final GrantApplicant applicant = GrantApplicant.builder().id(1).build();

    private final GrantApplicant applicant2 = GrantApplicant.builder().id(2).build();

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

    private final Submission submission2 = Submission.builder().id(submissionId2).scheme(schemeEntity).build();

    private final GrantMandatoryQuestions mandatoryQuestions2 = GrantMandatoryQuestions.builder()
        .id(mandatoryQuestion2Id)
        .schemeEntity(schemeEntity)
        .submission(submission2)
        .name("name")
        .addressLine1("addressLine1")
        .city("city")
        .postcode("g315sx")
        .orgType(GrantMandatoryQuestionOrgType.CHARITY)
        .fundingAmount(BigDecimal.valueOf(1000))
        .fundingLocation(new GrantMandatoryQuestionFundingLocation[] { GrantMandatoryQuestionFundingLocation.MIDLANDS })
        .status(GrantMandatoryQuestionStatus.COMPLETED)
        .version(1)
        .created(now)
        .lastUpdated(now)
        .createdBy(applicant2)
        .lastUpdatedBy(applicant2)
        .gapId("GAP987")
        .build();

    private final SpotlightSubmission spotlightSubmission2 = SpotlightSubmission.builder()
        .id(spotlightSubmissionId2)
        .mandatoryQuestions(mandatoryQuestions2)
        .grantScheme(schemeEntity)
        .status(SpotlightSubmissionStatus.QUEUED.toString())
        .lastSendAttempt(now)
        .version(1)
        .created(now)
        .lastUpdated(now)
        .build();

    private final SpotlightBatch spotlightBatch = SpotlightBatch.builder()
        .id(spotlightBatchId)
        .status(SpotlightBatchStatus.QUEUED)
        .lastSendAttempt(Instant.now())
        .version(1)
        .created(now)
        .lastUpdated(now)
        .spotlightSubmissions(List.of(spotlightSubmission, spotlightSubmission2))
        .build();

    @InjectMocks
    private SpotlightBatchMapper spotlightBatchMapper = Mappers.getMapper(SpotlightBatchMapper.class);

    @Test
    public void testSpotlightBatchToSpotlightBatchDto() {
        final SpotlightBatchDto spotlightBatchDto = spotlightBatchMapper
            .spotlightBatchToGetSpotlightBatchDto(spotlightBatch);

        final SpotlightSubmissionDto spotlightSubmissionDto1 = spotlightBatchDto.getSpotlightSubmissions().get(0);
        final SpotlightSubmissionDto spotlightSubmissionDto2 = spotlightBatchDto.getSpotlightSubmissions().get(1);

        assertThat(spotlightBatchDto).isNotNull();
        assertThat(spotlightBatchDto.getId()).isEqualTo(spotlightBatch.getId());
        assertThat(spotlightBatchDto.getStatus()).isEqualTo(spotlightBatch.getStatus());
        assertThat(spotlightBatchDto.getLastSendAttempt()).isEqualTo(spotlightBatch.getLastSendAttempt());
        assertThat(spotlightBatchDto.getVersion()).isEqualTo(spotlightBatch.getVersion());
        assertThat(spotlightBatchDto.getCreated()).isEqualTo(spotlightBatch.getCreated());
        assertThat(spotlightBatchDto.getLastUpdated()).isEqualTo(spotlightBatch.getLastUpdated());
        assertThat(spotlightBatchDto.getSpotlightSubmissions().size()).isEqualTo(2);
        // first submission
        assertThat(spotlightSubmissionDto1.getId()).isEqualTo(spotlightSubmission.getId());
        assertThat(spotlightSubmissionDto1.getStatus()).isEqualTo(spotlightSubmission.getStatus());
        assertThat(spotlightSubmissionDto1.getGrantScheme()).isEqualTo(schemeEntity);
        assertThat(spotlightSubmissionDto1.getCreated()).isEqualTo(spotlightSubmission.getLastSendAttempt());
        assertThat(spotlightSubmissionDto1.getLastUpdated()).isEqualTo(spotlightSubmission.getLastSendAttempt());
        assertThat(spotlightSubmissionDto1.getLastSendAttempt()).isEqualTo(spotlightSubmission.getLastSendAttempt());
        assertThat(spotlightSubmissionDto1.getVersion()).isEqualTo(spotlightSubmission.getVersion());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getId()).isEqualTo(mandatoryQuestions.getId());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getName()).isEqualTo(mandatoryQuestions.getName());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getAddressLine1())
            .isEqualTo(mandatoryQuestions.getAddressLine1());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getAddressLine2())
            .isEqualTo(mandatoryQuestions.getAddressLine2());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getCity()).isEqualTo(mandatoryQuestions.getCity());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getCounty())
            .isEqualTo(mandatoryQuestions.getCounty());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getPostcode())
            .isEqualTo(mandatoryQuestions.getPostcode());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getOrgType())
            .isEqualTo(mandatoryQuestions.getOrgType().name());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getCompaniesHouseNumber())
            .isEqualTo(mandatoryQuestions.getCompaniesHouseNumber());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getCharityCommissionNumber())
            .isEqualTo(mandatoryQuestions.getCharityCommissionNumber());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getFundingAmount())
            .isEqualTo(mandatoryQuestions.getFundingAmount());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getFundingLocation())
            .isEqualTo(new String[] { GrantMandatoryQuestionFundingLocation.LONDON.toString(),
                    GrantMandatoryQuestionFundingLocation.EAST_ENGLAND.toString() });
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getStatus())
            .isEqualTo(mandatoryQuestions.getStatus().toString());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getVersion())
            .isEqualTo(mandatoryQuestions.getVersion());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getCreated())
            .isEqualTo(mandatoryQuestions.getCreated());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getCreatedBy())
            .isEqualTo(mandatoryQuestions.getCreatedBy().getId());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getGapId()).isEqualTo(mandatoryQuestions.getGapId());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getSchemeId())
            .isEqualTo(mandatoryQuestions.getSchemeEntity().getId());
        assertThat(spotlightSubmissionDto1.getMandatoryQuestions().getSubmissionId())
            .isEqualTo(mandatoryQuestions.getSubmission().getId());

        // second submission
        assertThat(spotlightSubmissionDto2.getId()).isEqualTo(spotlightSubmission2.getId());
        assertThat(spotlightSubmissionDto2.getStatus()).isEqualTo(spotlightSubmission2.getStatus());
        assertThat(spotlightSubmissionDto2.getGrantScheme()).isEqualTo(schemeEntity);
        assertThat(spotlightSubmissionDto2.getCreated()).isEqualTo(spotlightSubmission2.getLastSendAttempt());
        assertThat(spotlightSubmissionDto2.getLastUpdated()).isEqualTo(spotlightSubmission2.getLastSendAttempt());
        assertThat(spotlightSubmissionDto2.getLastSendAttempt()).isEqualTo(spotlightSubmission2.getLastSendAttempt());
        assertThat(spotlightSubmissionDto2.getVersion()).isEqualTo(spotlightSubmission2.getVersion());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getId()).isEqualTo(mandatoryQuestions2.getId());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getName()).isEqualTo(mandatoryQuestions2.getName());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getAddressLine1())
            .isEqualTo(mandatoryQuestions2.getAddressLine1());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getAddressLine2())
            .isEqualTo(mandatoryQuestions2.getAddressLine2());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getCity()).isEqualTo(mandatoryQuestions2.getCity());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getCounty())
            .isEqualTo(mandatoryQuestions2.getCounty());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getPostcode())
            .isEqualTo(mandatoryQuestions2.getPostcode());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getOrgType())
            .isEqualTo(mandatoryQuestions2.getOrgType().name());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getCompaniesHouseNumber())
            .isEqualTo(mandatoryQuestions2.getCompaniesHouseNumber());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getCharityCommissionNumber())
            .isEqualTo(mandatoryQuestions2.getCharityCommissionNumber());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getFundingAmount())
            .isEqualTo(mandatoryQuestions2.getFundingAmount());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getFundingLocation())
            .isEqualTo(new String[] { GrantMandatoryQuestionFundingLocation.MIDLANDS.toString() });
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getStatus())
            .isEqualTo(mandatoryQuestions2.getStatus().toString());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getVersion())
            .isEqualTo(mandatoryQuestions2.getVersion());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getCreated())
            .isEqualTo(mandatoryQuestions2.getCreated());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getCreatedBy())
            .isEqualTo(mandatoryQuestions2.getCreatedBy().getId());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getGapId())
            .isEqualTo(mandatoryQuestions2.getGapId());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getSchemeId())
            .isEqualTo(mandatoryQuestions2.getSchemeEntity().getId());
        assertThat(spotlightSubmissionDto2.getMandatoryQuestions().getSubmissionId())
            .isEqualTo(mandatoryQuestions2.getSubmission().getId());

    }

}