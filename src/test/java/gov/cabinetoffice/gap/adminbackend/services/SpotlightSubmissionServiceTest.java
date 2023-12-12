package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SpotlightExportException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import org.junit.Assert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@WithAdminSession
class SpotlightSubmissionServiceTest {

    private static final UUID spotlightSubmissionId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final Integer SCHEME_ID = 1;

    private final LocalDate date = LocalDate.of(2023, 9, 25);

    private final LocalTime time = LocalTime.of(0, 0, 0);

    private final LocalDateTime dateTime = LocalDateTime.of(date, time);

    private final Instant DATE = dateTime.toInstant(ZoneOffset.UTC);

    private static final Integer KNOWN_INTEGER = 1;

    private final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(UUID.randomUUID())
            .lastSendAttempt(DATE).build();

    private final List<String> EXPECTED_SPOTLIGHT_ROW = Arrays.asList("GAP-ID", "Some company name",
            "9-10 St Andrew Square", "county", "Edinburgh", "EH2 2AF", "500", "12738494", "50000", "");

    private final SchemeEntity schemeEntity = SchemeEntity.builder().id(SCHEME_ID).funderId(1).name("name")
            .ggisIdentifier("123").build();

    private final Submission submission = Submission.builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000000")).scheme(schemeEntity)
            .status(SubmissionStatus.SUBMITTED).build();

    private final GrantMandatoryQuestions grantMandatoryQuestionsCharity = GrantMandatoryQuestions.builder()
            .name("Some company name").addressLine1("9-10 St Andrew Square").city("Edinburgh").county("county")
            .postcode("EH2 2AF").charityCommissionNumber("500").companiesHouseNumber("12738494")
            .orgType(GrantMandatoryQuestionOrgType.CHARITY).fundingAmount(BigDecimal.valueOf(50000))
            .schemeEntity(schemeEntity).gapId("GAP-ID").submission(submission).build();

    private final GrantMandatoryQuestions grantMandatoryQuestionsRegisteredCharity = GrantMandatoryQuestions.builder()
            .name("Some company name").addressLine1("9-10 St Andrew Square").city("Edinburgh").county("county")
            .postcode("EH2 2AF").charityCommissionNumber("500").companiesHouseNumber("12738494")
            .orgType(GrantMandatoryQuestionOrgType.REGISTERED_CHARITY).fundingAmount(BigDecimal.valueOf(50000))
            .schemeEntity(schemeEntity).gapId("GAP-ID").submission(submission).build();

    private final GrantMandatoryQuestions grantMandatoryQuestionsUnregisteredCharity = GrantMandatoryQuestions.builder()
            .name("Some company name").addressLine1("9-10 St Andrew Square").city("Edinburgh").county("county")
            .postcode("EH2 2AF").charityCommissionNumber("500").companiesHouseNumber("12738494")
            .orgType(GrantMandatoryQuestionOrgType.UNREGISTERED_CHARITY).fundingAmount(BigDecimal.valueOf(50000))
            .schemeEntity(schemeEntity).gapId("GAP-ID").submission(submission).build();

    @Mock
    private SpotlightSubmissionRepository spotlightSubmissionRepository;

    @Mock
    private ZipService zipService;

    @Spy
    @InjectMocks
    private SpotlightSubmissionService spotlightSubmissionService;

    @Test
    void getSubmissionsByBySchemeIdAndStatus_ReturnsList() {
        when(spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT.toString())).thenReturn(List.of(spotlightSubmission));

        final List<SpotlightSubmission> result = spotlightSubmissionService
                .getSubmissionsByBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT);

        verify(spotlightSubmissionRepository).findByGrantSchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT.toString());
        assertThat(result).isEqualTo(List.of(spotlightSubmission));
    }

    @Test
    void getCountBySchemeIdAndStatus_ReturnsCount() {
        when(spotlightSubmissionRepository.countByGrantSchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT.toString())).thenReturn(Long.valueOf(2));

        final long result = spotlightSubmissionService.getCountBySchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT);

        verify(spotlightSubmissionRepository).countByGrantSchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT.toString());
        assertThat(result).isEqualTo(2);

    }

    @Nested
    class getSpotlightSubmissionByMandatoryQuestionGapId {

        @Test
        void getSpotlightSubmissionByMandatoryQuestionGapId_success() {
            when(spotlightSubmissionRepository.findByMandatoryQuestions_GapId("gapId"))
                    .thenReturn(Optional.of(spotlightSubmission));

            final SpotlightSubmission result = spotlightSubmissionService
                    .getSpotlightSubmissionByMandatoryQuestionGapId("gapId");

            verify(spotlightSubmissionRepository).findByMandatoryQuestions_GapId("gapId");

            assertThat(result).isEqualTo(spotlightSubmission);
        }

        @Test
        void getSpotlightSubmissionByMandatoryQuestionGapId_notFound() {
            when(spotlightSubmissionRepository.findByMandatoryQuestions_GapId("gapId")).thenReturn(Optional.empty());

            final NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> spotlightSubmissionService.getSpotlightSubmissionByMandatoryQuestionGapId("gapId"));

            assertEquals("A spotlight submission with mandatory question gapId gapId could not be found",
                    exception.getMessage());
        }

    }

    @Nested
    class getLastSubmissionDate {

        @Test
        void getLastSubmissionDate_NoSubmissions() {
            when(spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT.toString())).thenReturn(List.of());
            final String result = spotlightSubmissionService.getLastSubmissionDate(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
            assertThat(result).isEmpty();
        }

        @Test
        void getLastSubmissionDate_HasSubmissions() {
            when(spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT.toString())).thenReturn(List.of(spotlightSubmission));
            final String result = spotlightSubmissionService.getLastSubmissionDate(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
            assertThat(result).isEqualTo("25 September 2023");
        }

    }

    @Nested
    class getSpotlightSubmission {

        @Test
        void getSpotlightSubmissionSuccess() {
            final SpotlightSubmission mockSpotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                    .build();

            when(spotlightSubmissionRepository.findById(spotlightSubmissionId))
                    .thenReturn(Optional.of(mockSpotlightSubmission));

            final SpotlightSubmission result = spotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId);

            assertEquals(mockSpotlightSubmission, result);
        }

        @Test
        void getSpotlightSubmissionNotFound() {

            when(spotlightSubmissionRepository.findById(spotlightSubmissionId)).thenReturn(Optional.empty());

            final NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> spotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId));

            assertEquals("A spotlight submission with id " + spotlightSubmissionId + " could not be found",
                    exception.getMessage());
        }

    }

    @Nested
    class getSpotlightSubmissionById {

        @Test
        void returnsExpectedSubmission() {
            final SpotlightSubmission mockSpotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                    .build();

            doReturn(mockSpotlightSubmission).when(spotlightSubmissionService)
                    .getSpotlightSubmission(spotlightSubmissionId);

            final Optional<SpotlightSubmission> result = spotlightSubmissionService
                    .getSpotlightSubmissionById(spotlightSubmissionId);

            assertEquals(Optional.of(mockSpotlightSubmission), result);
        }

        @Test
        void returnsOptionalEmpty() {

            doThrow(NotFoundException.class).when(spotlightSubmissionService)
                    .getSpotlightSubmission(spotlightSubmissionId);

            final Optional<SpotlightSubmission> result = spotlightSubmissionService
                    .getSpotlightSubmissionById(spotlightSubmissionId);

            assertEquals(Optional.empty(), result);
        }

    }

    @Nested
    class doesSchemeHaveSpotlightSubmission {

        @Test
        void success() {
            when(spotlightSubmissionRepository.existsByGrantScheme_Id(SCHEME_ID)).thenReturn(true);

            final boolean result = spotlightSubmissionService.doesSchemeHaveSpotlightSubmission(SCHEME_ID);

            verify(spotlightSubmissionRepository).existsByGrantScheme_Id(SCHEME_ID);
            assertTrue(result);
        }

    }

    @Nested
    class generateCharitiesAndLimitedCompanyDownloadFile {

        @Test
        void success_Charity() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .mandatoryQuestions(grantMandatoryQuestionsCharity).grantScheme(schemeEntity).build();
            final SchemeDTO schemeDto = SchemeDTO.builder().schemeId(schemeEntity.getId()).name(schemeEntity.getName())
                    .build();

            final List<SpotlightSubmission> spotlightSubmissions = List.of(spotlightSubmission);

            when(spotlightSubmissionRepository.findByGrantScheme_Id(SCHEME_ID)).thenReturn(spotlightSubmissions);
            when(zipService.createZip(anyList(), anyList(), anyList())).thenReturn(new ByteArrayOutputStream());

            doReturn(EXPECTED_SPOTLIGHT_ROW).when(spotlightSubmissionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestionsCharity);

            final ByteArrayOutputStream result = spotlightSubmissionService.generateDownloadFile(schemeDto, false);

            verify(spotlightSubmissionRepository).findByGrantScheme_Id(SCHEME_ID);
            assertThat(result).isNotNull();

        }

        @Test
        void success_RegisteredCharity() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .mandatoryQuestions(grantMandatoryQuestionsRegisteredCharity).grantScheme(schemeEntity).build();
            final SchemeDTO schemeDto = SchemeDTO.builder().schemeId(schemeEntity.getId()).name(schemeEntity.getName())
                    .build();

            final List<SpotlightSubmission> spotlightSubmissions = List.of(spotlightSubmission);

            when(spotlightSubmissionRepository.findByGrantScheme_Id(SCHEME_ID)).thenReturn(spotlightSubmissions);
            when(zipService.createZip(anyList(), anyList(), anyList())).thenReturn(new ByteArrayOutputStream());

            doReturn(EXPECTED_SPOTLIGHT_ROW).when(spotlightSubmissionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestionsRegisteredCharity);

            final ByteArrayOutputStream result = spotlightSubmissionService.generateDownloadFile(schemeDto, false);

            verify(spotlightSubmissionRepository).findByGrantScheme_Id(SCHEME_ID);
            assertThat(result).isNotNull();

        }

        @Test
        void success_UnregisteredCharity() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .mandatoryQuestions(grantMandatoryQuestionsUnregisteredCharity).grantScheme(schemeEntity).build();
            final SchemeDTO schemeDto = SchemeDTO.builder().schemeId(schemeEntity.getId()).name(schemeEntity.getName())
                    .build();

            final List<SpotlightSubmission> spotlightSubmissions = List.of(spotlightSubmission);

            when(spotlightSubmissionRepository.findByGrantScheme_Id(SCHEME_ID)).thenReturn(spotlightSubmissions);
            when(zipService.createZip(anyList(), anyList(), anyList())).thenReturn(new ByteArrayOutputStream());

            doReturn(EXPECTED_SPOTLIGHT_ROW).when(spotlightSubmissionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestionsUnregisteredCharity);

            final ByteArrayOutputStream result = spotlightSubmissionService.generateDownloadFile(schemeDto, false);

            verify(spotlightSubmissionRepository).findByGrantScheme_Id(SCHEME_ID);
            assertThat(result).isNotNull();

        }

        @Test
        void success_ValidationErrors() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .mandatoryQuestions(grantMandatoryQuestionsUnregisteredCharity).grantScheme(schemeEntity)
                    .status(SpotlightSubmissionStatus.VALIDATION_ERROR.toString()).build();
            final SchemeDTO schemeDto = SchemeDTO.builder().schemeId(schemeEntity.getId()).name(schemeEntity.getName())
                    .build();

            final List<SpotlightSubmission> spotlightSubmissions = List.of(spotlightSubmission);

            when(spotlightSubmissionRepository.findByGrantScheme_Id(SCHEME_ID)).thenReturn(spotlightSubmissions);
            when(zipService.createZip(anyList(), anyList(), anyList())).thenReturn(new ByteArrayOutputStream());

            doReturn(EXPECTED_SPOTLIGHT_ROW).when(spotlightSubmissionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestionsUnregisteredCharity);

            final ByteArrayOutputStream result = spotlightSubmissionService.generateDownloadFile(schemeDto, true);

            verify(spotlightSubmissionRepository).findByGrantScheme_Id(SCHEME_ID);
            assertThat(result).isNotNull();

        }

    }

    @Nested
    class MandatoryValueTests {

        @Test
        void givenNullValue_throwsException() {
            final Exception exception = Assert.assertThrows(SpotlightExportException.class,
                    () -> spotlightSubmissionService.mandatoryValue(KNOWN_INTEGER, "valueName", null));

            final String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains(KNOWN_INTEGER.toString()).contains("valueName");
        }

        @Test
        void givenEmptyValue_throwsException() {
            final Exception exception = Assert.assertThrows(SpotlightExportException.class,
                    () -> spotlightSubmissionService.mandatoryValue(KNOWN_INTEGER, "valueName", ""));

            final String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains(KNOWN_INTEGER.toString()).contains("valueName");
        }

        @Test
        void givenValue_returnsValue() {
            assertThat(spotlightSubmissionService.mandatoryValue(KNOWN_INTEGER, "valueName", "value"))
                    .isEqualTo("value");
        }

    }

    @Nested
    class CombineAddressLinesTests {

        private static Stream<Arguments> addressLineCombinations() {
            return Stream.of(Arguments.of(null, null, ""), Arguments.of("", "", ""),
                    Arguments.of("addressLine1", null, "addressLine1"),
                    Arguments.of(null, "addressLine2", "addressLine2"),
                    Arguments.of("addressLine1", "addressLine2", "addressLine1, addressLine2"),
                    Arguments.of("", "addressLine2", "addressLine2"), Arguments.of("addressLine1", "", "addressLine1"),
                    Arguments.of("", "", ""));
        }

        @ParameterizedTest
        @MethodSource("addressLineCombinations")
        void combineAddressLinesTest(String addressLine1, String addressLine2, String expected) {
            final String result = spotlightSubmissionService.combineAddressLines(addressLine1, addressLine2);
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    class BuildSingleSpotlightRowTests {

        @Test
        void givenGoodInput_returnsExpectedData() {
            final List<String> spotlightRow = spotlightSubmissionService
                    .buildSingleSpotlightRow(grantMandatoryQuestionsCharity);

            assertThat(spotlightRow).containsAll(EXPECTED_SPOTLIGHT_ROW);
        }

        @Test
        void givenDataWithoutOrgName_throwsException() {
            grantMandatoryQuestionsCharity.setName(null);

            final Exception exception = Assert.assertThrows(SpotlightExportException.class,
                    () -> spotlightSubmissionService.buildSingleSpotlightRow(grantMandatoryQuestionsCharity));

            final String actualMessage = exception.getMessage();

            assertThat(actualMessage).contains("organisation name");
        }

        @Test
        void givenDataWithoutPostcode_throwsException() {
            grantMandatoryQuestionsCharity.setPostcode(null);

            final Exception exception = Assert.assertThrows(SpotlightExportException.class,
                    () -> spotlightSubmissionService.buildSingleSpotlightRow(grantMandatoryQuestionsCharity));

            final String actualMessage = exception.getMessage();

            assertThat(actualMessage).contains("postcode");
        }

        @Test
        void givenDataWithoutAmount_throwsException() {
            grantMandatoryQuestionsCharity.setFundingAmount(null);

            final Exception exception = Assert.assertThrows(SpotlightExportException.class,
                    () -> spotlightSubmissionService.buildSingleSpotlightRow(grantMandatoryQuestionsCharity));

            final String actualMessage = exception.getMessage();

            assertThat(actualMessage).contains("Unable to find mandatory question data:");
        }

        @Test
        void givenNullSchemeData_throwsException() {
            grantMandatoryQuestionsCharity.setSchemeEntity(null);

            final Exception exception = Assert.assertThrows(SpotlightExportException.class,
                    () -> spotlightSubmissionService.buildSingleSpotlightRow(grantMandatoryQuestionsCharity));

            final String actualMessage = exception.getMessage();

            assertThat(actualMessage).contains("Unable to find mandatory question data:");
        }

        @Test
        void givenDataWithoutCharityNumber_returnsExpectedData() {
            grantMandatoryQuestionsCharity.setCharityCommissionNumber(null);
            EXPECTED_SPOTLIGHT_ROW.set(6, "");

            final List<String> spotlightRow = spotlightSubmissionService
                    .buildSingleSpotlightRow(grantMandatoryQuestionsCharity);

            assertThat(spotlightRow).containsAll(EXPECTED_SPOTLIGHT_ROW);
        }

    }

}
