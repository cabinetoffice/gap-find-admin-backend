package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    @Value("${cloud.aws.s3.submissions-export-bucket-name}")
    private String attachmentsBucket;

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    public String generateExportDocSignedUrl(String objectKey) {
        int linkTimeoutDuration = 604800;
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(attachmentsBucket,
                objectKey).withMethod(HttpMethod.GET)
                        .withExpiration(Date.from(Instant.now().plusSeconds(linkTimeoutDuration)));

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest).toExternalForm();
    }

}
