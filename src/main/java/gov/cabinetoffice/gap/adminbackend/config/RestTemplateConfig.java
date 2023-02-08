package gov.cabinetoffice.gap.adminbackend.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplateBuilder().requestFactory(HttpComponentsClientHttpRequestFactory.class).build();
        // The standard HTTP library doesn't support PATCH requests but Salesforce
        // requires these to update entities.
    }

}
