package gov.cabinetoffice.gap.adminbackend.security;

import gov.cabinetoffice.gap.adminbackend.config.JwtTokenFilterConfig;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    private static final String UUID_REGEX_STRING = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    public WebSecurityConfig(final UserService userService, final JwtTokenFilterConfig jwtTokenFilterConfig) {
        this.jwtTokenFilter = new JwtTokenFilter(userService, jwtTokenFilterConfig);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
                .authorizeHttpRequests(auth -> auth
                        .mvcMatchers("/login", "/health", "/emails/sendLambdaConfirmationEmail",
                                "/users/validateAdminSession", "/submissions/{submissionId:" + UUID_REGEX_STRING
                                        + "}/export-batch/{batchExportId:" + UUID_REGEX_STRING + "}/submission",
                                "/submissions/*/export-batch/*/status",
                                "/submissions/{submissionId:" + UUID_REGEX_STRING + "}/export-batch/{batchExportId:"
                                        + UUID_REGEX_STRING + "}/signedUrl",
                                "/export-batch/{exportId:" + UUID_REGEX_STRING + "}/outstandingCount",
                                "/grant-advert/lambda/{grantAdvertId:" + UUID_REGEX_STRING + "}/publish",
                                "/grant-advert/lambda/{grantAdvertId:" + UUID_REGEX_STRING + "}/unpublish",
                                "/users/migrate", "/users/delete/**",
                                "/application-forms/lambda/**"
                                )
                        .permitAll()
                        .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger-ui.html",
                                "/webjars/**")
                        .permitAll().anyRequest().authenticated())

                .formLogin().disable().httpBasic().disable().logout().disable().csrf().disable().exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        http.addFilterAfter(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
