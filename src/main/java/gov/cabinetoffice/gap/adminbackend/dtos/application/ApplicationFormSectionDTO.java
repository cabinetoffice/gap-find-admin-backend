package gov.cabinetoffice.gap.adminbackend.dtos.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ApplicationFormSectionDTO {

    private String sectionId;

    private String sectionTitle;

    private SectionStatusEnum sectionStatus;

    private List<ApplicationFormQuestionDTO> questions;

    public ApplicationFormSectionDTO(String sectionTitle) {
        this.sectionTitle = sectionTitle;
        this.sectionId = UUID.randomUUID().toString();
        this.questions = Collections.emptyList();
    }

    public ApplicationFormQuestionDTO getQuestionById(String questionId) {
        List<ApplicationFormQuestionDTO> applicationFormQuestionDTOList = this.questions.stream()
                .filter((question) -> Objects.equals(question.getQuestionId(), questionId)).toList();

        if (applicationFormQuestionDTOList.size() > 1) {
            throw new ApplicationFormException("Ambiguous reference - more than one question with id " + questionId);
        }
        else if (applicationFormQuestionDTOList.isEmpty()) {
            throw new NotFoundException("Question with id " + questionId + " does not exist");
        }
        else {
            return applicationFormQuestionDTOList.get(0);
        }
    }

}
