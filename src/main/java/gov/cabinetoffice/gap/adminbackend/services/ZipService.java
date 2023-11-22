package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.utils.XlsxGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ZipService {

    public ByteArrayOutputStream createZip(List<String> headersList, List<List<List<String>>> dataList,
            List<String> filenames) {
        ByteArrayOutputStream zipStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(zipStream)) {
            for (int i = 0; i < dataList.size(); i++) {
                List<List<String>> data = dataList.get(i);
                if (!data.isEmpty()) {
                    final String filename = filenames.get(i);

                    final ByteArrayOutputStream excelStream = XlsxGenerator.createResource(headersList, data);

                    final ZipEntry entry = new ZipEntry(filename);
                    zipOut.putNextEntry(entry);
                    zipOut.write(excelStream.toByteArray());
                    zipOut.closeEntry();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create ZIP file: " + e.getMessage());
        }

        return zipStream;
    }

}
