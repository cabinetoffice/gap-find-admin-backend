package gov.cabinetoffice.gap.adminbackend.dtos.application.questions;

import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class QuestionSessionDTO {

    @Size(min = 2, message = "Question title can not be less than 2 characters")
    @Size(max = 255, message = "Question title can not be greater than 255 characters")
    private String fieldTitle;

    private ResponseTypeEnum responseType;

    @Size(min = 1)
    private String profileField; // Can admins ever access this field, or only available
                                 // on template questions?

    @Size(max = 1000, message = "Question hint can not be greater than 1000 characters")
    private String hintText;

    @Size(max = 6000, message = "Text input can not be over 6000 characters")
    private String displayText;

    @NotEmpty(message = "Select whether the question is optional or not")
    private String optional;

    private Integer version;

}
