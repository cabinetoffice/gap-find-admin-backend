package gov.cabinetoffice.gap.adminbackend.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "completion-statistics-scheduler")
public class CompletionStatisticsSchedulerConfigProperties {

    @NotNull
    private String queue;

}
