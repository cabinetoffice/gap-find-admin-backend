package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationDefinitionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormQuestionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Creates a new application form object. Best used when you want an application form with
 * a specific question
 */
public class RandomApplicationFormWithCustomQuestionGenerator {

    public static ApplicationFormEntity.ApplicationFormEntityBuilder randomApplicationFormWithCustomQuestion(
            final ApplicationFormQuestionDTO questionDTO) {
        return ApplicationFormEntity.builder().grantApplicationId(1).grantSchemeId(1).version(1).created(Instant.now())
                .applicationName("Application name").applicationStatus(ApplicationStatusEnum.DRAFT)
                .definition(randomApplicationDefinitionWithCustomQuestion(questionDTO).build());
    }

    public static ApplicationDefinitionDTO.ApplicationDefinitionDTOBuilder randomApplicationDefinitionWithCustomQuestion(
            final ApplicationFormQuestionDTO questionDTO) {
        return ApplicationDefinitionDTO.builder()
                .sections(Collections.singletonList(randomApplicationSectionWithCustomQuestion(questionDTO).build()));
    }

    public static ApplicationFormSectionDTO.ApplicationFormSectionDTOBuilder randomApplicationSectionWithCustomQuestion(
            final ApplicationFormQuestionDTO questionDTO) {
        return ApplicationFormSectionDTO.builder().sectionId("1").sectionTitle("Section title")
                .sectionStatus(SectionStatusEnum.INCOMPLETE)
                .questions(new LinkedList<>(Collections.singletonList(questionDTO)));
    }

}
