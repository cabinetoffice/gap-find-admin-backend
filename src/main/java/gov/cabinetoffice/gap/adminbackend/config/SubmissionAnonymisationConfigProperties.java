package gov.cabinetoffice.gap.adminbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "submission-anonymisation-scheduler")
public class SubmissionAnonymisationConfigProperties {

    private int daysBeforeExpiry = 90;

}
