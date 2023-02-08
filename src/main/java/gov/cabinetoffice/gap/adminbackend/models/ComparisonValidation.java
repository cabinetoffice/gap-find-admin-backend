package gov.cabinetoffice.gap.adminbackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparisonValidation {

    private String questionId;

    private String errorMessage;

    private boolean greaterThan = false;

    private boolean lessThan = false;

}
