package gov.cabinetoffice.gap.adminbackend;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration class that specifically enables JPA Repositories, it was necessary for
 * tests to remove this from AdminBackendApplication
 */
@Configuration
@EnableJpaRepositories
public class JpaConfig {

}
