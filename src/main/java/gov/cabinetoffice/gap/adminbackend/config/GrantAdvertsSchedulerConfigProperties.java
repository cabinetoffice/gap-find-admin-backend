package gov.cabinetoffice.gap.adminbackend.config;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "grant-adverts-scheduler")
public class GrantAdvertsSchedulerConfigProperties {

    @NotNull
    private String grantAdvertSchedulerQueue;

}
