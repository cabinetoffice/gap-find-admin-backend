package gov.cabinetoffice.gap.adminbackend.config;

import gov.cabinetoffice.gap.adminbackend.security.interceptors.AuthorizationHeaderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class SpotlightPublisherInterceptor implements WebMvcConfigurer {

    private final SpotlightPublisherConfigProperties spotlightPublisherConfigProperties;

    @Bean
    public AuthorizationHeaderInterceptor authorizationHeaderInterceptor() {
        return new AuthorizationHeaderInterceptor(spotlightPublisherConfigProperties.getSecret());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationHeaderInterceptor()).order(Ordered.HIGHEST_PRECEDENCE);
    }

}
