package gov.cabinetoffice.gap.adminbackend.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class AdvertDefinitionQuestion {

    private String id;

    private String title;

    private String displayText;

    private String hintText;

    private AdvertDefinitionQuestionExampleText exampleText;

    private String fieldPrefix;

    private String suffixText;

    private String summaryTitle;

    private String summarySuffixText;

    private List<String> options;

    private AdvertDefinitionQuestionValidation validation;

    private AdvertDefinitionQuestionValidationMessages validationMessages;

    private AdvertDefinitionQuestionResponseType responseType;

}
