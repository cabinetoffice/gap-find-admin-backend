package gov.cabinetoffice.gap.adminbackend.dtos.errors;

import gov.cabinetoffice.gap.adminbackend.models.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FieldErrorsDTO {

    List<ValidationError> fieldErrors;

}
