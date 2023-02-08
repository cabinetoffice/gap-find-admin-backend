package gov.cabinetoffice.gap.adminbackend.models;

import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdvertBuilderQuestionView {

    private AdvertDefinitionQuestionResponseType responseType;

    private String questionId;

    private String questionTitle;

    private String hintText;

    private List<String> options;

    private GrantAdvertQuestionResponse response;

    private String fieldPrefix;

    private AdvertDefinitionQuestionValidation questionValidation;

    private AdvertDefinitionQuestionExampleText exampleText;

}
