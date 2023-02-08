package gov.cabinetoffice.gap.adminbackend.dtos.application.questions;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.annotations.NotAllNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * This class inherits only the base fields from {@link QuestionAbstractPatchDTO}. The
 * reason for this is due to the inability to override @NotAllNull, where a unique set of
 * values is required for each inheriting class. The class is used to validate all
 * question types except MultiSelect and Dropdown.
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@NotAllNull(fields = { "profileField", "fieldTitle", "hintText", "displayText", "questionSuffix", "validation" })
public class QuestionGenericPatchDTO extends QuestionAbstractPatchDTO {

    public QuestionGenericPatchDTO(String fieldTitle, String profileField, String hintText, String displayText,
            String questionSuffix, Map<String, Object> validation) {
        super.setFieldTitle(fieldTitle);
        super.setProfileField(profileField);
        super.setHintText(hintText);
        super.setDisplayText(displayText);
        super.setQuestionSuffix(questionSuffix);
        super.setValidation(validation);
    }

}
