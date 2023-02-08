package gov.cabinetoffice.gap.adminbackend.config;

import gov.cabinetoffice.gap.adminbackend.models.AdvertDefinition;
import gov.cabinetoffice.gap.adminbackend.repositories.GapDefinitionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class AdvertDefinitionConfig {

    @Autowired
    private GapDefinitionRepository gapDefinitionRepository;

    @Bean
    public AdvertDefinition getGrantAdvertDefinition() {
        try {
            AdvertDefinition advertDefinition = gapDefinitionRepository.findByName("Grant Advert Definition")
                    .getDefinition();
            log.info("getGrantAdvertDefinition Bean successfully created");
            return advertDefinition;
        }
        catch (Exception exception) {
            throw new BeanCreationException("getGrantAdvertDefinition", "Failed to create a getGrantAdvertDefinition",
                    exception);
        }

    }

}
