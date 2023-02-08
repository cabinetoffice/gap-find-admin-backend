package gov.cabinetoffice.gap.adminbackend.validation;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@Builder
public class ValidationResult {

    @Builder.Default
    private boolean isValid = false;

    @Builder.Default
    private LinkedHashMap<String, String> fieldErrors = new LinkedHashMap<>();

    public void addError(String key, String value) {
        this.fieldErrors.put(key, value);
    }

}
