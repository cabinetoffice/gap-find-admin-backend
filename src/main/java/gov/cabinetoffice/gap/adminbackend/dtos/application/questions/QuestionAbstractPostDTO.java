package gov.cabinetoffice.gap.adminbackend.dtos.application.questions;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

/**
 * This abstract class is used for dynamically validating different types of question The
 * fields and validation here are inherited by {@link QuestionGenericPatchDTO} and
 * {@link QuestionOptionsPatchDTO}
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class QuestionAbstractPostDTO {

    @Size(min = 2, message = "Question title can not be less than 2 characters")
    @NotNull(message = "Question title can not be less than 2 characters")
    @Size(max = 255, message = "Question title can not be greater than 255 characters")
    private String fieldTitle;

    @NotNull(message = "Question type can not be null")
    private ResponseTypeEnum responseType;

    @Size(min = 1)
    private String profileField; // Can admins ever access this field, or only available
                                 // on template questions?

    @Size(max = 1000, message = "Question hint can not be greater than 1000 characters")
    private String hintText;

    @Size(max = 6000, message = "Text input can not be over 6000 characters")
    private String displayText;

    @Size(max = 255, message = "Question suffix can not be greater than 255 characters")
    private String questionSuffix;

    @NotEmpty(message = "Select whether the question is mandatory or not")
    private Map<String, Object> validation; // FIXME replace Map with Validation object
                                            // with all possible constraints, will allow
                                            // us to enforce mandatory field

}
