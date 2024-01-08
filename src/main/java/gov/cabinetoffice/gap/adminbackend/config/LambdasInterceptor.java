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
public class LambdasInterceptor implements WebMvcConfigurer {

    private static final String UUID_REGEX_STRING = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    private final SpotlightPublisherConfigProperties spotlightPublisherConfigProperties;

    private final LambdaSecretConfigProperties lambdaSecretConfigProperties;

    @Bean(name = "spotlightPublisherLambdaInterceptor")
    public AuthorizationHeaderInterceptor spotlightPublisherLambdaInterceptor() {
        return new AuthorizationHeaderInterceptor(spotlightPublisherConfigProperties.getSecret(),
                spotlightPublisherConfigProperties.getPrivateKey());
    }

    @Bean(name = "submissionExportAndScheduledPublishingLambdasInterceptor")
    AuthorizationHeaderInterceptor submissionExportAndScheduledPublishingLambdasInterceptor() {
        return new AuthorizationHeaderInterceptor(lambdaSecretConfigProperties.getSecret(),
                lambdaSecretConfigProperties.getPrivateKey());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spotlightPublisherLambdaInterceptor())
                .addPathPatterns("/spotlight-submissions/**", "/spotlight-batch/**").order(Ordered.HIGHEST_PRECEDENCE);

        registry.addInterceptor(submissionExportAndScheduledPublishingLambdasInterceptor())
                .addPathPatterns(
                        "/emails/sendLambdaConfirmationEmail", "/submissions/{submissionId:" + UUID_REGEX_STRING
                                + "}/export-batch/{batchExportId:" + UUID_REGEX_STRING + "}/submission",
                        "/submissions/*/export-batch/*/status",
                        "/submissions/{submissionId:" + UUID_REGEX_STRING + "}/export-batch/{batchExportId:"
                                + UUID_REGEX_STRING + "}/s3-object-key",
                        "/export-batch/{exportId:" + UUID_REGEX_STRING + "}/outstandingCount",
                        "/grant-advert/lambda/{grantAdvertId:" + UUID_REGEX_STRING + "}/publish",
                        "/grant-advert/lambda/{grantAdvertId:" + UUID_REGEX_STRING + "}/unpublish",
                        "/application-forms/lambda/**")
                .order(Ordered.HIGHEST_PRECEDENCE);
    }

}
