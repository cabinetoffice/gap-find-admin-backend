package gov.cabinetoffice.gap.adminbackend.security;

import gov.cabinetoffice.gap.adminbackend.config.JwtTokenFilterConfig;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    private static final String UUID_REGEX_STRING = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    public WebSecurityConfig(final UserService userService, final JwtTokenFilterConfig jwtTokenFilterConfig) {
        this.jwtTokenFilter = new JwtTokenFilter(userService, jwtTokenFilterConfig);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //if you add a path which is hit by the lambda, remember to update also the paths in gov/cabinetoffice/gap/adminbackend/config/LambdasInterceptor.java
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
                .authorizeHttpRequests(auth -> auth
                        .mvcMatchers("/login",
                                "/health",
                                "/emails/sendLambdaConfirmationEmail",
                                "/users/validateAdminSession",
                                "/submissions/{submissionId:" + UUID_REGEX_STRING
                                        + "}/export-batch/{batchExportId:" + UUID_REGEX_STRING + "}/submission",
                                "/submissions/*/export-batch/*/status",
                                "/submissions/{submissionId:" + UUID_REGEX_STRING + "}/export-batch/{batchExportId:"
                                        + UUID_REGEX_STRING + "}/s3-object-key",
                                "/grant-export/{exportId:" + UUID_REGEX_STRING + "}/outstandingCount",
                                "/grant-export/{exportId:" + UUID_REGEX_STRING + "}/failedCount",
                                "/grant-export/{exportId:" + UUID_REGEX_STRING + "}/remainingCount",
                                "/grant-export/{exportId:" + UUID_REGEX_STRING + "}/completed",
                                "/grant-export/{exportId:" + UUID_REGEX_STRING + "}/batch/status",
                                "/grant-export/{exportId:" + UUID_REGEX_STRING + "}/batch/s3-object-key",
                                "/grant-advert/lambda/{grantAdvertId:" + UUID_REGEX_STRING + "}/publish",
                                "/grant-advert/lambda/{grantAdvertId:" + UUID_REGEX_STRING + "}/unpublish",
                                "/users/migrate",
                                "/users/delete",
                                "/users/tech-support-user/**",
                                "/users/admin-user/**",
                                "/users/funding-organisation",
                                "/application-forms/lambda/**",
                                "/feedback/add"
                        )
                        .permitAll()
                        .antMatchers("/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**")
                        .permitAll()
                        .antMatchers("/spotlight-submissions/{spotlightSubmissionId:" + UUID_REGEX_STRING + "}")
                        .permitAll()
                        .antMatchers("/spotlight-batch/status/**",
                                "/spotlight-batch",
                                "/spotlight-batch/{spotlightBatchId" + UUID_REGEX_STRING
                                        + "}/add-spotlight-submission/**",
                                "/spotlight-batch/send-to-spotlight")
                        .permitAll()
                        .anyRequest()
                        .authenticated())

                .formLogin().disable().httpBasic().disable().logout().disable().csrf().disable().exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        http.addFilterAfter(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
