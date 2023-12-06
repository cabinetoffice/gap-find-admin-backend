package gov.cabinetoffice.gap.adminbackend.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Configuration("spotlightPublisherConfigurationProperties")
@ConfigurationProperties(prefix = "spotlight-publisher")
public class SpotlightPublisherConfigProperties {

    private String secret;

}
