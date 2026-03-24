package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
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
    private String exportBucketName;

    @Value("${aws.attachmentsBucket}")
    private String attachmentsBucketName;

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    public void deleteAttachment(String location) {
        final AmazonS3URI s3Uri = new AmazonS3URI(location);
        final String key = s3Uri.getKey();
        log.info("Deleting S3 object {} from bucket {}", key, s3Uri.getBucket());
        s3Client.deleteObject(new DeleteObjectRequest(s3Uri.getBucket(), key));
        s3Client.deleteObject(new DeleteObjectRequest(attachmentsBucketName, key));
    }

    public String generateExportDocSignedUrl(String objectKey) {
        int linkTimeoutDuration = 604800;
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(exportBucketName,
                objectKey).withMethod(HttpMethod.GET)
                        .withExpiration(Date.from(Instant.now().plusSeconds(linkTimeoutDuration)));

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest).toExternalForm();
    }

}
