package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationDefinitionDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;

import java.util.Collections;

import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators.randomApplicationFormEntity;

/**
 * Creates a new application form object without any sections
 */
public class EmptySectionApplicationFormGenerators {

    public static ApplicationFormEntity.ApplicationFormEntityBuilder emptySectionApplicationFormGenerator() {
        return randomApplicationFormEntity().definition(emptySectionApplicationDefinitionGenerator().build());
    }

    public static ApplicationDefinitionDTO.ApplicationDefinitionDTOBuilder emptySectionApplicationDefinitionGenerator() {
        return ApplicationDefinitionDTO.builder().sections(Collections.emptyList());
    }

}
