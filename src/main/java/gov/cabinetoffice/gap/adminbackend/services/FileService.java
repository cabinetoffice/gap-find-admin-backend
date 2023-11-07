package gov.cabinetoffice.gap.adminbackend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class FileService {

    public InputStreamResource createTemporaryFile(ByteArrayOutputStream stream, String filename) {
        try {
            File tempFile = File.createTempFile(filename, ".xlsx");
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                stream.writeTo(out);
            }
            return new InputStreamResource(new ByteArrayInputStream(stream.toByteArray()));
        }
        catch (Exception e) {
            log.error("Error creating temporary for file {} problem reported {}", filename, e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
