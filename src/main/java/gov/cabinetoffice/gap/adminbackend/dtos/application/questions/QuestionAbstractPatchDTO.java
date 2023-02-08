package gov.cabinetoffice.gap.adminbackend.dtos.application.questions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Map;

/**
 * This abstract class is used for dynamically validating different types of question The
 * fields and validation here are inherited by {@link QuestionGenericPatchDTO} and
 * {@link QuestionOptionsPatchDTO}
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class QuestionAbstractPatchDTO {

    @Size(min = 1)
    private String profileField;

    @Size(min = 2, message = "Question title can not be less than 2 characters")
    @Size(max = 255, message = "Question title can not be greater than 255 characters")
    private String fieldTitle;

    @Size(max = 1000, message = "Question hint can not be greater than 1000 characters")
    private String hintText;

    @Size(min = 2, message = "Text input can not be less than 2 characters")
    @Size(max = 6000, message = "Text input can not be greater than 6000 characters")
    private String displayText;

    @Size(min = 1, message = "Question suffix can not be blank")
    @Size(max = 255, message = "Question suffix can not be greater than 255 characters")
    private String questionSuffix;

    private Map<String, Object> validation;

}
