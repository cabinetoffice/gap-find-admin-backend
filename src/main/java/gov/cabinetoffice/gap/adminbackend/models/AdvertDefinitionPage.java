package gov.cabinetoffice.gap.adminbackend.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class AdvertDefinitionPage {

    private String id;

    private String title;

    @Builder.Default
    private List<AdvertDefinitionQuestion> questions = new ArrayList<>();

    public AdvertDefinitionQuestion getQuestionById(String questionId) {
        return questions.stream().filter(page -> Objects.equals(page.getId(), questionId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Question with id " + questionId + " does not exist"));

    }

}
