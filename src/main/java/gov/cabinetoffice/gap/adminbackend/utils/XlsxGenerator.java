package gov.cabinetoffice.gap.adminbackend.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class XlsxGenerator {

    private static final String SHEET_NAME = "Sheet1";

    private XlsxGenerator() {
    }

    public static ByteArrayOutputStream createResource(List<String> headers, List<List<String>> data) {
        final Workbook workbook = createWorkbook(headers, data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
            return out;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to generate stream for XlsxGenerator:" + e.getMessage());
        }
    }

    static Workbook createWorkbook(List<String> headers, List<List<String>> data) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("Headers cannot be null or empty");
        }

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(SHEET_NAME);
        addData(sheet, data);
        addHeaders(sheet, headers);
        return workbook;
    }

    private static void addHeaders(Sheet worksheet, List<String> headers) {
        final Row row = worksheet.createRow(0);
        for (int col = 0; col < headers.size(); col++) {
            Cell cell = row.createCell(col);
            cell.setCellValue(headers.get(col));

            // leaving this method call in to apply basic formatting with minimal
            // performance hit
            worksheet.autoSizeColumn(col);
        }
    }

    private static void addData(Sheet worksheet, List<List<String>> data) {
        for (int row = 0; row < data.size(); row++) {
            final Row dataRow = worksheet.createRow(row + 1);
            for (int col = 0; col < data.get(row).size(); col++) {
                Cell cell = dataRow.createCell(col);
                cell.setCellValue(data.get(row).get(col));
            }
        }
    }

}
