package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationDefinitionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormQuestionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormsFoundDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Creates a new application form object. Best used when you want any application form, or
 * want to override direct attributes of the ApplicationFormEntity
 */
public class RandomApplicationFormGenerators {

    public static ApplicationFormEntity.ApplicationFormEntityBuilder randomApplicationFormEntity() {
        return ApplicationFormEntity.builder().grantApplicationId(1).grantSchemeId(1).version(1).created(Instant.now())
                .createdBy(1).applicationName("Application name").applicationStatus(ApplicationStatusEnum.DRAFT)
                .definition(randomApplicationDefinition().build());
    }

    public static ApplicationDefinitionDTO.ApplicationDefinitionDTOBuilder randomApplicationDefinition() {
        return ApplicationDefinitionDTO.builder()
                .sections(Collections.singletonList(randomApplicationFormSection().build()));
    }

    public static ApplicationFormSectionDTO.ApplicationFormSectionDTOBuilder randomApplicationFormSection() {
        return ApplicationFormSectionDTO.builder().sectionId("1").sectionTitle("Section title")
                .sectionStatus(SectionStatusEnum.INCOMPLETE)
                .questions(new LinkedList<>(Collections.singletonList(randomApplicationFormQuestion().build())));
    }

    public static ApplicationFormQuestionDTO.ApplicationFormQuestionDTOBuilder randomApplicationFormQuestion() {
        return ApplicationFormQuestionDTO.builder().questionId("1").fieldTitle("Field title")
                .hintText("A description of this question").responseType(ResponseTypeEnum.ShortAnswer)
                .validation(Collections.singletonMap("mandatory", true));
    }

    public static ApplicationFormsFoundDTO.ApplicationFormsFoundDTOBuilder randomApplicationFormFound() {
        return ApplicationFormsFoundDTO.builder().applicationId(1).inProgressCount(0).submissionCount(0);
    }

}
