package gov.cabinetoffice.gap.adminbackend.utils;

import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CsvUtils {

    @SneakyThrows
    public static ByteArrayResource createCSVData(List<String> headers, List<List<String>> data) {
        if (headers == null || headers.isEmpty()) {
            throw new RuntimeException("Headers cannot be null or empty");
        }
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("Data cannot be null or empty");
        }

        ByteArrayResource byteArrayResource;

        // CSVFormat needs an array of Strings as opposed to a list
        String[] convertedHeaders = headers.toArray(new String[0]);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out),
                        CSVFormat.Builder.create().setHeader(convertedHeaders).build())) {
            // populating the CSV content
            for (List<String> row : data)
                csvPrinter.printRecord(row);

            // writing the underlying stream
            csvPrinter.flush();

            byteArrayResource = new ByteArrayResource(out.toByteArray());
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return byteArrayResource;

    }

}
