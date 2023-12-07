package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.constants.DueDiligenceHeaders;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.SpotlightExportException;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantMandatoryQuestionRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService.combineAddressLines;
import static gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService.mandatoryValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class GrantMandatoryQuestionServiceTest {

    private static final Integer KNOWN_INTEGER = 1;

    private final Integer SCHEME_ID = 2;

    private final SchemeEntity schemeEntity = SchemeEntity.builder().id(SCHEME_ID).funderId(1).name("name")
            .ggisIdentifier("123").build();

    private final Submission submission = Submission.builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000000")).scheme(schemeEntity)
            .status(SubmissionStatus.SUBMITTED).build();

    private final GrantMandatoryQuestions grantMandatoryQuestionsExternal = GrantMandatoryQuestions.builder()
            .name("Some company name").addressLine1("9-10 St Andrew Square").city("Edinburgh").county("county")
            .postcode("EH2 2AF").charityCommissionNumber("500").companiesHouseNumber("12738494")
            .orgType(GrantMandatoryQuestionOrgType.CHARITY).fundingAmount(BigDecimal.valueOf(50000))
            .schemeEntity(schemeEntity).gapId("GAP-ID").build();

    private final GrantMandatoryQuestions grantMandatoryQuestionsInternal = GrantMandatoryQuestions.builder()
            .name("Some company name").addressLine1("9-10 St Andrew Square").city("Edinburgh").county("county")
            .postcode("EH2 2AF").charityCommissionNumber("500").companiesHouseNumber("12738494")
            .orgType(GrantMandatoryQuestionOrgType.CHARITY).fundingAmount(BigDecimal.valueOf(50000))
            .schemeEntity(schemeEntity).gapId("GAP-ID").submission(submission).build();

    private final GrantMandatoryQuestions grantMandatoryQuestionsNonLimitedCompany = GrantMandatoryQuestions.builder()
            .name("Another company name").addressLine1("9-10 St Andrew Square").city("Glasgow").county("county")
            .postcode("G02 2AF").companiesHouseNumber("12738494")
            .orgType(GrantMandatoryQuestionOrgType.NON_LIMITED_COMPANY).fundingAmount(BigDecimal.valueOf(50000))
            .schemeEntity(schemeEntity).gapId("GAP-ID").build();

    final List<GrantMandatoryQuestions> mandatoryQuestionsList = List.of(grantMandatoryQuestionsExternal,
            grantMandatoryQuestionsNonLimitedCompany);

    private final SchemeDTO schemeDTO = SchemeDTO.builder().name("schemeName").ggisReference("123").build();

    private final List<String> EXPECTED_DUE_DILIGENCE_ROW = Arrays.asList("GAP-ID", "Some company name",
            "9-10 St Andrew Square", "county", "Edinburgh", "EH2 2AF", "500", "12738494", "50000",
            "Non-limited company", "");

    private final List<String> EXPECTED_SPOTLIGHT_ROW = Arrays.asList("GAP-ID", "Some company name",
            "9-10 St Andrew Square", "county", "Edinburgh", "EH2 2AF", "500", "12738494", "50000", "");

    @Mock
    private GrantMandatoryQuestionRepository grantMandatoryQuestionRepository;

    @Mock
    private SchemeService schemeService;

    @Mock
    private ZipService zipService;

    @InjectMocks
    @Spy
    private GrantMandatoryQuestionService grantMandatoryQuestionService;

    @Test
    void getCharitiesAndCompaniesMandatoryQuestionsBySchemeAndCompletedStatus() {
        when(grantMandatoryQuestionRepository.findCharitiesAndCompaniesBySchemeEntityIdAndCompletedStatus(SCHEME_ID))
                .thenReturn(List.of(grantMandatoryQuestionsExternal));

        List<GrantMandatoryQuestions> result = grantMandatoryQuestionService
                .getCharitiesAndCompaniesMandatoryQuestionsBySchemeAndCompletedStatus(SCHEME_ID);

        assertThat(result).isEqualTo(List.of(grantMandatoryQuestionsExternal));

    }

    @Test
    void getNonLimitedCompaniesMandatoryQuestionsBySchemeAndCompletedStatus() {
        when(grantMandatoryQuestionRepository.findNonLimitedCompaniesBySchemeEntityIdAndCompletedStatus(SCHEME_ID))
                .thenReturn(List.of(grantMandatoryQuestionsNonLimitedCompany));

        List<GrantMandatoryQuestions> result = grantMandatoryQuestionService
                .getNonLimitedCompaniesMandatoryQuestionsBySchemeAndCompletedStatus(SCHEME_ID);

        assertThat(result).isEqualTo(List.of(grantMandatoryQuestionsNonLimitedCompany));

    }

    @Nested
    class GetGrantMandatoryQuestionBySchemeAndStatusTests {

        @Test
        void validSchemeId() {
            when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                    .thenReturn(List.of(grantMandatoryQuestionsExternal));

            List<GrantMandatoryQuestions> result = grantMandatoryQuestionService
                    .getGrantMandatoryQuestionBySchemeAndCompletedStatus(SCHEME_ID);

            assertThat(result).isEqualTo(List.of(grantMandatoryQuestionsExternal));

        }

    }

    @Nested
    class getValidationErrorChecks {

        @Test
        void getValidationErrorChecks() {
            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenReturn(schemeDTO);
            when(zipService.createZip(anyList(), anyList(), anyList())).thenReturn(new ByteArrayOutputStream());
            doReturn(EXPECTED_SPOTLIGHT_ROW).when(grantMandatoryQuestionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, false);
            doReturn(EXPECTED_SPOTLIGHT_ROW).when(grantMandatoryQuestionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestionsNonLimitedCompany, false);

            ByteArrayOutputStream dataStream = grantMandatoryQuestionService
                    .getValidationErrorChecks(mandatoryQuestionsList, SCHEME_ID);

            assertNotNull(dataStream);
        }

        @Test
        void getValidationErrorChecks_throwAccessDeniedException() {

            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenThrow(new AccessDeniedException("accessDenied"));

            Exception exception = assertThrows(AccessDeniedException.class,
                    () -> grantMandatoryQuestionService.getValidationErrorChecks(mandatoryQuestionsList, SCHEME_ID));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage)
                    .isEqualTo("Admin 1 is unable to access mandatory questions with scheme id " + SCHEME_ID);
        }

    }

    @Nested
    class getSpotlightChecks {

        @Test
        void getSpotlightChecks() {
            when(grantMandatoryQuestionRepository
                    .findCharitiesAndCompaniesBySchemeEntityIdAndCompletedStatus(SCHEME_ID))
                            .thenReturn(List.of(grantMandatoryQuestionsExternal));
            when(grantMandatoryQuestionRepository.findNonLimitedCompaniesBySchemeEntityIdAndCompletedStatus(SCHEME_ID))
                    .thenReturn(List.of(grantMandatoryQuestionsNonLimitedCompany));
            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenReturn(schemeDTO);
            when(zipService.createZip(anyList(), anyList(), anyList())).thenReturn(new ByteArrayOutputStream());
            doReturn(EXPECTED_SPOTLIGHT_ROW).when(grantMandatoryQuestionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, false);
            doReturn(EXPECTED_SPOTLIGHT_ROW).when(grantMandatoryQuestionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestionsNonLimitedCompany, false);

            ByteArrayOutputStream dataStream = grantMandatoryQuestionService.getSpotlightChecks(SCHEME_ID);

            assertNotNull(dataStream);
        }

        @Test
        void getSpotlightChecks_throwAccessDeniedException() {
            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenThrow(new AccessDeniedException("accessDenied"));

            Exception exception = assertThrows(AccessDeniedException.class,
                    () -> grantMandatoryQuestionService.getSpotlightChecks(SCHEME_ID));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage)
                    .isEqualTo("Admin 1 is unable to access mandatory questions with scheme id " + SCHEME_ID);
        }

    }

    @Nested
    class getDueDiligenceData {

        private static void assertRowIsAsExpected(Row actualRow, List<String> expectedRow) {
            assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(expectedRow.size());
            for (int col = 0; col < expectedRow.size(); col++) {
                assertThat(actualRow.getCell(col).getStringCellValue()).isEqualTo(expectedRow.get(col));
            }
        }

        @Nested
        class internalApplications {

            @Test
            void forSingleRowWithGoodData() throws IOException {
                when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                        .thenReturn(List.of(grantMandatoryQuestionsExternal, grantMandatoryQuestionsInternal));
                doReturn(EXPECTED_DUE_DILIGENCE_ROW).when(grantMandatoryQuestionService)
                        .buildSingleSpotlightRow(grantMandatoryQuestionsInternal, true);

                ByteArrayOutputStream dataStream = grantMandatoryQuestionService.getDueDiligenceData(SCHEME_ID, true);

                Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dataStream.toByteArray()));
                Row headerRow = workbook.getSheetAt(0).getRow(0);
                assertRowIsAsExpected(headerRow, DueDiligenceHeaders.DUE_DILIGENCE_HEADERS);
                Row dataRow = workbook.getSheetAt(0).getRow(1);
                assertRowIsAsExpected(dataRow, EXPECTED_DUE_DILIGENCE_ROW);
            }

            @Test
            void forSingleRowWithGoodData_throwAccessDeniedException() {
                when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenThrow(new AccessDeniedException("accessDenied"));

                Exception exception = assertThrows(AccessDeniedException.class,
                        () -> grantMandatoryQuestionService.getDueDiligenceData(SCHEME_ID, true));

                String actualMessage = exception.getMessage();
                assertThat(actualMessage)
                        .isEqualTo("Admin 1 is unable to access mandatory questions with scheme id " + SCHEME_ID);
            }

        }

        @Nested
        class externalApplications {

            @Test
            void forSingleRowWithGoodData() throws IOException {
                when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                        .thenReturn(List.of(grantMandatoryQuestionsExternal));
                doReturn(EXPECTED_DUE_DILIGENCE_ROW).when(grantMandatoryQuestionService)
                        .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, true);

                ByteArrayOutputStream dataStream = grantMandatoryQuestionService.getDueDiligenceData(SCHEME_ID, false);

                Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dataStream.toByteArray()));
                Row headerRow = workbook.getSheetAt(0).getRow(0);
                assertRowIsAsExpected(headerRow, DueDiligenceHeaders.DUE_DILIGENCE_HEADERS);
                Row dataRow = workbook.getSheetAt(0).getRow(1);
                assertRowIsAsExpected(dataRow, EXPECTED_DUE_DILIGENCE_ROW);
            }

            @Test
            void forSingleRowWithGoodData_throwAccessDeniedException() {
                when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenThrow(new AccessDeniedException("accessDenied"));

                Exception exception = assertThrows(AccessDeniedException.class,
                        () -> grantMandatoryQuestionService.getDueDiligenceData(SCHEME_ID, false));

                String actualMessage = exception.getMessage();
                assertThat(actualMessage)
                        .isEqualTo("Admin 1 is unable to access mandatory questions with scheme id " + SCHEME_ID);
            }

        }

    }

    @Nested
    class MandatoryValueTests {

        @Test
        void givenNullValue_throwsException() {
            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> mandatoryValue(KNOWN_INTEGER, "valueName", null));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains(KNOWN_INTEGER.toString()).contains("valueName");
        }

        @Test
        void givenEmptyValue_throwsException() {
            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> mandatoryValue(KNOWN_INTEGER, "valueName", ""));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains(KNOWN_INTEGER.toString()).contains("valueName");
        }

        @Test
        void givenValue_returnsValue() {
            assertThat(mandatoryValue(KNOWN_INTEGER, "valueName", "value")).isEqualTo("value");
        }

    }

    @Nested
    class CombineAddressLinesTests {

        @Test
        void givenNullData_returnsEmptyString() {
            String addressLine1 = null;
            String addressLine2 = null;
            String result = combineAddressLines(addressLine1, addressLine2);
            assertThat(result).isEmpty();
        }

        @Test
        void givenBlankData_returnsEmptyString() {
            String addressLine1 = "";
            String addressLine2 = "";
            String result = combineAddressLines(addressLine1, addressLine2);
            assertThat(result).isEmpty();
        }

        @Test
        void givenOnlyFirstLine_returnsFirst() {
            String addressLine1 = "addressLine1";
            String addressLine2 = null;
            String result = combineAddressLines(addressLine1, addressLine2);
            assertThat(result).isEqualTo("addressLine1");
        }

        @Test
        void givenOnlySecondLine_returnsSecond() {
            String addressLine1 = null;
            String addressLine2 = "addressLine2";
            String result = combineAddressLines(addressLine1, addressLine2);
            assertThat(result).isEqualTo("addressLine2");
        }

        @Test
        void givenFirstAndSecondLine_returnsFirstAndSecond() {
            String addressLine1 = "addressLine1";
            String addressLine2 = "addressLine2";
            String result = combineAddressLines(addressLine1, addressLine2);
            assertThat(result).isEqualTo("addressLine1, addressLine2");
        }

    }

    @Nested
    class BuildSingleSpotlightRowTests {

        @Test
        void givenGoodInput_returnsExpectedData() {
            List<String> spotlightRow = grantMandatoryQuestionService
                    .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, false);

            assertThat(spotlightRow).containsAll(EXPECTED_SPOTLIGHT_ROW);
        }

        @Test
        void givenDataWithoutOrgName_throwsException() {
            grantMandatoryQuestionsExternal.setName(null);

            Exception exception = assertThrows(SpotlightExportException.class, () -> grantMandatoryQuestionService
                    .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, false));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("organisation name");
        }

        @Test
        void givenDataWithoutPostcode_throwsException() {
            grantMandatoryQuestionsExternal.setPostcode(null);

            Exception exception = assertThrows(SpotlightExportException.class, () -> grantMandatoryQuestionService
                    .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, false));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("postcode");
        }

        @Test
        void givenDataWithoutAmount_throwsException() {
            grantMandatoryQuestionsExternal.setFundingAmount(null);

            Exception exception = assertThrows(SpotlightExportException.class, () -> grantMandatoryQuestionService
                    .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, false));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("Unable to find mandatory question data:");
        }

        @Test
        void givenNullSchemeData_throwsException() {
            grantMandatoryQuestionsExternal.setSchemeEntity(null);

            Exception exception = assertThrows(SpotlightExportException.class, () -> grantMandatoryQuestionService
                    .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, false));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("Unable to find mandatory question data:");
        }

        @Test
        void givenDataWithoutCharityNumber_returnsExpectedData() {
            grantMandatoryQuestionsExternal.setCharityCommissionNumber(null);
            EXPECTED_SPOTLIGHT_ROW.set(6, "");
            List<String> spotlightRow = grantMandatoryQuestionService
                    .buildSingleSpotlightRow(grantMandatoryQuestionsExternal, false);
            assertThat(spotlightRow).containsAll(EXPECTED_SPOTLIGHT_ROW);
        }

    }

    @Nested
    class GenerateExportFileNameTest {

        @Test
        void generatesFileName() {
            String dateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            SchemeDTO schemeDTO = SchemeDTO.builder().name("schemeName").ggisReference("123").build();

            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenReturn(schemeDTO);

            String result = grantMandatoryQuestionService.generateExportFileName(SCHEME_ID, null);

            assertThat(result)
                    .isEqualTo(dateString + "_" + schemeDTO.getGgisReference() + "_" + schemeDTO.getName() + ".xlsx");

        }

        @Test
        void generatesFileNameWithOrgType() {
            String dateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            SchemeDTO schemeDTO = SchemeDTO.builder().name("schemeName").ggisReference("123").build();

            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenReturn(schemeDTO);

            String result = grantMandatoryQuestionService.generateExportFileName(SCHEME_ID, "charity");

            assertThat(result).isEqualTo(
                    dateString + "_" + schemeDTO.getGgisReference() + "_" + schemeDTO.getName() + "_charity" + ".xlsx");

        }

    }

    @Nested
    class doesSchemeHaveCompletedMandatoryQuestions {

        @Test
        void returnsTrue() {
            when(grantMandatoryQuestionRepository.existsBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                    .thenReturn(true);
            boolean result = grantMandatoryQuestionService.hasCompletedMandatoryQuestions(SCHEME_ID);
            assertThat(result).isEqualTo(true);
        }

        @Test
        void returnFalse() {
            when(grantMandatoryQuestionRepository.existsBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                    .thenReturn(false);
            boolean result = grantMandatoryQuestionService.hasCompletedMandatoryQuestions(SCHEME_ID);
            assertThat(result).isEqualTo(false);
        }

    }

    @Nested
    class doesSchemeHaveCompletedDataForSpotlight {

        @Test
        void returnsTrue() {
            when(grantMandatoryQuestionRepository.existsBySchemeEntityIdAndCompleteStatusAndOrgType(SCHEME_ID))
                    .thenReturn(true);
            boolean result = grantMandatoryQuestionService.hasCompletedDataForSpotlight(SCHEME_ID);
            assertThat(result).isEqualTo(true);
        }

        @Test
        void returnFalse() {
            when(grantMandatoryQuestionRepository.existsBySchemeEntityIdAndCompleteStatusAndOrgType(SCHEME_ID))
                    .thenReturn(false);
            boolean result = grantMandatoryQuestionService.hasCompletedDataForSpotlight(SCHEME_ID);
            assertThat(result).isEqualTo(false);
        }

    }

}