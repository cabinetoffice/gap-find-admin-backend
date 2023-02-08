package gov.cabinetoffice.gap.adminbackend.utils;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.Charset;
import java.util.Collections;

import static gov.cabinetoffice.gap.adminbackend.testdata.SubmissionTestData.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CsvUtilsTest {

    @Test
    void createCSVFile() {
        ByteArrayResource csvData = CsvUtils.createCSVData(SPOTLIGHT_EXPORT_HEADERS, SPOTLIGHT_EXPORT_DATA);

        // rebuild the bytearray into a String
        String s = new String(csvData.getByteArray(), Charset.defaultCharset());

        assertThat(s).contains(String.join(",", SPOTLIGHT_EXPORT_HEADERS))
                .contains(String.join(",", SPOTLIGHT_EXPORT_ROW1)).contains(String.join(",", SPOTLIGHT_EXPORT_ROW2));

    }

    @Test
    void createCSVFileNullHeadersThrowException() {
        assertThatThrownBy(() -> CsvUtils.createCSVData(null, SPOTLIGHT_EXPORT_DATA))
                .isInstanceOf(RuntimeException.class).hasMessage("Headers cannot be null or empty");

        assertThatThrownBy(() -> CsvUtils.createCSVData(Collections.emptyList(), SPOTLIGHT_EXPORT_DATA))
                .isInstanceOf(RuntimeException.class).hasMessage("Headers cannot be null or empty");
    }

    @Test
    void createCSVFileNullDataThrowsException() {
        assertThatThrownBy(() -> CsvUtils.createCSVData(SPOTLIGHT_EXPORT_HEADERS, null))
                .isInstanceOf(RuntimeException.class).hasMessage("Data cannot be null or empty");

        assertThatThrownBy(() -> CsvUtils.createCSVData(SPOTLIGHT_EXPORT_HEADERS, Collections.emptyList()))
                .isInstanceOf(RuntimeException.class).hasMessage("Data cannot be null or empty");
    }

}