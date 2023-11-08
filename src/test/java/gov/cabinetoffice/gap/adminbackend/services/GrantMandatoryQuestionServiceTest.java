package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.constants.SpotlightHeaders;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
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

import static gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService.combineAddressLines;
import static gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService.mandatoryValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class GrantMandatoryQuestionServiceTest {

    private static final Integer KNOWN_INTEGER = 1;

    private final Integer SCHEME_ID = 2;

    private final SchemeEntity schemeEntity = SchemeEntity.builder().id(SCHEME_ID).funderId(1).name("name").build();

    @Mock
    private GrantMandatoryQuestionRepository grantMandatoryQuestionRepository;

    @Mock
    private SchemeService schemeService;

    @InjectMocks
    @Spy
    private GrantMandatoryQuestionService grantMandatoryQuestionService;

    private final List<String> EXPECTED_SPOTLIGHT_ROW = Arrays.asList("GAP-ID", "Some company name",
            "9-10 St Andrew Square", "county", "Edinburgh", "EH2 2AF", "500", "12738494", "50000", "");

    private final GrantMandatoryQuestions grantMandatoryQuestions = GrantMandatoryQuestions.builder()
            .name("Some company name").addressLine1("9-10 St Andrew Square").city("Edinburgh").county("county")
            .postcode("EH2 2AF").charityCommissionNumber("500").companiesHouseNumber("12738494")
            .fundingAmount(BigDecimal.valueOf(50000)).schemeEntity(schemeEntity).gapId("GAP-ID").build();

    @Nested
    class GetGrantMandatoryQuestionBySchemeAndStatusTests {

        @Test
        void validSchemeId() {
            when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                    .thenReturn(List.of(grantMandatoryQuestions));

            List<GrantMandatoryQuestions> result = grantMandatoryQuestionService
                    .getGrantMandatoryQuestionBySchemeAndCompletedStatus(SCHEME_ID);

            assertThat(result).isEqualTo(List.of(grantMandatoryQuestions));

        }

        @Test
        void invalidSchemeId() {
            when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID)).thenReturn(null);

            Exception exception = assertThrows(NotFoundException.class,
                    () -> grantMandatoryQuestionService.getGrantMandatoryQuestionBySchemeAndCompletedStatus(SCHEME_ID));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).isEqualTo("No completed mandatory questions with ID " + SCHEME_ID + " was found");
        }

    }

    @Nested
    class ExportSpotlightChecksTests {

        private static void assertRowIsAsExpected(Row actualRow, List<String> expectedRow) {
            assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(expectedRow.size());
            for (int col = 0; col < expectedRow.size(); col++) {
                assertThat(actualRow.getCell(col).getStringCellValue()).isEqualTo(expectedRow.get(col));
            }
        }

        @Test
        void forSingleRowWithGoodData() throws IOException {
            when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                    .thenReturn(List.of(grantMandatoryQuestions));
            doReturn(EXPECTED_SPOTLIGHT_ROW).when(grantMandatoryQuestionService)
                    .buildSingleSpotlightRow(grantMandatoryQuestions);

            ByteArrayOutputStream dataStream = grantMandatoryQuestionService.exportSpotlightChecks(SCHEME_ID);

            Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dataStream.toByteArray()));
            Row headerRow = workbook.getSheetAt(0).getRow(0);
            assertRowIsAsExpected(headerRow, SpotlightHeaders.SPOTLIGHT_HEADERS);
            Row dataRow = workbook.getSheetAt(0).getRow(1);
            assertRowIsAsExpected(dataRow, EXPECTED_SPOTLIGHT_ROW);
        }

        @Test
        void ignoresBadDataRows() throws IOException {
            final GrantMandatoryQuestions badGrantMandatoryQuestions = GrantMandatoryQuestions.builder()
                    .addressLine1("addressLine1").addressLine2("addressLine2").city("city")
                    .charityCommissionNumber("123").companiesHouseNumber("321").schemeEntity(schemeEntity).build();

            when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                    .thenReturn(List.of(grantMandatoryQuestions, badGrantMandatoryQuestions));

            ByteArrayOutputStream dataStream = grantMandatoryQuestionService.exportSpotlightChecks(SCHEME_ID);

            Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dataStream.toByteArray()));
            assertThat(workbook.getSheetAt(0).getPhysicalNumberOfRows()).isEqualTo(2);
        }

        @Test
        void forSingleRowWithGoodData_throwAccessDeniedException() {
            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenThrow(new AccessDeniedException("accessDenied"));

            Exception exception = assertThrows(AccessDeniedException.class,
                    () -> grantMandatoryQuestionService.exportSpotlightChecks(SCHEME_ID));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage)
                    .isEqualTo("Admin 1 is unable to access mandatory questions with scheme id " + SCHEME_ID);
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
            List<String> spotlightRow = grantMandatoryQuestionService.buildSingleSpotlightRow(grantMandatoryQuestions);

            assertThat(spotlightRow).containsAll(EXPECTED_SPOTLIGHT_ROW);
        }

        @Test
        void givenDataWithoutOrgName_throwsException() {
            grantMandatoryQuestions.setName(null);

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> grantMandatoryQuestionService.buildSingleSpotlightRow(grantMandatoryQuestions));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("organisation name");
        }

        @Test
        void givenDataWithoutPostcode_throwsException() {
            grantMandatoryQuestions.setPostcode(null);

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> grantMandatoryQuestionService.buildSingleSpotlightRow(grantMandatoryQuestions));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("postcode");
        }

        @Test
        void givenDataWithoutAmount_throwsException() {
            grantMandatoryQuestions.setFundingAmount(null);

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> grantMandatoryQuestionService.buildSingleSpotlightRow(grantMandatoryQuestions));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("application amount");
        }

        @Test
        void givenNullSchemeData_throwsException() {
            grantMandatoryQuestions.setSchemeEntity(null);

            Exception exception = assertThrows(SpotlightExportException.class,
                    () -> grantMandatoryQuestionService.buildSingleSpotlightRow(grantMandatoryQuestions));

            String actualMessage = exception.getMessage();
            assertThat(actualMessage).contains("Unable to find mandatory question data:");
        }

    }

    @Nested
    class GenerateExportFileNameTest {

        @Test
        void generatesFileName() {
            String dateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            SchemeDTO schemeDTO = SchemeDTO.builder().name("schemeName").ggisReference("123").build();

            when(schemeService.getSchemeBySchemeId(SCHEME_ID)).thenReturn(schemeDTO);

            String result = grantMandatoryQuestionService.generateExportFileName(SCHEME_ID);

            assertThat(result)
                    .isEqualTo(dateString + "_" + schemeDTO.getGgisReference() + "_" + schemeDTO.getName() + ".xlsx");

        }

    }

    @Nested
    class doesSchemeHaveCompletedMandatoryQuestions {

        @Test
        void returnsTrue() {
            when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID))
                    .thenReturn(List.of(grantMandatoryQuestions));
            boolean result = grantMandatoryQuestionService.hasCompletedMandatoryQuestions(SCHEME_ID);
            assertThat(result).isEqualTo(true);
        }

        @Test
        void returnFalse() {
            when(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(SCHEME_ID)).thenReturn(null);
            boolean result = grantMandatoryQuestionService.hasCompletedMandatoryQuestions(SCHEME_ID);
            assertThat(result).isEqualTo(false);
        }

    }

}