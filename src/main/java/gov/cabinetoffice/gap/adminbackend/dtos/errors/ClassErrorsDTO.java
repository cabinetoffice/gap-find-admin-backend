package gov.cabinetoffice.gap.adminbackend.dtos.errors;

import gov.cabinetoffice.gap.adminbackend.models.ClassError;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ClassErrorsDTO {

    List<ClassError> classErrors;

}
