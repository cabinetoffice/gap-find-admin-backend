package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionQuestion;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionQuestionValidation;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionSection;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionSectionStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.SubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomGrantExportMapperImplTest {

    final UUID mockExportId = UUID.fromString("a3e3e3e3-3e3e-3e3e-3e3e-3e3e3e3e3e3e");
    final UUID submissionId = UUID.fromString("f5e3e3e3-3e3e-3e3e-3e3e-3e3e3e3e3e3e");
    public final ZonedDateTime FIXED_DATE_TIME = ZonedDateTime.of(
            LocalDate.of(2024, 2, 22),
            LocalTime.NOON,
            ZoneId.systemDefault()
    );
    final GrantExportEntity grantExport = GrantExportEntity.builder()
        .id(GrantExportId.builder().exportBatchId(mockExportId).submissionId(submissionId).build())
        .status(GrantExportStatus.COMPLETE)
        .location("location")
        .build();
    final ExportedSubmissionsDto exportedSubmissionsDto = ExportedSubmissionsDto.builder()
        .submissionId(submissionId)
        .zipFileLocation("location")
        .status(GrantExportStatus.COMPLETE)
        .name("Some company name")
            .submittedDate(FIXED_DATE_TIME)
        .build();
    final SubmissionQuestion ORG_NAME_SUBMISSION_QUESTION = SubmissionQuestion.builder()
        .questionId("APPLICANT_ORG_NAME")
        .profileField("ORG_NAME")
        .fieldTitle("Enter the name of your organisation")
        .hintText(
                "This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission")
        .responseType(ResponseTypeEnum.ShortAnswer)
        .validation(SubmissionQuestionValidation.builder().mandatory(true).minLength(5).maxLength(100).build())
        .response("Some company name")
        .build();
    final SubmissionSection ESSENTIAL_SECTION_SUBMISSION = SubmissionSection.builder()
        .sectionId("ESSENTIAL")
        .sectionTitle("Essential Information")
        .sectionStatus(SubmissionSectionStatus.COMPLETED)
        .questions(List.of(ORG_NAME_SUBMISSION_QUESTION))
        .build();
    final SubmissionDefinition submissionDefinition = SubmissionDefinition.builder()
        .sections(List.of(ESSENTIAL_SECTION_SUBMISSION))
        .build();
    final SchemeEntity scheme = SchemeEntity.builder().version(1).build();
    final Submission submission = Submission.builder()
        .id(submissionId)
        .scheme(scheme)
        .status(SubmissionStatus.SUBMITTED)
        .createdBy(GrantApplicant.builder().id(1).userId(UUID.randomUUID().toString()).build())
        .definition(submissionDefinition)
            .submittedDate(FIXED_DATE_TIME)
        .build();

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private CustomGrantExportMapperImpl customGrantExportMapper;

    @Test
    void grantExportEntityToGrantExportDTO() {
        final GrantExportDTO grantExportDTO = customGrantExportMapper.grantExportEntityToGrantExportDTO(grantExport);

        assertEquals(grantExportDTO.getSubmissionId(), grantExport.getId().getSubmissionId());
        assertEquals(grantExportDTO.getStatus(), grantExport.getStatus());
        assertEquals(grantExportDTO.getLocation(), grantExport.getLocation());
        assertEquals(grantExportDTO.getExportBatchId(), grantExport.getId().getExportBatchId());
        assertEquals(grantExportDTO.getApplicationId(), grantExport.getApplicationId());
        assertEquals(grantExportDTO.getEmailAddress(), grantExport.getEmailAddress());
        assertEquals(grantExportDTO.getCreated(), grantExport.getCreated());
        assertEquals(grantExportDTO.getCreatedBy(), grantExport.getCreatedBy());
        assertEquals(grantExportDTO.getLastUpdated(), grantExport.getLastUpdated());
    }

    @Test
    void grantExportEntityToExportedSubmissions_hasSubmission() {
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        final ExportedSubmissionsDto exportedSubmissions = customGrantExportMapper.grantExportEntityToExportedSubmissions(grantExport);

        assertEquals(exportedSubmissionsDto, exportedSubmissions);
    }

    @Test
    void grantExportEntityToExportedSubmissions_hasNoSubmission() {
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

        final ExportedSubmissionsDto exportedSubmissions = customGrantExportMapper.grantExportEntityToExportedSubmissions(grantExport);

        exportedSubmissionsDto.setName(submissionId.toString());
        exportedSubmissionsDto.setSubmittedDate(null);
        assertEquals(exportedSubmissionsDto, exportedSubmissions);
    }
}