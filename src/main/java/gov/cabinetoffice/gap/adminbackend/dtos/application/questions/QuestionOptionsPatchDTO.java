package gov.cabinetoffice.gap.adminbackend.dtos.application.questions;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.annotations.NotAllNull;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * Inherits the base fields from {@link QuestionAbstractPatchDTO} and additionally
 * requires options. The class is used to validate MultiSelect and Dropdown type
 * questions.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NotAllNull(
        fields = { "profileField", "fieldTitle", "hintText", "options", "displayText", "questionSuffix", "validation" })
public class QuestionOptionsPatchDTO extends QuestionAbstractPatchDTO {

    @Size(min = 2, message = "You must have a minimum of two options")
    private List<@Size(min = 1, message = "Enter an option") @Size(max = 255,
            message = "Option cannot be greater than 255 characters") String> options;

    public QuestionOptionsPatchDTO(String fieldTitle, String profileField, String hintText, String displayText,
        String questionSuffix, ResponseTypeEnum responseType, Map<String, Object> validation, List<String> options) {
        super.setFieldTitle(fieldTitle);
        super.setProfileField(profileField);
        super.setHintText(hintText);
        super.setDisplayText(displayText);
        super.setQuestionSuffix(questionSuffix);
        super.setResponseType(responseType);
        super.setValidation(validation);
        this.options = options;
    }

}
