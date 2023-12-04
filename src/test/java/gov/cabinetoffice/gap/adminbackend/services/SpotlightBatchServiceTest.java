package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.SpotlightConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.SpotlightQueueConfigProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.DraftAssessmentDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SendToSpotlightDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.SpotlightSchemeDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response.DraftAssessmentResponseDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response.MasterSchemeStatusDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response.SpotlightResponseDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response.SpotlightResponseResultsDto;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.GetSpotlightBatchErrorCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.entities.*;
import gov.cabinetoffice.gap.adminbackend.enums.*;
import gov.cabinetoffice.gap.adminbackend.exceptions.JsonParseException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SecretValueException;
import gov.cabinetoffice.gap.adminbackend.mappers.MandatoryQuestionsMapper;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightBatchRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static gov.cabinetoffice.gap.adminbackend.enums.DraftAssessmentResponseDtoStatus.FAILURE;
import static gov.cabinetoffice.gap.adminbackend.enums.DraftAssessmentResponseDtoStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@WithAdminSession
class SpotlightBatchServiceTest {

    private static final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final String APPLICATION_NUMBER = "GAP-an-environment-name-20231115-1-5550";

    Pageable pageable = PageRequest.of(0, 1);

    @Mock
    private GrantMandatoryQuestionService grantMandatoryQuestionService;

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

    @Mock
    private AmazonSQS amazonSqs;

    @Mock
    private SnsService snsService;

    @Mock
    private SpotlightSubmissionService spotlightSubmissionService;

    @Mock
    private SpotlightSubmissionRepository spotlightSubmissionRepository;

    @Captor
    private ArgumentCaptor<SpotlightSubmission> argumentCaptor;

    @BeforeEach
    void setup() {
        spotlightConfigProperties = SpotlightConfigProperties.builder().spotlightUrl("spotlightUrl")
                .secretName("secretName").build();
        objectMapper = Mockito.spy(new ObjectMapper());
        spotlightQueueProperties = SpotlightQueueConfigProperties.builder().queueUrl("queueUrl").build();
        spotlightBatchService = Mockito.spy(new SpotlightBatchService(spotlightBatchRepository,
                mandatoryQuestionsMapper, secretsManagerClient, restTemplate, spotlightSubmissionRepository,
                spotlightConfigProperties, objectMapper, spotlightQueueProperties, amazonSqs,
                spotlightSubmissionService, grantMandatoryQuestionService, snsService));
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
    class getSpotlightBatchByMandatoryQuestionGapId {

        @Test
        void getSpotlightBatchByMandatoryQuestionGapId_success() {
            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(uuid).build();

            when(spotlightBatchRepository.findByStatusAndSpotlightSubmissions_MandatoryQuestions_GapId(any(), any()))
                    .thenReturn(Optional.of(spotlightBatch));

            final SpotlightBatch result = spotlightBatchService
                    .getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId("GAP123");

            assertThat(result).isEqualTo(spotlightBatch);
        }

        @Test
        void getSpotlightBatchByMandatoryQuestionGapId_notFound() {
            when(spotlightBatchRepository.findByStatusAndSpotlightSubmissions_MandatoryQuestions_GapId(any(), any()))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> spotlightBatchService.getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId("GAP123"));
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
            final SpotlightResponseResultsDto spotlightResponseResults = SpotlightResponseResultsDto.builder().build();

            doReturn(sendToSpotlightDtos).when(spotlightBatchService)
                    .generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);
            when(secretsManagerClient.getSecretValue((GetSecretValueRequest) any())).thenReturn(getSecretValueResponse);
            doReturn(spotlightResponseResults).when(spotlightBatchService).sendBatchToSpotlight(sendToSpotlightDto,
                    "token");
            doNothing().when(spotlightBatchService).processSpotlightResponse(sendToSpotlightDto,
                    spotlightResponseResults);

            spotlightBatchService.sendQueuedBatchesToSpotlightAndProcessThem();

            verify(spotlightBatchService, times(1)).generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);
            verify(spotlightBatchService, times(1)).processSpotlightResponse(sendToSpotlightDto,
                    spotlightResponseResults);

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

            assertThrows(JsonParseException.class,
                    () -> spotlightBatchService.sendQueuedBatchesToSpotlightAndProcessThem());

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

            assertThrows(SecretValueException.class,
                    () -> spotlightBatchService.sendQueuedBatchesToSpotlightAndProcessThem());

            verify(spotlightBatchService, times(1)).generateSendToSpotlightDtosList(SpotlightBatchStatus.QUEUED);

        }

    }

    @Nested
    class processSpotlightResponse {

        @Test
        void spotlightResponsesResultIsNull() {
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().build();
            final SpotlightResponseResultsDto spotlightResponseResults = SpotlightResponseResultsDto.builder().build();

            doNothing().when(spotlightBatchService).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);
            doNothing().when(spotlightBatchService).updateSpotlightSubmissionStatus(sendToSpotlightDto,
                    SpotlightSubmissionStatus.SEND_ERROR);
            doNothing().when(spotlightBatchService).addMessageToQueue(sendToSpotlightDto);

            spotlightBatchService.processSpotlightResponse(sendToSpotlightDto, spotlightResponseResults);

            verify(spotlightBatchService, times(1)).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);
            verify(spotlightBatchService, times(1)).updateSpotlightSubmissionStatus(sendToSpotlightDto,
                    SpotlightSubmissionStatus.SEND_ERROR);
            verify(spotlightBatchService, times(1)).addMessageToQueue(sendToSpotlightDto);

        }

        @Test
        void spotlightResponsesResultIsNotNullAndDraftAssessmentStatusIsSuccess() {
            final DraftAssessmentResponseDto draftAssessmentResponseDto = DraftAssessmentResponseDto.builder()
                    .status(SUCCESS.toString()).applicationNumber("applicationNumber").build();
            final SpotlightResponseDto response = SpotlightResponseDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessmentsResults(List.of(draftAssessmentResponseDto)).build();
            final SpotlightResponseResultsDto spotlightResponseResults = SpotlightResponseResultsDto.builder()
                    .results(List.of(response)).build();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(uuid)
                    .status(SpotlightSubmissionStatus.QUEUED.toString()).build();
            final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().ggisSchemeId("ggisId1")
                    .applicationNumber("applicationNumber").build();
            final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessments(List.of(draftAssessmentDto)).build();
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder()
                    .schemes(List.of(spotlightSchemeDto)).build();

            when(spotlightSubmissionService.getSpotlightSubmissionByMandatoryQuestionGapId("applicationNumber"))
                    .thenReturn(spotlightSubmission);

            doNothing().when(spotlightBatchService).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.SUCCESS);

            spotlightBatchService.processSpotlightResponse(sendToSpotlightDto, spotlightResponseResults);

            verify(spotlightSubmissionRepository).save(argumentCaptor.capture());
            final SpotlightSubmission result = argumentCaptor.getValue();

            assertThat(result.getStatus()).isEqualTo(SpotlightSubmissionStatus.SENT.toString());

            verify(spotlightBatchService, times(1)).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.SUCCESS);

        }

        @Test
        void spotlightResponsesResultIsNotNullAndDraftAssessmentStatusIsFailureAndMessageIsNull() {
            final DraftAssessmentResponseDto draftAssessmentResponseDto = DraftAssessmentResponseDto.builder()
                    .status("anyNonSuccessStatus").applicationNumber("applicationNumber").build();
            final SpotlightResponseDto response = SpotlightResponseDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessmentsResults(List.of(draftAssessmentResponseDto)).build();
            final SpotlightResponseResultsDto spotlightResponseResults = SpotlightResponseResultsDto.builder()
                    .results(List.of(response)).build();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(uuid)
                    .status(SpotlightSubmissionStatus.QUEUED.toString()).build();
            final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().ggisSchemeId("ggisId1")
                    .applicationNumber("applicationNumber").build();
            final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessments(List.of(draftAssessmentDto)).build();
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder()
                    .schemes(List.of(spotlightSchemeDto)).build();

            when(spotlightSubmissionService.getSpotlightSubmissionByMandatoryQuestionGapId("applicationNumber"))
                    .thenReturn(spotlightSubmission);

            doNothing().when(spotlightBatchService).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);
            doNothing().when(spotlightBatchService).sendMessageToQueue(spotlightSubmission);

            spotlightBatchService.processSpotlightResponse(sendToSpotlightDto, spotlightResponseResults);

            verify(spotlightSubmissionRepository).save(argumentCaptor.capture());
            final SpotlightSubmission result = argumentCaptor.getValue();

            assertThat(result.getStatus()).isEqualTo(SpotlightSubmissionStatus.SEND_ERROR.toString());

            verify(spotlightBatchService, times(1)).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);
            verify(spotlightBatchService, times(1)).sendMessageToQueue(spotlightSubmission);
        }

        @Test
        void spotlightResponsesResultIsNotNullAndDraftAssessmentStatusIsFailureAndMessageIsFor406error() {
            final DraftAssessmentResponseDto draftAssessmentResponseDto = DraftAssessmentResponseDto.builder()
                    .status(FAILURE.toString()).applicationNumber("applicationNumber")
                    .message("Scheme Does Not Exist here").build();
            final SpotlightResponseDto response = SpotlightResponseDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessmentsResults(List.of(draftAssessmentResponseDto)).build();
            final SpotlightResponseResultsDto spotlightResponseResults = SpotlightResponseResultsDto.builder()
                    .results(List.of(response)).build();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(uuid)
                    .status(SpotlightSubmissionStatus.QUEUED.toString()).build();
            final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().ggisSchemeId("ggisId1")
                    .applicationNumber("applicationNumber").build();
            final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessments(List.of(draftAssessmentDto)).build();
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder()
                    .schemes(List.of(spotlightSchemeDto)).build();

            when(spotlightSubmissionService.getSpotlightSubmissionByMandatoryQuestionGapId("applicationNumber"))
                    .thenReturn(spotlightSubmission);

            doNothing().when(spotlightBatchService).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);
            doNothing().when(spotlightBatchService).sendMessageToQueue(spotlightSubmission);

            spotlightBatchService.processSpotlightResponse(sendToSpotlightDto, spotlightResponseResults);

            verify(spotlightSubmissionRepository).save(argumentCaptor.capture());
            final SpotlightSubmission result = argumentCaptor.getValue();

            assertThat(result.getStatus()).isEqualTo(SpotlightSubmissionStatus.GGIS_ERROR.toString());

            verify(spotlightBatchService, times(1)).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);
            verify(spotlightBatchService, times(1)).sendMessageToQueue(spotlightSubmission);
        }

        @Test
        void spotlightResponsesResultIsNotNullAndDraftAssessmentStatusIsFailureAndMessageIsFor409error__FieldMissing() {
            final DraftAssessmentResponseDto draftAssessmentResponseDto = DraftAssessmentResponseDto.builder()
                    .status(FAILURE.toString()).applicationNumber("applicationNumber")
                    .message("Required fields are missing").build();
            final SpotlightResponseDto response = SpotlightResponseDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessmentsResults(List.of(draftAssessmentResponseDto)).build();
            final SpotlightResponseResultsDto spotlightResponseResults = SpotlightResponseResultsDto.builder()
                    .results(List.of(response)).build();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(uuid)
                    .status(SpotlightSubmissionStatus.QUEUED.toString()).build();
            final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().ggisSchemeId("ggisId1")
                    .applicationNumber("applicationNumber").build();
            final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessments(List.of(draftAssessmentDto)).build();
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder()
                    .schemes(List.of(spotlightSchemeDto)).build();

            when(spotlightSubmissionService.getSpotlightSubmissionByMandatoryQuestionGapId("applicationNumber"))
                    .thenReturn(spotlightSubmission);

            doNothing().when(spotlightBatchService).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);

            spotlightBatchService.processSpotlightResponse(sendToSpotlightDto, spotlightResponseResults);

            verify(spotlightSubmissionRepository).save(argumentCaptor.capture());
            final SpotlightSubmission result = argumentCaptor.getValue();

            assertThat(result.getStatus()).isEqualTo(SpotlightSubmissionStatus.VALIDATION_ERROR.toString());

            verify(spotlightBatchService, times(1)).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);
            verify(snsService, times(1)).spotlightValidationError();
        }

        @Test
        void spotlightResponsesResultIsNotNullAndDraftAssessmentStatusIsFailureAndMessageIsFor409error__ValueTooLong() {
            final DraftAssessmentResponseDto draftAssessmentResponseDto = DraftAssessmentResponseDto.builder()
                    .status(FAILURE.toString()).applicationNumber("applicationNumber").message("data value too large")
                    .build();
            final SpotlightResponseDto response = SpotlightResponseDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessmentsResults(List.of(draftAssessmentResponseDto)).build();
            final SpotlightResponseResultsDto spotlightResponseResults = SpotlightResponseResultsDto.builder()
                    .results(List.of(response)).build();
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(uuid)
                    .status(SpotlightSubmissionStatus.QUEUED.toString()).build();
            final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().ggisSchemeId("ggisId1")
                    .applicationNumber("applicationNumber").build();
            final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder().ggisSchemeId("ggisId1")
                    .draftAssessments(List.of(draftAssessmentDto)).build();
            final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder()
                    .schemes(List.of(spotlightSchemeDto)).build();

            when(spotlightSubmissionService.getSpotlightSubmissionByMandatoryQuestionGapId("applicationNumber"))
                    .thenReturn(spotlightSubmission);

            doNothing().when(spotlightBatchService).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);

            spotlightBatchService.processSpotlightResponse(sendToSpotlightDto, spotlightResponseResults);

            verify(spotlightSubmissionRepository).save(argumentCaptor.capture());
            final SpotlightSubmission result = argumentCaptor.getValue();

            assertThat(result.getStatus()).isEqualTo(SpotlightSubmissionStatus.VALIDATION_ERROR.toString());

            verify(spotlightBatchService, times(1)).updateSpotlightBatchStatus(sendToSpotlightDto,
                    SpotlightBatchStatus.FAILURE);
            verify(snsService, times(1)).spotlightValidationError();
        }

    }

    @Nested
    class sendBatchToSpotlight {

        final String accessToken = "an-access-token";

        final SendToSpotlightDto batch = SendToSpotlightDto.builder().build();

        final String batchAsJson = "{}";

        @Mock
        HttpClientErrorException clientErrorException;

        @Mock
        HttpServerErrorException serverErrorException;

        @Test
        void success() throws JsonProcessingException {

            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Bearer " + accessToken);
            requestHeaders.add("Content-Type", "application/json");

            final HttpEntity<String> requestEntity = new HttpEntity<>(batchAsJson, requestHeaders);

            final ResponseEntity<String> httpResponse = ResponseEntity.ok().body(
                    "[  { \"GGISSchemeID\": \"GG-55555-0987\", \"MasterSchemeStatus\": { \"Exists\": true, \"Message\": null }, \"DraftAssessmentsResults\": [  { \"ApplicationNumber\": \"GAP-an-environment-name-20231115-1-5550\", \"Status\": \"Success\", \"Message\": null, \"Id\": \"SFS-GAP-an-environment-name-20231115-1-5550\" }  ] }  ]");

            final DraftAssessmentResponseDto draftAssessmentResponse = DraftAssessmentResponseDto.builder()
                    .status("Success").applicationNumber("GAP-an-environment-name-20231115-1-5550")
                    .id("SFS-GAP-an-environment-name-20231115-1-5550").build();

            final MasterSchemeStatusDto masterSchemeStatus = MasterSchemeStatusDto.builder().message(null).exists(true)
                    .build();

            final SpotlightResponseDto responseDto = SpotlightResponseDto.builder().ggisSchemeId("GG-55555-0987")
                    .masterSchemeStatus(masterSchemeStatus).draftAssessmentsResults(List.of(draftAssessmentResponse))
                    .build();

            final SpotlightResponseResultsDto expectedResponse = SpotlightResponseResultsDto.builder()
                    .results(List.of(responseDto)).build();

            when(objectMapper.writeValueAsString(batch)).thenReturn(batchAsJson);

            when(restTemplate.postForEntity(
                    spotlightConfigProperties.getSpotlightUrl() + "/services/apexrest/DraftAssessments", requestEntity,
                    String.class)).thenReturn(httpResponse);

            final SpotlightResponseResultsDto methodResponse = spotlightBatchService.sendBatchToSpotlight(batch,
                    accessToken);

            assertThat(methodResponse).isEqualTo(expectedResponse);
        }

        @Test
        void clientError_ThatCanBeHandled() throws JsonProcessingException {

            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Bearer " + accessToken);
            requestHeaders.add("Content-Type", "application/json");

            final HttpEntity<String> requestEntity = new HttpEntity<>(batchAsJson, requestHeaders);

            final DraftAssessmentResponseDto draftAssessmentResponse = DraftAssessmentResponseDto.builder()
                    .status("Failure").applicationNumber("GAP-an-environment-name-20231115-1-5550")
                    .id("SFS-GAP-an-environment-name-20231115-1-5550").message("Scheme Does Not Exist").build();

            final MasterSchemeStatusDto masterSchemeStatus = MasterSchemeStatusDto.builder().message(null).exists(false)
                    .build();

            final SpotlightResponseDto responseDto = SpotlightResponseDto.builder().ggisSchemeId("GG-55555-0987")
                    .masterSchemeStatus(masterSchemeStatus).draftAssessmentsResults(List.of(draftAssessmentResponse))
                    .build();

            final SpotlightResponseResultsDto expectedResponse = SpotlightResponseResultsDto.builder()
                    .results(List.of(responseDto)).build();

            when(objectMapper.writeValueAsString(batch)).thenReturn(batchAsJson);

            when(restTemplate.postForEntity(
                    spotlightConfigProperties.getSpotlightUrl() + "/services/apexrest/DraftAssessments", requestEntity,
                    String.class)).thenThrow(clientErrorException);

            when(clientErrorException.getStatusCode()).thenReturn(HttpStatus.NOT_ACCEPTABLE);

            when(clientErrorException.getResponseBodyAsString()).thenReturn(
                    "[  { \"GGISSchemeID\": \"GG-55555-0987\", \"MasterSchemeStatus\": { \"Exists\": false, \"Message\": null }, \"DraftAssessmentsResults\": [  { \"ApplicationNumber\": \"GAP-an-environment-name-20231115-1-5550\", \"Status\": \"Failure\", \"Message\": \"Scheme Does Not Exist\", \"Id\": \"SFS-GAP-an-environment-name-20231115-1-5550\" }  ] }  ]");

            final SpotlightResponseResultsDto methodResponse = spotlightBatchService.sendBatchToSpotlight(batch,
                    accessToken);

            assertThat(methodResponse).isEqualTo(expectedResponse);
        }

        @Test
        void clientError_ThatCannotBeHandled() throws JsonProcessingException {

            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Bearer " + accessToken);
            requestHeaders.add("Content-Type", "application/json");

            final HttpEntity<String> requestEntity = new HttpEntity<>(batchAsJson, requestHeaders);

            final SpotlightResponseResultsDto expectedResponse = SpotlightResponseResultsDto.builder().build();

            when(objectMapper.writeValueAsString(batch)).thenReturn(batchAsJson);

            when(restTemplate.postForEntity(
                    spotlightConfigProperties.getSpotlightUrl() + "/services/apexrest/DraftAssessments", requestEntity,
                    String.class)).thenThrow(clientErrorException);

            when(clientErrorException.getStatusCode()).thenReturn(HttpStatus.I_AM_A_TEAPOT); // we
                                                                                             // like
                                                                                             // to
                                                                                             // do
                                                                                             // a
                                                                                             // little
                                                                                             // trolling
                                                                                             // ;)

            final SpotlightResponseResultsDto methodResponse = spotlightBatchService.sendBatchToSpotlight(batch,
                    accessToken);

            assertThat(methodResponse).isEqualTo(expectedResponse);
        }

        @Test
        void serverError() throws JsonProcessingException {

            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Bearer " + accessToken);
            requestHeaders.add("Content-Type", "application/json");

            final HttpEntity<String> requestEntity = new HttpEntity<>(batchAsJson, requestHeaders);

            final SpotlightResponseResultsDto expectedResponse = SpotlightResponseResultsDto.builder().build();

            when(objectMapper.writeValueAsString(batch)).thenReturn(batchAsJson);

            when(restTemplate.postForEntity(
                    spotlightConfigProperties.getSpotlightUrl() + "/services/apexrest/DraftAssessments", requestEntity,
                    String.class)).thenThrow(serverErrorException);

            when(serverErrorException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

            final SpotlightResponseResultsDto methodResponse = spotlightBatchService.sendBatchToSpotlight(batch,
                    accessToken);

            verify(snsService).spotlightApiError();
            assertThat(methodResponse).isEqualTo(expectedResponse);
        }

        @Test
        void throwsJsonParseException() throws JsonProcessingException {
            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Bearer " + accessToken);
            requestHeaders.add("Content-Type", "application/json");

            final HttpEntity<String> requestEntity = new HttpEntity<>(batchAsJson, requestHeaders);

            final String responseAsString = "[  { \"GGISSchemeID\": \"GG-55555-0987\", \"MasterSchemeStatus\": { \"Exists\": true, \"Message\": null }, \"DraftAssessmentsResults\": [  { \"ApplicationNumber\": \"GAP-an-environment-name-20231115-1-5550\", \"Status\": \"Success\", \"Message\": null, \"Id\": \"SFS-GAP-an-environment-name-20231115-1-5550\" }  ] }  ]";
            final ResponseEntity<String> httpResponse = ResponseEntity.ok().body(responseAsString);

            final DraftAssessmentResponseDto draftAssessmentResponse = DraftAssessmentResponseDto.builder()
                    .status("Success").applicationNumber("GAP-an-environment-name-20231115-1-5550")
                    .id("SFS-GAP-an-environment-name-20231115-1-5550").build();

            final MasterSchemeStatusDto masterSchemeStatus = MasterSchemeStatusDto.builder().message(null).exists(true)
                    .build();

            final SpotlightResponseDto responseDto = SpotlightResponseDto.builder().ggisSchemeId("GG-55555-0987")
                    .masterSchemeStatus(masterSchemeStatus).draftAssessmentsResults(List.of(draftAssessmentResponse))
                    .build();

            final SpotlightResponseResultsDto expectedResponse = SpotlightResponseResultsDto.builder()
                    .results(List.of(responseDto)).build();

            when(objectMapper.writeValueAsString(batch)).thenReturn(batchAsJson);

            when(restTemplate.postForEntity(
                    spotlightConfigProperties.getSpotlightUrl() + "/services/apexrest/DraftAssessments", requestEntity,
                    String.class)).thenReturn(httpResponse);

            when(objectMapper.readValue(responseAsString, SpotlightResponseDto[].class))
                    .thenThrow(JsonProcessingException.class);

            assertThrows(JsonParseException.class,
                    () -> spotlightBatchService.sendBatchToSpotlight(batch, accessToken));
        }

        @Test
        void unauthorizedError() throws JsonProcessingException {
            final HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Bearer " + accessToken);
            requestHeaders.add("Content-Type", "application/json");

            final HttpEntity<String> requestEntity = new HttpEntity<>(batchAsJson, requestHeaders);

            when(objectMapper.writeValueAsString(batch)).thenReturn(batchAsJson);

            when(restTemplate.postForEntity(
                    spotlightConfigProperties.getSpotlightUrl() + "/services/apexrest/DraftAssessments", requestEntity,
                    String.class)).thenThrow(clientErrorException);

            when(clientErrorException.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);

            spotlightBatchService.sendBatchToSpotlight(batch, accessToken);

            verify(snsService).spotlightOAuthDisconnected();
        }

    }

    @Nested
    class updateSpotlightBatchStatus {

        final SpotlightBatch spotlightBatch = SpotlightBatch.builder().build();

        final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().applicationNumber(APPLICATION_NUMBER)
                .build();

        final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder()
                .draftAssessments(List.of(draftAssessmentDto)).build();

        final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().schemes(List.of(spotlightSchemeDto))
                .build();

        final SpotlightBatchStatus status = SpotlightBatchStatus.SUCCESS;

        @Captor
        ArgumentCaptor<SpotlightBatch> batchCaptor;

        @Test
        void success() {

            doReturn(spotlightBatch).when(spotlightBatchService)
                    .getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId(APPLICATION_NUMBER);

            spotlightBatchService.updateSpotlightBatchStatus(sendToSpotlightDto, status);

            verify(spotlightBatchRepository).save(batchCaptor.capture());

            final SpotlightBatch capturedBatch = batchCaptor.getValue();

            assertThat(capturedBatch.getStatus()).isEqualTo(status);
            assertThat(capturedBatch.getLastUpdated()).isNotNull();
            assertThat(capturedBatch.getLastSendAttempt()).isNotNull();
        }

    }

    @Nested
    class updateSpotlightSubmissionStatus {

        final SpotlightSubmission submission = SpotlightSubmission.builder().build();

        final SpotlightBatch spotlightBatch = SpotlightBatch.builder().spotlightSubmissions(List.of(submission))
                .build();

        final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().applicationNumber(APPLICATION_NUMBER)
                .build();

        final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder()
                .draftAssessments(List.of(draftAssessmentDto)).build();

        final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().schemes(List.of(spotlightSchemeDto))
                .build();

        final SpotlightSubmissionStatus status = SpotlightSubmissionStatus.SENT;

        @Captor
        ArgumentCaptor<SpotlightBatch> batchCaptor;

        @Test
        void success() {

            doReturn(spotlightBatch).when(spotlightBatchService)
                    .getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId(APPLICATION_NUMBER);

            spotlightBatchService.updateSpotlightSubmissionStatus(sendToSpotlightDto, status);

            verify(spotlightBatchRepository).save(batchCaptor.capture());

            final SpotlightBatch capturedBatch = batchCaptor.getValue();

            capturedBatch.getSpotlightSubmissions().forEach(s -> {
                assertThat(s.getStatus()).isEqualTo(status.toString());
                assertThat(s.getLastUpdated()).isNotNull();
                assertThat(s.getLastSendAttempt()).isNotNull();
            });
        }

    }

    @Nested
    class addMessageToQueue {

        final SpotlightSubmission submission = SpotlightSubmission.builder().build();

        final SpotlightBatch spotlightBatch = SpotlightBatch.builder().spotlightSubmissions(List.of(submission))
                .build();

        final DraftAssessmentDto draftAssessmentDto = DraftAssessmentDto.builder().applicationNumber(APPLICATION_NUMBER)
                .build();

        final SpotlightSchemeDto spotlightSchemeDto = SpotlightSchemeDto.builder()
                .draftAssessments(List.of(draftAssessmentDto)).build();

        final SendToSpotlightDto sendToSpotlightDto = SendToSpotlightDto.builder().schemes(List.of(spotlightSchemeDto))
                .build();

        @Test
        void success() {
            doReturn(spotlightBatch).when(spotlightBatchService)
                    .getSpotlightBatchWithQueuedStatusByMandatoryQuestionGapId(APPLICATION_NUMBER);
            doNothing().when(spotlightBatchService).sendMessageToQueue(submission);

            spotlightBatchService.addMessageToQueue(sendToSpotlightDto);

            verify(spotlightBatchService, times(1)).sendMessageToQueue(submission);
        }

    }

    @Nested
    class sendMessageToQueue {

        final UUID spotlightSubmissionId = UUID.randomUUID();

        final SpotlightSubmission submission = SpotlightSubmission.builder().id(spotlightSubmissionId).build();

        @Captor
        ArgumentCaptor<SendMessageRequest> sqsRequestCaptor;

        @Test
        void success() {

            spotlightBatchService.sendMessageToQueue(submission);

            // Test that the spotlight submission has been sent to SQS
            verify(amazonSqs).sendMessage(sqsRequestCaptor.capture());

            final SendMessageRequest sqsRequest = sqsRequestCaptor.getValue();

            assertThat(sqsRequest.getMessageBody()).isNotNull();
            assertThat(sqsRequest.getQueueUrl()).isEqualTo(spotlightQueueProperties.getQueueUrl());
            assertThat(sqsRequest.getMessageBody()).isEqualTo(submission.getId().toString());
        }

    }

    @Nested
    class GetSpotlightBatchErrorCountTests {

        final Integer schemeId = 1;

        final SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId).build();

        final List<SpotlightBatch> spotlightBatches = Collections
                .singletonList(SpotlightBatch.builder().spotlightSubmissions(new ArrayList<>()).build());

        @Test
        void noSubmissionsForSchemeId() {
            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId);

            assertEquals(0, result.getErrorCount());
            assertEquals("OK", result.getErrorStatus());
            assertFalse(result.isErrorFound());
        }

        @Test
        void returnAPIError() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .status(SpotlightSubmissionStatus.SEND_ERROR.toString()).grantScheme(schemeEntity).build();
            spotlightBatches.get(0).getSpotlightSubmissions().add(spotlightSubmission);
            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId);

            assertTrue(result.getErrorCount() > 0);
            assertEquals("API", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void returnGGISError() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .status(SpotlightSubmissionStatus.GGIS_ERROR.toString()).grantScheme(schemeEntity).build();
            spotlightBatches.get(0).getSpotlightSubmissions().add(spotlightSubmission);
            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId);

            assertTrue(result.getErrorCount() > 0);
            assertEquals("GGIS", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void returnValidationError() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .status(SpotlightSubmissionStatus.VALIDATION_ERROR.toString()).grantScheme(schemeEntity).build();
            spotlightBatches.get(0).getSpotlightSubmissions().add(spotlightSubmission);
            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId);

            assertTrue(result.getErrorCount() > 0);
            assertEquals("VALIDATION", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

    }

    @Nested
    class OrderSpotlightErrorStatusesByPriorityTests {

        final int schemeId1 = 1;

        final int schemeId2 = 2;

        final SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId1).build();

        private SpotlightBatch createSpotlightBatchWithSubmissions(int schemeId,
                SpotlightSubmissionStatus... statuses) {
            final SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId).build();
            final List<SpotlightSubmission> spotlightSubmissions = Arrays.stream(statuses).map(
                    status -> SpotlightSubmission.builder().status(status.toString()).grantScheme(schemeEntity).build())
                    .collect(Collectors.toList());

            return SpotlightBatch.builder().spotlightSubmissions(spotlightSubmissions).build();
        }

        @Test
        void orderSpotlightErrorStatusesByHighestPriority_API() {
            final SpotlightBatch spotlightBatch = createSpotlightBatchWithSubmissions(schemeId1,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.SEND_ERROR,
                    SpotlightSubmissionStatus.VALIDATION_ERROR);
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(1, result.getErrorCount());
            assertEquals("API", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void orderSpotlightErrorStatusesBySecondPriority_GGIS() {
            final SpotlightBatch spotlightBatch = createSpotlightBatchWithSubmissions(schemeId1,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.VALIDATION_ERROR);
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(1, result.getErrorCount());
            assertEquals("GGIS", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void orderSpotlightErrorStatusesByLowestPriority_VALIDATION() {
            final SpotlightBatch spotlightBatch = createSpotlightBatchWithSubmissions(schemeId1,
                    SpotlightSubmissionStatus.VALIDATION_ERROR);
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(1, result.getErrorCount());
            assertEquals("VALIDATION", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void orderSpotlightErrorStatusesByPriorityAndFilterBySchemeId() {
            // Priority - API > GGIS > VALIDATION

            final SpotlightBatch spotlightBatch1 = createSpotlightBatchWithSubmissions(schemeId1,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.GGIS_ERROR,
                    SpotlightSubmissionStatus.VALIDATION_ERROR);

            final SpotlightBatch spotlightBatch2 = createSpotlightBatchWithSubmissions(schemeId2,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.SEND_ERROR);

            final List<SpotlightSubmission> spotlightSubmissions = Stream
                    .concat(spotlightBatch1.getSpotlightSubmissions().stream(),
                            spotlightBatch2.getSpotlightSubmissions().stream())
                    .toList();

            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().spotlightSubmissions(spotlightSubmissions)
                    .build();
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(2, result.getErrorCount());
            assertEquals("GGIS", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void orderSpotlightErrorStatusesByPriorityAndFilterBySchemeId_NoErrors() {
            final SpotlightBatch spotlightBatch = createSpotlightBatchWithSubmissions(schemeId2,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.SEND_ERROR,
                    SpotlightSubmissionStatus.VALIDATION_ERROR);
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(0, result.getErrorCount());
            assertEquals("OK", result.getErrorStatus());
            assertFalse(result.isErrorFound());
        }

    }

    @Nested
    class getFilteredSpotlightSubmissionsWithValidationErrors {

        final Integer schemeId = 1;

        final SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId).build();

        final List<SpotlightBatch> spotlightBatches = Collections
                .singletonList(SpotlightBatch.builder().spotlightSubmissions(new ArrayList<>()).build());

        private final GrantMandatoryQuestions grantMandatoryQuestions = GrantMandatoryQuestions.builder()
                .name("Some company name").addressLine1("9-10 St Andrew Square").city("Edinburgh").county("county")
                .postcode("EH2 2AF").charityCommissionNumber("500").companiesHouseNumber("12738494")
                .orgType(GrantMandatoryQuestionOrgType.CHARITY).fundingAmount(BigDecimal.valueOf(50000))
                .schemeEntity(schemeEntity).gapId("GAP-ID").build();

        final List<GrantMandatoryQuestions> mandatoryQuestionsList = List.of(grantMandatoryQuestions);

        final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                .status(SpotlightSubmissionStatus.VALIDATION_ERROR.toString()).grantScheme(schemeEntity)
                .mandatoryQuestions(grantMandatoryQuestions).build();

        @Test
        void getFilteredSpotlightSubmissionsWithValidationErrors() throws IOException {
            final ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(zipStream)) {
                final ZipEntry entry = new ZipEntry("mock_excel_file.xlsx");
                zipOut.putNextEntry(entry);
                zipOut.write("Mock Excel File Content".getBytes());
                zipOut.closeEntry();
            }

            when(spotlightBatchRepository.findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable))
                    .thenReturn(spotlightBatches);
            spotlightBatches.get(0).getSpotlightSubmissions().add(spotlightSubmission);
            when(grantMandatoryQuestionService.getValidationErrorChecks(mandatoryQuestionsList, schemeId))
                    .thenReturn(zipStream);

            final ByteArrayOutputStream result = spotlightBatchService
                    .getFilteredSpotlightSubmissionsWithValidationErrors(schemeId);

            verify(spotlightBatchRepository).findByLastSendAttemptNotNullOrderByLastSendAttemptDesc(pageable);
            assertThat(result).isEqualTo(zipStream);
        }

    }

}
