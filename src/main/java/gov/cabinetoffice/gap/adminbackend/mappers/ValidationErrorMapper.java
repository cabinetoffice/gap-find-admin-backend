package gov.cabinetoffice.gap.adminbackend.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import gov.cabinetoffice.gap.adminbackend.models.ClassError;
import gov.cabinetoffice.gap.adminbackend.models.ValidationError;

@Mapper(componentModel = "spring")
public interface ValidationErrorMapper {

    @Mapping(target = "fieldName", source = "field")
    @Mapping(target = "errorMessage", source = "defaultMessage")
    ValidationError springFieldErrorToValidationError(FieldError springFieldError);

    List<ValidationError> springFieldErrorListToValidationErrorList(List<FieldError> springFieldError);

    @Mapping(target = "className", source = "objectName")
    @Mapping(target = "errorMessage", source = "defaultMessage")
    ClassError springObjectErrorToClassError(ObjectError springFieldError);

    List<ClassError> springObjectErrorListToClassErrorList(List<ObjectError> springFieldError);

}
