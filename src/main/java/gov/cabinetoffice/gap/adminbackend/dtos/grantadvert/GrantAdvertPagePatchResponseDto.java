package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;

import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.models.GrantAdvertQuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrantAdvertPagePatchResponseDto {

    @Builder.Default
    private List<GrantAdvertQuestionResponse> questions = new ArrayList<>();

    private GrantAdvertPageResponseStatus status;

    public Optional<GrantAdvertQuestionResponse> getQuestionById(String questionId) {
        return questions.stream().filter(page -> Objects.equals(page.getId(), questionId)).findFirst();

    }

}
