package gov.cabinetoffice.gap.adminbackend.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Log4j2
public class XlsxGeneratorTest {

    public static final String HEADER_1 = "Address town (optional)";

    public static final String HEADER_2 = "Address postcode (required)";

    private static final List<String> TEST_HEADERS = List.of(HEADER_1, HEADER_2);

    public static final String DATA_1_1 = "row 1 - col 1";

    public static final String DATA_1_2 = "row 1 - col 2";

    private static final List<String> TEST_DATA_1 = List.of(DATA_1_1, DATA_1_2);

    public static final String DATA_2_1 = "row 2 - col 1";

    public static final String DATA_2_2 = "row 2 - col 2";

    private static final List<String> TEST_DATA_2 = List.of(DATA_2_1, DATA_2_2);

    @Test
    public void createWorkbookWillThrowExceptionForNoHeaders() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> XlsxGenerator.createWorkbook(null, null));
        assertThat(exception.getMessage(), is("Headers cannot be null or empty"));
    }

    @Test
    public void createWorkbookWillThrowExceptionForNoData() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> XlsxGenerator.createWorkbook(TEST_HEADERS, null));
        assertThat(exception.getMessage(), is("Data cannot be null or empty"));
    }

    @Test
    public void createWorkbookWillContainExpectedHeader() {
        Workbook workbook = XlsxGenerator.createWorkbook(TEST_HEADERS, List.of(TEST_DATA_1));

        Row headerRow = workbook.getSheetAt(0).getRow(0);
        assertThat(headerRow.getPhysicalNumberOfCells(), equalTo(TEST_HEADERS.size()));
        assertThat(headerRow.getCell(0).getStringCellValue(), is(HEADER_1));
        assertThat(headerRow.getCell(1).getStringCellValue(), is(HEADER_2));
    }

    private static void assertRowAsExpected(Row dataRow, List<String> expectedData) {
        assertThat(dataRow.getPhysicalNumberOfCells(), equalTo(expectedData.size()));
        for (int i = 0; i < expectedData.size(); i++) {
            assertThat(dataRow.getCell(i).getStringCellValue(), is(expectedData.get(i)));
        }
    }

    @Test
    public void createWorkbookForSingleDataRowWillContainExpectedData() {
        Workbook workbook = XlsxGenerator.createWorkbook(TEST_HEADERS, List.of(TEST_DATA_1));

        assertThat(workbook.getSheetAt(0).getPhysicalNumberOfRows(), equalTo(2));
        Row dataRow = workbook.getSheetAt(0).getRow(1);
        assertRowAsExpected(dataRow, TEST_DATA_1);
    }

    @Test
    public void createWorkbookForMultipleDataRowsWillContainExpectedData() {
        Workbook workbook = XlsxGenerator.createWorkbook(TEST_HEADERS, List.of(TEST_DATA_1, TEST_DATA_2));

        assertThat(workbook.getSheetAt(0).getPhysicalNumberOfRows(), equalTo(3));
        Row dataRow1 = workbook.getSheetAt(0).getRow(1);
        assertRowAsExpected(dataRow1, TEST_DATA_1);
        Row dataRow2 = workbook.getSheetAt(0).getRow(2);
        assertRowAsExpected(dataRow2, TEST_DATA_2);
    }

    @Test
    public void createResourceWillReturnStream() {
        OutputStream stream = XlsxGenerator.createResource(TEST_HEADERS, List.of(TEST_DATA_1));
        assertNotNull(stream);
    }

    @Test
    public void createResourceReturnCanGenerateFile() {
        ByteArrayOutputStream stream = XlsxGenerator.createResource(TEST_HEADERS, List.of(TEST_DATA_1));
        byte[] data = stream.toByteArray();
        OutputStream os = null;
        try {
            File file = File.createTempFile("test-", ".xlsx");
            log.info("Writing to file {}", file.getAbsolutePath());
            os = new BufferedOutputStream(new FileOutputStream(file));
            os.write(data);
            os.flush();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(os);
        }
    }

}
