package gov.cabinetoffice.gap.adminbackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertDefinitionQuestionValidation {

    private boolean mandatory;

    private Integer minLength;

    private Integer maxLength;

    private boolean url;

    private Integer lessThan;

    private Integer greaterThan;

    private ComparisonValidation comparedTo;

}
