package gov.cabinetoffice.gap.adminbackend.validation;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormPatchDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AtLeastOneFieldNotNullValidator implements ConstraintValidator<AtLeastOneFieldNotNull, ApplicationFormPatchDTO> {

    @Override
    public void initialize(AtLeastOneFieldNotNull constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(ApplicationFormPatchDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return false;
        }
        
        // Check if at least one field is not null
        return dto.getApplicationStatus() != null || dto.getAllowsMultipleSubmissions() != null;
    }
}

