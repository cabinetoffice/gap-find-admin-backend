package gov.cabinetoffice.gap.adminbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class SpotlightBeanConfig {

    @Bean
    public SecretsManagerClient getSecretsManagerClient() {
        return SecretsManagerClient.builder().region(Region.EU_WEST_2).build();
    }

}
