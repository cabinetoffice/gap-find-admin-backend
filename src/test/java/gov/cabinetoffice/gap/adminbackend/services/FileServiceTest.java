package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig
@WithAdminSession
class FileServiceTest {

    @Spy
    private FileService fileService;

    @Value("classpath:spotlight/XLSX_Spotlight_Template.xlsx")
    Resource exampleFile;

    @Test
    public void createTemporaryFileAsExpected() throws IOException {
        final byte[] data = exampleFile.getInputStream().readAllBytes();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(data);
        final InputStreamResource expectedResult = new InputStreamResource(
                new ByteArrayInputStream(outputStream.toByteArray()));

        final InputStreamResource result = fileService.createTemporaryFile(outputStream, "test_file_name");

        assertThat(result.getDescription()).isEqualTo(expectedResult.getDescription());
    }

    @Test
    public void createTemporaryFileThrowException() {
        assertThatThrownBy(() -> fileService.createTemporaryFile(null, "test_file_name"))
                .isInstanceOf(RuntimeException.class);

    }

}