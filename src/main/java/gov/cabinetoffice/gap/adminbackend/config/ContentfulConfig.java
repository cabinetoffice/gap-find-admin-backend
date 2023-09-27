package gov.cabinetoffice.gap.adminbackend.config;

import com.contentful.java.cda.CDAClient;
import com.contentful.java.cma.CMAClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class ContentfulConfig {

    private final ContentfulConfigProperties configProperties;

    @Bean
    public CMAClient getContentfulManagementClient() {
        return new CMAClient.Builder().setAccessToken(configProperties.getAccessToken())
                .setSpaceId(configProperties.getSpaceId()).setEnvironmentId(configProperties.getEnvironmentId())
                .build();
    }

    @Bean
    public CDAClient getContentfulDeliveryClient() {

        return CDAClient.builder().setToken(configProperties.getDeliveryAPIAccessToken())
                .setSpace(configProperties.getSpaceId()).setEnvironment(configProperties.getEnvironmentId()).build();
    }

}
