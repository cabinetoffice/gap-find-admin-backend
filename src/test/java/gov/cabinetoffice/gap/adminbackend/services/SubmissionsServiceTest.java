package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.UserV2DTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationAuditDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.*;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SpotlightExportException;
import gov.cabinetoffice.gap.adminbackend.mappers.SubmissionMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.SubmissionMapperImpl;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAttachmentRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportBatchRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SubmissionRepository;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomGrantExportEntityGenerator;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSubmissionGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.time.ZonedDateTime;
import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.services.SubmissionsService.combineAddressLines;
import static gov.cabinetoffice.gap.adminbackend.services.SubmissionsService.mandatoryValue;
import static gov.cabinetoffice.gap.adminbackend.testdata.SubmissionTestData.*;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSubmissionGenerator.randomSubmission;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSubmissionGenerator.randomSubmissionDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@WithAdminSession
class SubmissionsServiceTest {

    private static final UUID KNOWN_UUID = UUID.fromString("12345678-0000-0000-0000-000000000000");

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private GrantAttachmentRepository grantAttachmentRepository;

    @Mock
    private AmazonSQS amazonSQS;

    @Mock
    private GrantExportRepository grantExportRepository;

    @Mock
    private GrantExportBatchRepository grantExportBatchRepository;

    @InjectMocks
    @Spy
    private SubmissionsService submissionsService;

    @Mock
    private ApplicationFormService applicationFormService;

    @Spy
    private SubmissionMapper submissionMapper = new SubmissionMapperImpl();

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SchemeService schemeService;

    @Mock
    private ZipService zipService;

    private final List<String> EXPECTED_SPOTLIGHT_ROW = Arrays.asList("GAP-LL-20220927-1", "Some company name",
            "9-10 St Andrew Square", "Edinburgh", "EH2 2AF", "500", "12738494", "Yes", "");

    @Nested
    class GetSubmissionById {
        final UUID submissionId = UUID.randomUUID();

        @Test
        void successfullyGetSubmissionById() {
            final Submission submission = Submission.builder().id(submissionId).build();
            when(submissionRepository.findById(submissionId)).thenReturn(Optional.ofNullable(submission));

            final Submission response = submissionsService.getSubmissionById(submissionId);

            verify(submissionRepository).findById(submissionId);
            assertThat(response).isEqualTo(submission);
        }

        @Test
        void throwsNotFoundException() {
            when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> submissionsService.getSubmissionById(submissionId),
                    "No Submission with ID " + submissionId + " was found");

        }

    }
    @Nested
    class ExportSpotlightChecksTests {

        @Test
        void exportSpotlightChecksWithGoodData() {
            final ApplicationFormDTO applicationFormDTO = ApplicationFormDTO.builder()
                    .audit(ApplicationAuditDTO.builder().createdBy(1).build()).grantSchemeId(1)
                    .applicationName("applicationName").build();
            final SubmissionDefinition submissionDefinition = randomSubmissionDefinition(SUBMISSION_DEFINITION).build();
            final Submission submission = RandomSubmissionGenerator.randomSubmission()
                    .status(SubmissionStatus.SUBMITTED)
                    .createdBy(GrantApplicant.builder().id(1).userId(UUID.randomUUID().toString()).build())
                    .definition(submissionDefinition).build();
            final SchemeDTO schemeDTO = SchemeDTO.builder().schemeId(1).ggisReference("123").build();

            when(applicationFormService.retrieveApplicationFormSummary(1, false, false)).thenReturn(applicationFormDTO);
            when(submissionRepository.findByApplicationGrantApplicationIdAndStatus(1, SubmissionStatus.SUBMITTED))
                    .thenReturn(Collections.singletonList(submission));
            when(schemeService.getSchemeBySchemeId(applicationFormDTO.getGrantSchemeId())).thenReturn(schemeDTO);
            doReturn(EXPECTED_SPOTLIGHT_ROW).when(submissionsService).buildSingleSpotlightRow(submission);
            when(zipService.createZip(anyList(), anyList(), anyList())).thenReturn(new ByteArrayOutputStream());

            ByteArrayOutputStream dataStream = submissionsService.exportSpotlightChecks(1);

            verify(submissionRepository).findByApplicationGrantApplicationIdAndStatus(1, SubmissionStatus.SUBMITTED);
            assertThat(dataStream).isNotNull();
        }

        @Test
        void exportSpotlightChecksWithEmptySubmission() {

            final ApplicationFormDTO applicationFormDTO = ApplicationFormDTO.builder()
                    .audit(ApplicationAuditDTO.builder().createdBy(1).build()).grantSchemeId(1)
                    .applicationName("applicationName").build();
            final Submission submission = randomSubmission().definition(emptySubmissionDefinition()).build();
            final SchemeDTO schemeDTO = SchemeDTO.builder().schemeId(1).ggisReference("123").build();

            when(applicationFormService.retrieveApplicationFormSummary(1, false, false)).thenReturn(applicationFormDTO);
            when(submissionRepository.findByApplicationGrantApplicationIdAndStatus(1, SubmissionStatus.SUBMITTED))
                    .thenReturn(Collections.singletonList(submission));
            when(schemeService.getSchemeBySchemeId(applicationFormDTO.getGrantSchemeId())).thenReturn(schemeDTO);
            doReturn(EXPECTED_SPOTLIGHT_ROW).when(submissionsService).buildSingleSpotlightRow(submission);
            when(zipService.createZip(anyList(), anyList(), anyList())).thenReturn(new ByteArrayOutputStream());

            ByteArrayOutputStream dataStream = submissionsService.exportSpotlightChecks(1);

            verify(submissionRepository).findByApplicationGrantApplicationIdAndStatus(1, SubmissionStatus.SUBMITTED);
            assertThat(dataStream).isNotNull();

        }

        @Test
        void exportSpotlightApplicationNotFound() {
            when(applicationFormService.retrieveApplicationFormSummary(1, false, false))
                    .thenThrow(new ApplicationFormException("Application form not found with id 1"));

            assertThatThrownBy(() -> submissionsService.exportSpotlightChecks(1))
                    .isInstanceOf(ApplicationFormException.class).hasMessage("Application form not found with id 1");
        }

        @Test
        void exportSpotlightSubmissionsNotFound() {
            ApplicationFormDTO applicationFormDTO = ApplicationFormDTO.builder()
                    .audit(new ApplicationAuditDTO(null, null, 2, null, null, null)).build();

            when(applicationFormService.retrieveApplicationFormSummary(1, false, false)).thenReturn(applicationFormDTO);

            assertThatThrownBy(() -> submissionsService.exportSpotlightChecks(1))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Admin 1 is unable to access application with id 1");
        }

    }

    @Nested
    class MandatoryValueTests {

        @Test
        void givenNullValue_throwsException() {
            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> mandatoryValue(KNOWN_UUID, "valueName", null));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains(KNOWN_UUID.toString()).contains("valueName");
        }

        @Test
        void givenEmptyValue_throwsException() {
            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> mandatoryValue(KNOWN_UUID, "valueName", ""));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains(KNOWN_UUID.toString()).contains("valueName");
        }

        @Test
        void givenValue_returnsValue() {
            assertThat(mandatoryValue(KNOWN_UUID, "valueName", "value")).isEqualTo("value");
        }

    }

    @Nested
    class CombineAddressLinesTests {

        @Test
        void givenNoData_returnsEmptyString() {
            String[] data = new String[0];
            String result = combineAddressLines(data);
            assertThat(result).isEmpty();
        }

        @Test
        void givenNullData_returnsEmptyString() {
            String[] data = new String[] { null, null, null, null, null };
            String result = combineAddressLines(data);
            assertThat(result).isEmpty();
        }

        @Test
        void givenBlankData_returnsEmptyString() {
            String[] data = new String[] { "", "", "", "", "" };
            String result = combineAddressLines(data);
            assertThat(result).isEmpty();
        }

        @Test
        void givenOnlyFirstLine_returnsFirst() {
            String[] data = new String[] { "one", null, "", "", "" };
            String result = combineAddressLines(data);
            assertThat(result).isEqualTo("one");
        }

        @Test
        void givenOnlySecondLine_returnsSecond() {
            String[] data = new String[] { null, "two", "", "", "" };
            String result = combineAddressLines(data);
            assertThat(result).isEqualTo("two");
        }

        @Test
        void givenFirstAndSecondLine_returnsFirstAndSecond() {
            String[] data = new String[] { "one", "two", "", "", "" };
            String result = combineAddressLines(data);
            assertThat(result).isEqualTo("one, two");
        }

    }

    @Nested
    class BuildSingleSpotlightRowTests {

        @Test
        void givenGoodInput_returnsExpectedData() {
            Submission submissionSection = randomSubmission().gapId("GAP-LL-20220927-1")
                    .definition(createSubmissionDefinition()).build();

            List<String> spotlightRow = submissionsService.buildSingleSpotlightRow(submissionSection);

            assertThat(spotlightRow).contains("GAP-LL-20220927-1").containsAll(EXPECTED_SPOTLIGHT_ROW);
        }

        @Test
        void givenSubmissionWithoutEssentialSection_throwsException() {
            Submission submission = randomSubmission().definition(emptySubmissionDefinition()).build();

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> submissionsService.buildSingleSpotlightRow(submission));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("Section with id ESSENTIAL does not exist");
        }

        @Test
        void givenDataWithoutGapId_throwsException() {
            Submission submission = randomSubmission().definition(createSubmissionDefinition()).build();

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> submissionsService.buildSingleSpotlightRow(submission));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("GAP_ID");
        }

        @Test
        void givenDataWithoutOrgName_throwsException() {
            Submission submission = randomSubmission().gapId("GAP-LL-20220927-1")
                    .definition(createSubmissionDefinition()).build();
            submission.getDefinition().getSections().get(1).getQuestionById("APPLICANT_ORG_NAME").setResponse("");

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> submissionsService.buildSingleSpotlightRow(submission));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("APPLICANT_ORG_NAME");
        }

        @Test
        void givenDataWithoutPostcode_throwsException() {
            Submission submission = randomSubmission().gapId("GAP-LL-20220927-1")
                    .definition(createSubmissionDefinition()).build();
            String[] address = new String[5];
            submission.getDefinition().getSections().get(1).getQuestionById("APPLICANT_ORG_ADDRESS")
                    .setMultiResponse(address);

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> submissionsService.buildSingleSpotlightRow(submission));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("POSTCODE");
        }

        @Test
        void givenDataWithoutAmount_throwsException() {
            Submission submission = randomSubmission().gapId("GAP-LL-20220927-1")
                    .definition(createSubmissionDefinition()).build();
            submission.getDefinition().getSections().get(1).getQuestionById("APPLICANT_AMOUNT").setResponse("");

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> submissionsService.buildSingleSpotlightRow(submission));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("APPLICANT_AMOUNT");
        }

    }

    @Nested
    class UpdateApplicationExportSpotlightFlagTests {

        @Test
        void updateApplicationExportSpotlightFlag() {
            doNothing().when(submissionRepository).updateLastRequiredChecksExportByGrantApplicationIdAndStatus(any(),
                    any(), eq(SubmissionStatus.SUBMITTED));

            assertThatNoException().isThrownBy(() -> submissionsService.updateSubmissionLastRequiredChecksExport(1));
        }

    }

    @Test
    void updateLastRequiredChecksExportBySchemeIdAndStatus() {
        doNothing().when(submissionRepository).updateLastRequiredChecksExportBySchemeIdAndStatus(any(), any(),
                eq(SubmissionStatus.SUBMITTED));

        assertThatNoException().isThrownBy(() -> submissionsService.updateLastRequiredChecksExportBySchemeId(1));
    }

    @Nested
    class TriggerSubmissionsExportTests {

        @Test
        void happyPath_throwsNoException() {
            SchemeEntity scheme = SchemeEntity.builder().id(1).build();
            List<Submission> submissions = Collections
                    .singletonList(randomSubmission().definition(SUBMISSION_DEFINITION).scheme(scheme).build());
            when(submissionRepository.findByApplicationGrantApplicationIdAndStatus(1, SubmissionStatus.SUBMITTED))
                    .thenReturn(submissions);

            // results of exportRecordRepository.save() and amazonSQS.sendMessage()
            // are ignored, so no need to mock them since mocks return null anyway - only
            // interested in them not throwing an exception

            assertThatNoException().isThrownBy(() -> submissionsService.triggerSubmissionsExport(1));
        }

        @Test
        void noSubmissionsFound_throwsException() {
            when(submissionRepository.findByApplicationGrantApplicationIdAndStatus(1, SubmissionStatus.SUBMITTED))
                    .thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> submissionsService.triggerSubmissionsExport(1))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("No submissions in SUBMITTED state for application 1");

        }

        @Test
        void repoSaveFailure_throwsException() {
            when(submissionRepository.findByApplicationGrantApplicationIdAndStatus(1, SubmissionStatus.SUBMITTED))
                    .thenThrow(new RuntimeException("Generic save fails"));

            assertThatThrownBy(() -> submissionsService.triggerSubmissionsExport(1))
                    .isInstanceOf(RuntimeException.class);

        }

        @Test
        void sqsMessageFailure_throwsException() {
            SchemeEntity scheme = SchemeEntity.builder().id(1).build();
            List<Submission> submissions = Collections
                    .singletonList(randomSubmission().definition(SUBMISSION_DEFINITION).scheme(scheme).build());
            when(submissionRepository.findByApplicationGrantApplicationIdAndStatus(1, SubmissionStatus.SUBMITTED))
                    .thenReturn(submissions);
            when(amazonSQS.sendMessageBatch(any())).thenThrow(new AmazonSQSException("Cannot send messages"));

            assertThatThrownBy(() -> submissionsService.triggerSubmissionsExport(1))
                    .isInstanceOf(AmazonSQSException.class);

        }

    }

    @Nested
    class GetExportStatusTests {

        final Integer APPLICATION_ID = 1;

        @Test
        void whenApplicationIsNotFound_returnNotStarted() {
            when(grantExportRepository.existsByApplicationId(APPLICATION_ID)).thenReturn(false);

            final GrantExportStatus result = submissionsService.getExportStatus(APPLICATION_ID);

            assertThat(result).isEqualTo(GrantExportStatus.NOT_STARTED);
        }

        @Test
        void whenExportRecordsExist_returnProcessing() {
            when(grantExportRepository.existsByApplicationId(APPLICATION_ID)).thenReturn(true);
            when(grantExportRepository.existsByApplicationIdAndStatus(APPLICATION_ID, GrantExportStatus.PROCESSING))
                    .thenReturn(true);

            final GrantExportStatus result = submissionsService.getExportStatus(APPLICATION_ID);

            assertThat(result).isEqualTo(GrantExportStatus.PROCESSING);
        }

        @Test
        void whenExportRecordsStillRequested_returnAwaiting() {
            when(grantExportRepository.existsByApplicationId(APPLICATION_ID)).thenReturn(true);
            when(grantExportRepository.existsByApplicationIdAndStatus(APPLICATION_ID, GrantExportStatus.PROCESSING))
                    .thenReturn(false);
            when(grantExportRepository.existsByApplicationIdAndStatus(APPLICATION_ID, GrantExportStatus.REQUESTED))
                    .thenReturn(true);

            final GrantExportStatus result = submissionsService.getExportStatus(APPLICATION_ID);

            assertThat(result).isEqualTo(GrantExportStatus.REQUESTED);
        }

        @Test
        void whenExportRecordsComplete_returnComplete() {
            when(grantExportRepository.existsByApplicationId(APPLICATION_ID)).thenReturn(true);
            when(grantExportRepository.existsByApplicationIdAndStatus(APPLICATION_ID, GrantExportStatus.PROCESSING))
                    .thenReturn(false);
            when(grantExportRepository.existsByApplicationIdAndStatus(APPLICATION_ID, GrantExportStatus.REQUESTED))
                    .thenReturn(false);

            final GrantExportStatus result = submissionsService.getExportStatus(APPLICATION_ID);

            assertThat(result).isEqualTo(GrantExportStatus.COMPLETE);
        }

        @Test
        void whenExportRecordsExist_returnFailed() {
            when(grantExportRepository.existsByApplicationId(APPLICATION_ID)).thenReturn(true);
            when(grantExportRepository.existsByApplicationIdAndStatus(APPLICATION_ID, GrantExportStatus.FAILED))
                    .thenReturn(true);

            final GrantExportStatus result = submissionsService.getExportStatus(APPLICATION_ID);

            assertThat(result).isEqualTo(GrantExportStatus.FAILED);
        }

    }

    @Nested
    class getSubmissionInfo {

        final String authHeader = "randomAuthHeader";

        @Test
        void happyPath() {
            final ZonedDateTime zonedDateTime = ZonedDateTime.now();
            final Submission submission = randomSubmission()
                    .definition(randomSubmissionDefinition(randomSubmissionDefinition().build()).build())
                    .gapId("testGapID")
                    .applicant(GrantApplicant.builder()
                            .organisationProfile(
                                    GrantApplicantOrganisationProfile.builder().legalName("testLegalName").build())
                            .build())
                    .scheme(SchemeEntity.builder().id(1).name("testSchemeName").build()).submittedDate(zonedDateTime)
                    .build();
            final UserV2DTO userDTO = UserV2DTO.builder().emailAddress("testEmailAddress").build();

            when(grantExportRepository.existsById(any(GrantExportId.class))).thenReturn(true);
            when(submissionRepository.findByIdWithApplicant(any(UUID.class))).thenReturn(Optional.of(submission));
            when(submissionMapper.submissionToLambdaSubmissionDefinition(any(Submission.class))).thenCallRealMethod();
            when(restTemplate.exchange(anyString(), any(), any(), eq(UserV2DTO.class)))
                    .thenReturn(new ResponseEntity<>(userDTO, HttpStatus.OK));

            final LambdaSubmissionDefinition actual = submissionsService.getSubmissionInfo(UUID.randomUUID(),
                    UUID.randomUUID(), authHeader);
            final LambdaSubmissionDefinition expected = submissionMapper
                    .submissionToLambdaSubmissionDefinition(submission);
            expected.setEmail(userDTO.emailAddress());

            assertEquals(expected, actual);
        }

        @Test
        void resourceNotFoundPath() {
            when(grantExportRepository.existsById(any(GrantExportId.class))).thenReturn(true);
            when(submissionRepository.findByIdWithApplicant(any(UUID.class))).thenReturn(Optional.empty());
            when(submissionMapper.submissionToLambdaSubmissionDefinition(any(Submission.class))).thenCallRealMethod();

            assertThatThrownBy(
                    () -> submissionsService.getSubmissionInfo(UUID.randomUUID(), UUID.randomUUID(), authHeader))
                            .isInstanceOf(NotFoundException.class);
        }

        @Test
        void unauthorisedPath() {
            when(grantExportRepository.existsById(any(GrantExportId.class))).thenReturn(false);

            assertThatThrownBy(
                    () -> submissionsService.getSubmissionInfo(UUID.randomUUID(), UUID.randomUUID(), authHeader))
                            .isInstanceOf(NotFoundException.class);
        }

    }

    @Nested
    class getCompletedSubmissionExportsForBatch {

        @Test
        void getCompletedSubmissionExports() {
            UUID testUUID = UUID.randomUUID();
            String urlToTest = "directory_of_file/filename.zip";

            GrantExportEntity mockEntity = RandomGrantExportEntityGenerator.randomGrantExportEntityBuilder()
                    .location(urlToTest).build();
            List<GrantExportEntity> mockEntityList = Collections.singletonList(mockEntity);

            when(grantExportRepository.findAllByIdExportBatchIdAndStatusAndCreatedBy(testUUID,
                    GrantExportStatus.COMPLETE, 1)).thenReturn(mockEntityList);

            List<SubmissionExportsDTO> submissionExports = submissionsService
                    .getCompletedSubmissionExportsForBatch(testUUID);

            assertEquals(mockEntityList.size(), submissionExports.size());
            assertEquals("filename.zip", submissionExports.get(0).getLabel());
            assertEquals(urlToTest, submissionExports.get(0).getS3key());

        }

        @Test
        void getLabelFallbackWhenInvalidS3KeyPassed() {
            UUID testUUID = UUID.randomUUID();
            String urlToTest = "abcd";
            String submissionId = "cfb42c03-e39c-4972-adb5-5dc096c82bf4";

            GrantExportId mockIdWithSubmissionIdToTest = RandomGrantExportEntityGenerator
                    .randomGrantExportEntityIdBuilder().submissionId(UUID.fromString(submissionId)).build();
            GrantExportEntity mockEntity = RandomGrantExportEntityGenerator.randomGrantExportEntityBuilder()
                    .id(mockIdWithSubmissionIdToTest).location(urlToTest).build();
            List<GrantExportEntity> mockEntityList = Collections.singletonList(mockEntity);

            when(grantExportRepository.findAllByIdExportBatchIdAndStatusAndCreatedBy(testUUID,
                    GrantExportStatus.COMPLETE, 1)).thenReturn(mockEntityList);

            List<SubmissionExportsDTO> submissionExports = submissionsService
                    .getCompletedSubmissionExportsForBatch(testUUID);

            assertEquals(mockEntityList.size(), submissionExports.size());
            assertEquals(submissionId, submissionExports.get(0).getLabel());
            assertEquals(urlToTest, submissionExports.get(0).getS3key());

        }

        @Test
        void getNoCompletedSubmissionExports() {
            UUID testUUID = UUID.randomUUID();

            when(grantExportRepository.findAllByIdExportBatchIdAndStatusAndCreatedBy(testUUID,
                    GrantExportStatus.COMPLETE, 1)).thenReturn(Collections.emptyList());

            List<SubmissionExportsDTO> submissionExports = submissionsService
                    .getCompletedSubmissionExportsForBatch(testUUID);

            Assertions.assertTrue(submissionExports.isEmpty());

        }

    }

    @Nested
    class updateExportStatus {

        @Test
        void completesSuccessfullyWhen1ResultUpdated() {
            when(grantExportRepository.updateExportRecordStatus(any(), any(), any())).thenReturn(1);

            assertThatNoException().isThrownBy(
                    () -> submissionsService.updateExportStatus("1234", "5678", GrantExportStatus.COMPLETE));
        }

        @Test
        void throwsIf0ResultsUpdated() {
            when(grantExportRepository.updateExportRecordStatus(any(), any(), any())).thenReturn(0);

            assertThatThrownBy(() -> submissionsService.updateExportStatus("1234", "5678", GrantExportStatus.COMPLETE))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Could not update entry in export records table to COMPLETE");
        }

        @Test
        void throwsIf2ResultsUpdated() {
            when(grantExportRepository.updateExportRecordStatus(any(), any(), any())).thenReturn(2);

            assertThatThrownBy(() -> submissionsService.updateExportStatus("1234", "5678", GrantExportStatus.COMPLETE))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Could not update entry in export records table to COMPLETE");
        }

    }

    @Nested
    class addSignedUrlToSubmissionExport {

        @Test
        void successfullyAddSignedUrlToExport() {
            UUID mockSubmissionId = UUID.randomUUID();
            UUID mockBatchId = UUID.randomUUID();
            String mockLocation = "aws_domain.com/path_to/filename.zip";

            doNothing().when(grantExportRepository).updateExportRecordLocation(mockSubmissionId, mockBatchId,
                    mockLocation);

            submissionsService.addS3ObjectKeyToSubmissionExport(mockSubmissionId, mockBatchId, mockLocation);
            verify(grantExportRepository).updateExportRecordLocation(mockSubmissionId, mockBatchId, mockLocation);
        }

        @Test
        void errorOccursWhileAddingLocation() {
            UUID mockSubmissionId = UUID.randomUUID();
            UUID mockBatchId = UUID.randomUUID();
            String mockLocation = "aws_domain.com/path_to/filename.zip";

            doThrow(new RuntimeException()).when(grantExportRepository).updateExportRecordLocation(mockSubmissionId,
                    mockBatchId, mockLocation);

            assertThatThrownBy(() -> submissionsService.addS3ObjectKeyToSubmissionExport(mockSubmissionId, mockBatchId,
                    mockLocation)).isInstanceOf(RuntimeException.class);

        }

    }

}