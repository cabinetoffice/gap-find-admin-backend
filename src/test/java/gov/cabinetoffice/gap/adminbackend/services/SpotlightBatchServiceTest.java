package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.SpotlightConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.SpotlightQueueConfigProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SendToSpotlightDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SpotlightSchemeDto;
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
import gov.cabinetoffice.gap.adminbackend.exceptions.JsonParseException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SecretValueException;
import gov.cabinetoffice.gap.adminbackend.mappers.MandatoryQuestionsMapper;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightBatchRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class SpotlightBatchServiceTest {

    private static final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Mock
    private SpotlightBatchRepository spotlightBatchRepository;

    @Mock
    private MandatoryQuestionsMapper mandatoryQuestionsMapper;

    @Mock
    SecretsManagerClient secretsManagerClient;

    @Mock
    RestTemplate restTemplate;

    private SpotlightBatchService spotlightBatchService;

    private SpotlightConfigProperties spotlightConfigProperties;

    private ObjectMapper objectMapper;

    private SpotlightQueueConfigProperties spotlightQueueProperties;

    private AmazonSQS amazonSqs;

    private SpotlightSubmissionService spotlightSubmissionService;

    private SpotlightSubmissionRepository spotlightSubmissionRepository;

    @BeforeEach
    void setup() {
        spotlightConfigProperties = SpotlightConfigProperties.builder().spotlightUrl("spotlightUrl")
                .secretName("secretName").build();
        objectMapper = Mockito.spy(new ObjectMapper());
        spotlightQueueProperties = SpotlightQueueConfigProperties.builder().queueUrl("queueUrl").build();

        spotlightBatchService = Mockito.spy(new SpotlightBatchService(spotlightBatchRepository,
                mandatoryQuestionsMapper, secretsManagerClient,restTemplate, spotlightSubmissionRepository, spotlightConfigProperties, objectMapper ,
                spotlightQueueProperties, amazonSqs, spotlightSubmissionService));
    }

    @Nested
    class SpotlightBatchWithStatusExistsTests {

        @Test
        void spotlightBatchWithStatusExists() {
            when(spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt()))
                    .thenReturn(true);

            final boolean result = spotlightBatchService.existsByStatusAndMaxBatchSize(SpotlightBatchStatus.QUEUED,
                    200);

            assertTrue(result);
        }

        @Test
        void spotlightBatchWithStatusDoesNotExist() {
            when(spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt()))
                    .thenReturn(false);

            final boolean result = spotlightBatchService.existsByStatusAndMaxBatchSize(SpotlightBatchStatus.QUEUED,
                    200);

            assertFalse(result);
        }

    }

    @Nested
    class GetSpotlightBatchWithStatusTests {

        @Test
        void spotlightBatchWithStatusExists() {
            final SpotlightBatch mockSpotlightBatch = SpotlightBatch.builder().id(uuid).build();
            when(spotlightBatchRepository.findByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt()))
                    .thenReturn(Optional.of(mockSpotlightBatch));

            final SpotlightBatch result = spotlightBatchService.getSpotlightBatchWithStatus(SpotlightBatchStatus.QUEUED,
                    200);

            assertEquals(mockSpotlightBatch, result);
        }

        @Test
        void spotlightBatchWithStatusDoesNotExist() {
            when(spotlightBatchRepository.findByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt()))
                    .thenReturn(Optional.empty());

            final NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> spotlightBatchService.getSpotlightBatchWithStatus(SpotlightBatchStatus.QUEUED, 200));

            assertEquals("A spotlight batch with status QUEUED could not be found", exception.getMessage());
        }

    }

    @Nested
    class CreateSpotlightBatchTests {

        @Test
        void createSpotlightBatch() {
            final SpotlightBatch mockSpotlightBatch = SpotlightBatch.builder().id(uuid).build();
            when(spotlightBatchRepository.save(any())).thenReturn(mockSpotlightBatch);

            final SpotlightBatch result = spotlightBatchService.createSpotlightBatch();

            assertEquals(mockSpotlightBatch, result);
        }

    }

    @Nested
    class AddSpotlightSubmissionToSpotlightBatchTests {

        @Test
        void addSpotlightSubmissionToSpotlightBatch() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().batches(new ArrayList<>())
                    .build();
            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(uuid)
                    .spotlightSubmissions(new ArrayList<>()).build();

            when(spotlightBatchRepository.findById(uuid)).thenReturn(Optional.of(spotlightBatch));
            when(spotlightBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            final SpotlightBatch result = spotlightBatchService
                    .addSpotlightSubmissionToSpotlightBatch(spotlightSubmission, uuid);

            verify(spotlightBatchRepository, times(1)).findById(uuid);
            verify(spotlightBatchRepository, times(1)).save(spotlightBatch);

            List<SpotlightSubmission> resultSubmissions = result.getSpotlightSubmissions();
            List<SpotlightBatch> resultBatches = spotlightSubmission.getBatches();

            assertThat(resultSubmissions).hasSize(1);
            assertThat(resultBatches).hasSize(1);
            assertThat(resultSubmissions.get(0)).isEqualTo(spotlightSubmission);
            assertThat(resultBatches.get(0)).isEqualTo(result);
        }

        @Test
        void addSpotlightSubmissionToSpotlightBatchBatchNotFound() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().build();

            when(spotlightBatchRepository.findById(uuid)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> spotlightBatchService.addSpotlightSubmissionToSpotlightBatch(spotlightSubmission, uuid));

            assertEquals("A spotlight batch with id " + uuid + " could not be found", exception.getMessage());
        }

    }

    @Nested
    class getSpotlightBatchesByStatus {

        @Test
        void getSpotlightBatchesByStatus() {
            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(uuid).build();
            final List<SpotlightBatch> spotlightBatches = List.of(spotlightBatch);

            when(spotlightBatchRepository.findByStatus(any())).thenReturn(Optional.of(spotlightBatches));

            final List<SpotlightBatch> result = spotlightBatchService
                    .getSpotlightBatchesByStatus(SpotlightBatchStatus.QUEUED);

            assertThat(result).isEqualTo(spotlightBatches);
        }

        @Test
        void getSpotlightBatchesByStatusNotFound() {
            when(spotlightBatchRepository.findByStatus(any())).thenReturn(Optional.empty());

            final List<SpotlightBatch> result = spotlightBatchService
                    .getSpotlightBatchesByStatus(SpotlightBatchStatus.QUEUED);

            assertTrue(result.isEmpty());
        }

    }

    @Nested
    class GenerateSendToSpotlightDtoTests {

        final Instant now = Instant.now();

        final UUID mandatoryQuestionId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        final UUID spotlightSubmissionId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        final UUID submissionId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        final UUID spotlightBatchId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        final UUID mandatoryQuestion2Id = UUID.fromString("44444444-4444-4444-4444-444444444444");

        final UUID submissionId2 = UUID.fromString("55555555-5555-5555-5555-555555555555");

        final UUID spotlightSubmissionId2 = UUID.fromString("66666666-6666-6666-6666-666666666666");

        final UUID spotlightBatchId2 = UUID.fromString("77777777-7777-7777-7777-777777777777");

        final UUID spotlightSubmissionId3 = UUID.fromString("88888888-8888-8888-8888-888888888888");

        final UUID mandatoryQuestionsId3 = UUID.fromString("99999999-9999-9999-9999-999999999999");

        final UUID spotligtSubmissionId4 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        final UUID submissionId3 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        final UUID mandatoryQuestionId4 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        final GrantApplicant applicant = GrantApplicant.builder().id(1).build();

        final GrantApplicant applicant2 = GrantApplicant.builder().id(2).build();

        final SchemeEntity schemeEntity = SchemeEntity.builder().id(1).name("Test Scheme").ggisIdentifier("ggisId1")
                .build();

        final SchemeEntity schemeEntity2 = SchemeEntity.builder().id(2).name("Test Scheme 2").ggisIdentifier("ggisId2")
                .build();

        final Submission submission = Submission.builder().id(submissionId).scheme(schemeEntity).build();

        final GrantMandatoryQuestions mandatoryQuestions = GrantMandatoryQuestions.builder().id(mandatoryQuestionId)
                .schemeEntity(schemeEntity).submission(submission).name("Sample Question").addressLine1("123 Street")
                .addressLine2("Apt 456").city("Cityville").county("County").postcode("12345")
                .orgType(GrantMandatoryQuestionOrgType.LIMITED_COMPANY).companiesHouseNumber("ABC123")
                .charityCommissionNumber("XYZ789").fundingAmount(BigDecimal.TEN)
                .fundingLocation(
                        new GrantMandatoryQuestionFundingLocation[] { GrantMandatoryQuestionFundingLocation.LONDON,
                                GrantMandatoryQuestionFundingLocation.EAST_ENGLAND })
                .status(GrantMandatoryQuestionStatus.IN_PROGRESS).version(1).created(now).lastUpdated(now)
                .createdBy(applicant).lastUpdatedBy(applicant).gapId("GAP123").build();

        final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                .mandatoryQuestions(mandatoryQuestions).grantScheme(schemeEntity)
                .status(SpotlightSubmissionStatus.QUEUED.toString()).lastSendAttempt(now).version(1).created(now)
                .lastUpdated(now).build();

        final GrantMandatoryQuestions mandatoryQuestions4 = GrantMandatoryQuestions.builder().id(mandatoryQuestionId4)
                .schemeEntity(schemeEntity).submission(submission).name("Sample Question").addressLine1("123 Street")
                .addressLine2("Apt 456").city("Cityville").county("County").postcode("12345")
                .orgType(GrantMandatoryQuestionOrgType.LIMITED_COMPANY).companiesHouseNumber("ABC123")
                .charityCommissionNumber("XYZ789").fundingAmount(BigDecimal.TEN)
                .fundingLocation(
                        new GrantMandatoryQuestionFundingLocation[] { GrantMandatoryQuestionFundingLocation.LONDON,
                                GrantMandatoryQuestionFundingLocation.EAST_ENGLAND })
                .status(GrantMandatoryQuestionStatus.IN_PROGRESS).version(1).created(now).lastUpdated(now)
                .createdBy(applicant).lastUpdatedBy(applicant).gapId("GAP123").build();

        final SpotlightSubmission spotlightSubmission4 = SpotlightSubmission.builder().id(spotligtSubmissionId4)
                .mandatoryQuestions(mandatoryQuestions4).grantScheme(schemeEntity)
                .status(SpotlightSubmissionStatus.QUEUED.toString()).lastSendAttempt(now).version(1).created(now)
                .lastUpdated(now).build();

        final SpotlightBatch spotlightBatch2 = SpotlightBatch.builder().id(spotlightBatchId2)
                .status(SpotlightBatchStatus.QUEUED).lastSendAttempt(now).version(1).created(now).lastUpdated(now)
                .spotlightSubmissions(List.of(spotlightSubmission4)).build();

        final Submission submission2 = Submission.builder().id(submissionId2).scheme(schemeEntity).build();

        final GrantMandatoryQuestions mandatoryQuestions2 = GrantMandatoryQuestions.builder().id(mandatoryQuestion2Id)
                .schemeEntity(schemeEntity).submission(submission2).name("name").addressLine1("addressLine1")
                .city("city").postcode("g315sx").orgType(GrantMandatoryQuestionOrgType.CHARITY)
                .fundingAmount(BigDecimal.valueOf(1000))
                .fundingLocation(
                        new GrantMandatoryQuestionFundingLocation[] { GrantMandatoryQuestionFundingLocation.MIDLANDS })
                .status(GrantMandatoryQuestionStatus.COMPLETED).version(1).created(now).lastUpdated(now)
                .createdBy(applicant2).lastUpdatedBy(applicant2).gapId("GAP987").build();

        final SpotlightSubmission spotlightSubmission2 = SpotlightSubmission.builder().id(spotlightSubmissionId2)
                .mandatoryQuestions(mandatoryQuestions2).grantScheme(schemeEntity)
                .status(SpotlightSubmissionStatus.QUEUED.toString()).lastSendAttempt(now).version(1).created(now)
                .lastUpdated(now).build();

        final Submission submission3 = Submission.builder().id(submissionId3).scheme(schemeEntity2).build();

        final GrantMandatoryQuestions mandatoryQuestions3 = GrantMandatoryQuestions.builder().id(mandatoryQuestionsId3)
                .schemeEntity(schemeEntity2).submission(submission3).name("MqName").addressLine1("MqaddressLine1")
                .city("city").postcode("g315sx").orgType(GrantMandatoryQuestionOrgType.CHARITY)
                .fundingAmount(BigDecimal.valueOf(1000))
                .fundingLocation(
                        new GrantMandatoryQuestionFundingLocation[] { GrantMandatoryQuestionFundingLocation.MIDLANDS })
                .status(GrantMandatoryQuestionStatus.COMPLETED).version(1).created(now).lastUpdated(now)
                .createdBy(applicant2).lastUpdatedBy(applicant2).gapId("GAP987").build();

        final SpotlightSubmission spotlightSubmission3 = SpotlightSubmission.builder().id(spotlightSubmissionId3)
                .mandatoryQuestions(mandatoryQuestions3).grantScheme(schemeEntity2)
                .status(SpotlightSubmissionStatus.QUEUED.toString()).lastSendAttempt(now).version(1).created(now)
                .lastUpdated(now).build();

        final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(spotlightBatchId)
                .status(SpotlightBatchStatus.QUEUED).lastSendAttempt(now).version(1).created(now).lastUpdated(now)
                .spotlightSubmissions(List.of(spotlightSubmission, spotlightSubmission2, spotlightSubmission3)).build();

        final List<SpotlightBatch> spotlightBatches = List.of(spotlightBatch, spotlightBatch2);

        @Test
        void successfullyGenerateSendToSpotlightDto() {
            final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder()
                    .addressLine1(mandatoryQuestions.getAddressLine1())
                    .ggisSchemeId(mandatoryQuestions.getSchemeEntity().getGgisIdentifier()).build();

            final DraftAssessmentDto draftAssessmentDto2 = DraftAssessmentDto.builder()
                    .addressLine1(mandatoryQuestions2.getAddressLine1())
                    .ggisSchemeId(mandatoryQuestions2.getSchemeEntity().getGgisIdentifier()).build();

            final DraftAssessmentDto draftAssessmentDto3 = DraftAssessmentDto.builder()
                    .addressLine1(mandatoryQuestions3.getAddressLine1())
                    .ggisSchemeId(mandatoryQuestions3.getSchemeEntity().getGgisIdentifier()).build();

            final DraftAssessmentDto draftAssessmentDto4 = DraftAssessmentDto.builder()
                    .addressLine1(mandatoryQuestions4.getAddressLine1())
                    .ggisSchemeId(mandatoryQuestions4.getSchemeEntity().getGgisIdentifier())
                    .addressPostcode(mandatoryQuestions4.getPostcode())
                    .applicationAmount(mandatoryQuestions4.getFundingAmount().toString())
                    .applicationNumber(mandatoryQuestions4.getSubmission().getId().toString()).country("United Kingdom")
                    .charityCommissionRegNo(mandatoryQuestions4.getCharityCommissionNumber())
                    .companiesHouseRegNo(mandatoryQuestions4.getCompaniesHouseNumber())
                    .organisationName(mandatoryQuestions4.getName())
                    .organisationType(mandatoryQuestions4.getOrgType().toString())
                    .cityTown(mandatoryQuestions4.getCity()).funderID("funderId")

                    .build();

            when(spotlightBatchRepository.findByStatus(any())).thenReturn(Optional.of(spotlightBatches));
            when(mandatoryQuestionsMapper.mandatoryQuestionsToDraftAssessmentDto(any())).thenReturn(draftAssessmentDto)
                    .thenReturn(draftAssessmentDto2).thenReturn(draftAssessmentDto3).thenReturn(draftAssessmentDto4);

            final List<SendToSpotlightDto> result = spotlightBatchService
                    .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

            assertThat(result).hasSize(2);

            // DTO 1
            final SendToSpotlightDto dto1 = result.get(0);

            assertThat(dto1.getSchemes()).hasSize(2);

            final SpotlightSchemeDto dto1Scheme1 = dto1.getSchemes().get(0);

            assertThat(dto1Scheme1.getGgisSchemeId()).isEqualTo("ggisId1");
            assertThat(dto1Scheme1.getDraftAssessments()).hasSize(2);

            final DraftAssessmentDto dto1Scheme1DraftAssessment1 = dto1Scheme1.getDraftAssessments().get(0);

            assertThat(dto1Scheme1DraftAssessment1.getAddressLine1()).isEqualTo(mandatoryQuestions.getAddressLine1());
            assertThat(dto1Scheme1DraftAssessment1.getGgisSchemeId())
                    .isEqualTo(mandatoryQuestions.getSchemeEntity().getGgisIdentifier());

            final DraftAssessmentDto dto1Scheme1DraftAssessment2 = dto1Scheme1.getDraftAssessments().get(1);

            assertThat(dto1Scheme1DraftAssessment2.getAddressLine1()).isEqualTo(mandatoryQuestions2.getAddressLine1());
            assertThat(dto1Scheme1DraftAssessment2.getGgisSchemeId())
                    .isEqualTo(mandatoryQuestions2.getSchemeEntity().getGgisIdentifier());

            final SpotlightSchemeDto dto1Scheme2 = dto1.getSchemes().get(1);

            assertThat(dto1Scheme2.getGgisSchemeId()).isEqualTo("ggisId2");
            assertThat(dto1Scheme2.getDraftAssessments()).hasSize(1);

            final DraftAssessmentDto dto1Scheme2DraftAssessment1 = dto1Scheme2.getDraftAssessments().get(0);

            assertThat(dto1Scheme2DraftAssessment1.getAddressLine1()).isEqualTo(mandatoryQuestions3.getAddressLine1());
            assertThat(dto1Scheme2DraftAssessment1.getGgisSchemeId())
                    .isEqualTo(mandatoryQuestions3.getSchemeEntity().getGgisIdentifier());

            // DTO 2
            final SendToSpotlightDto dto2 = result.get(1);

            assertThat(dto2.getSchemes()).hasSize(1);

            final SpotlightSchemeDto dto2Scheme1 = dto2.getSchemes().get(0);

            assertThat(dto2Scheme1.getGgisSchemeId()).isEqualTo("ggisId1");
            assertThat(dto2Scheme1.getDraftAssessments()).hasSize(1);

            final DraftAssessmentDto dto2Scheme1DraftAssessment1 = dto2Scheme1.getDraftAssessments().get(0);

            assertThat(dto2Scheme1DraftAssessment1.getAddressLine1()).isEqualTo(mandatoryQuestions4.getAddressLine1());
            assertThat(dto2Scheme1DraftAssessment1.getGgisSchemeId())
                    .isEqualTo(mandatoryQuestions4.getSchemeEntity().getGgisIdentifier());

        }

        @Test
        void generateSendToSpotlightDto__returnEmptyList() {
            when(spotlightBatchRepository.findByStatus(any())).thenReturn(Optional.empty());

            final List<SendToSpotlightDto> result = spotlightBatchService
                    .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

            assertThat(result).isEqualTo(List.of());
        }

    }

    @Nested
    class getSpotlightBatchByMandatoryQuestionGapId{

     	@Test
     	void getSpotlightBatchByMandatoryQuestionGapId_success() {
     		final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(uuid).build();


     		when(spotlightBatchRepository.findBySpotlightSubmissions_MandatoryQuestions_GapId(any())).thenReturn(Optional.of(spotlightBatch));

     		final SpotlightBatch result = spotlightBatchService.getSpotlightBatchByMandatoryQuestionGapId("GAP123");

     		assertThat(result).isEqualTo(spotlightBatch);
     	}

     	@Test
     	void getSpotlightBatchByMandatoryQuestionGapId_notFound() {
     		when(spotlightBatchRepository.findBySpotlightSubmissions_MandatoryQuestions_GapId(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> spotlightBatchService.getSpotlightBatchByMandatoryQuestionGapId("GAP123"));
     	}
    }

    @Nested
    class sendQueuedBatchesToSpotlight {

        @Test
        void sendQueuedBatchesToSpotlight_success() throws JsonProcessingException {
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().build();
            final List<SendToSpotlightDto> sendToSpotlightDtos = List.of(sendToSpotlightDto);
            final GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                    .secretString("{\"access_token\":\"token\"}").build();

            doReturn(sendToSpotlightDtos).when(spotlightBatchService)
                    .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);
            when(secretsManagerClient.getSecretValue((GetSecretValueRequest) any())).thenReturn(getSecretValueResponse);
            when(restTemplate.postForObject(any(), eq(RequestEntity.class), eq(String.class))).thenReturn("success");

            spotlightBatchService.sendQueuedBatchesToSpotlightAndProcessThem();

            verify(spotlightBatchService, times(1)).generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

            verify(restTemplate, times(1)).postForObject(eq("spotlightUrl/services/apexrest/DraftAssessments"), any(),
                    eq(String.class));

        }

        @Test
        void sendQueuedBatchesToSpotlight_throwsJsonParseException() throws JsonProcessingException {
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().build();
            final List<SendToSpotlightDto> sendToSpotlightDtos = List.of(sendToSpotlightDto);
            final GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                    .secretString("{\"access_token\":\"token\"}").build();

            doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsString(sendToSpotlightDto);
            doReturn(sendToSpotlightDtos).when(spotlightBatchService)
                    .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);
            when(secretsManagerClient.getSecretValue((GetSecretValueRequest) any())).thenReturn(getSecretValueResponse);

            assertThrows(JsonParseException.class, () -> spotlightBatchService.sendQueuedBatchesToSpotlightAndProcessThem());

            verify(spotlightBatchService, times(1)).generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

        }

        @Test
        void sendQueuedBatchesToSpotlight_throwsSecretValueException() throws JsonProcessingException {
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().build();
            final List<SendToSpotlightDto> sendToSpotlightDtos = List.of(sendToSpotlightDto);
            final GetSecretValueResponse getSecretValueResponse = GetSecretValueResponse.builder()
                    .secretString("Wrong Json").build();

            doReturn(sendToSpotlightDtos).when(spotlightBatchService)
                    .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);
            when(secretsManagerClient.getSecretValue((GetSecretValueRequest) any())).thenReturn(getSecretValueResponse);

            assertThrows(SecretValueException.class, () -> spotlightBatchService.sendQueuedBatchesToSpotlightAndProcessThem());

            verify(spotlightBatchService, times(1)).generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

        }

    }

}
