package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class S3ServiceTest {

    private static S3Service s3Service;

    private static AmazonS3 mockS3Client;

    ArgumentCaptor<GeneratePresignedUrlRequest> presignedUrlRequestCaptor = ArgumentCaptor
            .forClass(GeneratePresignedUrlRequest.class);

    @BeforeAll
    static void beforeAll() {
        s3Service = new S3Service();
        mockS3Client = mock(AmazonS3.class);
        ReflectionTestUtils.setField(s3Service, "s3Client", mockS3Client);
    }

    @BeforeEach
    void resetMocks() {
        reset(mockS3Client);
    }

    @Test
    void successfullyGenerateExportSignedURL() throws Exception {

        URL mockUrl = new URL("https://mock_url.co.uk/object_path");

        when(mockS3Client.generatePresignedUrl(any())).thenReturn(mockUrl);

        Instant currentInstant = Instant.now();
        Date mockExpiryDate = Date.from(currentInstant.plusSeconds(604800));

        try (MockedStatic<Date> mockedInstant = mockStatic(Date.class)) {
            mockedInstant.when(() -> Date.from(any())).thenReturn(mockExpiryDate);

            String response = s3Service.generateExportDocSignedUrl("object_path");

            verify(mockS3Client).generatePresignedUrl(presignedUrlRequestCaptor.capture());
            GeneratePresignedUrlRequest capturedValues = presignedUrlRequestCaptor.getValue();

            assertEquals(mockUrl.toExternalForm(), response);
            assertEquals(mockExpiryDate, capturedValues.getExpiration());
            assertEquals(HttpMethod.GET, capturedValues.getMethod());

        }

    }

    @Test
    void unableToGenerateSignedURL() throws Exception {
        when(mockS3Client.generatePresignedUrl(any())).thenThrow(SdkClientException.class);

        assertThrows(SdkClientException.class, () -> s3Service.generateExportDocSignedUrl("object_path"));
    }

}