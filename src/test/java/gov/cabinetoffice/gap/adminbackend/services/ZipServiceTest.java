package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
@WithAdminSession
public class ZipServiceTest {

    @InjectMocks
    private ZipService zipService;

    final List<String> headersList = Arrays.asList("Header1", "Header2", "Header3");

    final List<List<List<String>>> dataList = Arrays.asList(Arrays.asList(Arrays.asList("Data1", "Data2", "Data3")),
            Arrays.asList(Arrays.asList("Data4", "Data5", "Data6")));

    final List<String> filenames = Arrays.asList("File1.xlsx", "File2.xlsx");

    @Test
    public void createZip() {
        ByteArrayOutputStream result = zipService.createZip(headersList, dataList, filenames);
        assertNotNull(result);
    }

}
