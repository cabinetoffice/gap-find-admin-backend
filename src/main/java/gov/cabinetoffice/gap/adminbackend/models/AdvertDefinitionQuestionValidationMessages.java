package gov.cabinetoffice.gap.adminbackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertDefinitionQuestionValidationMessages {

    private String mandatory;

    private String minLength;

    private String maxLength;

    private String url;

    private String lessThan;

    private String greaterThan;

}
