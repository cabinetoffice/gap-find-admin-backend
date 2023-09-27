package gov.cabinetoffice.gap.adminbackend.dtos.application.questions;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionGenericPostDTO extends QuestionAbstractPostDTO {

    public QuestionGenericPostDTO(String fieldTitle, ResponseTypeEnum responseType, String profileField,
            String hintText, String displayText, String questionSuffix, Map<String, Object> validation) {
        super.setFieldTitle(fieldTitle);
        super.setResponseType(responseType);
        super.setProfileField(profileField);
        super.setHintText(hintText);
        super.setDisplayText(displayText);
        super.setQuestionSuffix(questionSuffix);
        super.setValidation(validation);
    }

}
