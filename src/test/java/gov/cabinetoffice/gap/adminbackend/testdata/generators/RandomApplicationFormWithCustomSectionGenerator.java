package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationDefinitionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;

import java.time.Instant;
import java.util.Collections;

/**
 * Creates a new application form object. Best used when you want an application form with
 * a specific section
 */
public class RandomApplicationFormWithCustomSectionGenerator {

    public static ApplicationFormEntity.ApplicationFormEntityBuilder randomApplicationFormWithCustomSection(
            final ApplicationFormSectionDTO sectionDTO) {
        return ApplicationFormEntity.builder().grantApplicationId(1).grantSchemeId(1).version(1).created(Instant.now())
                .applicationName("Application name").applicationStatus(ApplicationStatusEnum.DRAFT)
                .definition(randomApplicationDefinitionWithCustomSection(sectionDTO).build());
    }

    public static ApplicationDefinitionDTO.ApplicationDefinitionDTOBuilder randomApplicationDefinitionWithCustomSection(
            final ApplicationFormSectionDTO sectionDTO) {
        return ApplicationDefinitionDTO.builder().sections(Collections.singletonList(sectionDTO));
    }

}
