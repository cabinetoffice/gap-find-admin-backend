package gov.cabinetoffice.gap.adminbackend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Service
@Slf4j
public class FileService {

    public InputStreamResource createTemporaryFile(ByteArrayOutputStream stream, String filename) {
        try {
            try (OutputStream out = Files.newOutputStream(Paths.get(filename + ".xlsx"))) {
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
