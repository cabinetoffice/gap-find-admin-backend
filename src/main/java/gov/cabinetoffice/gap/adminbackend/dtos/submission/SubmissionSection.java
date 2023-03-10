package gov.cabinetoffice.gap.adminbackend.dtos.submission;

import gov.cabinetoffice.gap.adminbackend.enums.SubmissionSectionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionSection {

    private String sectionId;

    private String sectionTitle;

    private SubmissionSectionStatus sectionStatus;

    private List<SubmissionQuestion> questions;

    public SubmissionQuestion getQuestionById(String questionId) {
        return this.questions.stream().filter((question) -> Objects.equals(question.getQuestionId(), questionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Question with id " + questionId + " does not exist"));
    }

}
