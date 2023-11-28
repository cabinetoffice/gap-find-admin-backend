package gov.cabinetoffice.gap.adminbackend.config;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class SNSConfig {

    @Primary
    @Bean
    public AmazonSNSClient getSnsClient() {
        return (AmazonSNSClient) AmazonSNSClientBuilder.defaultClient();
    }

}
