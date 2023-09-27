package gov.cabinetoffice.gap.adminbackend.controllers;

import com.contentful.java.cma.model.CMAHttpException;
import gov.cabinetoffice.gap.adminbackend.annotations.NotAllNull;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.ClassErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.FieldErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapper;
import gov.cabinetoffice.gap.adminbackend.models.ClassError;
import gov.cabinetoffice.gap.adminbackend.models.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private ValidationErrorMapper validationErrorMapper;

    /**
     * Currently this is an exception handler for all controllers, catching any
     * RuntimeExceptions stemming from REST calls
     */
    @ExceptionHandler(value = { RuntimeException.class })
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex, new GenericErrorDTO(ex.getMessage()), new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = { ConstraintViolationException.class })
    protected ResponseEntity<Object> handleConflict(ConstraintViolationException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().stream().findFirst().get();

        // if validation violation was raised by NotAllNullValidator, then map to
        // ClassErrorsDto. Else, map to FieldErrorsDto
        if (Objects.equals(constraintViolation.getConstraintDescriptor().getAnnotation().annotationType(),
                NotAllNull.class)) {
            ClassErrorsDTO errorResponse = new ClassErrorsDTO(ex.getConstraintViolations().stream()
                    .map((violation -> new ClassError(violation.getRootBeanClass().getName(), violation.getMessage())))
                    .toList());
            return ResponseEntity.badRequest().body(errorResponse);
        }
        else {
            FieldErrorsDTO errorResponse = new FieldErrorsDTO(ex.getConstraintViolations().stream().map(
                    (violation -> new ValidationError(violation.getPropertyPath().toString(), violation.getMessage())))
                    .toList());
            return ResponseEntity.badRequest().body(errorResponse);
        }

    }

    @ExceptionHandler(value = { FieldViolationException.class })
    protected ResponseEntity<Object> handleConflict(FieldViolationException ex, WebRequest request) {
        FieldErrorsDTO errorResponse = new FieldErrorsDTO(
                Collections.singletonList(new ValidationError(ex.getFieldName(), ex.getMessage())));

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = { UnauthorizedException.class })
    protected ResponseEntity<Object> handleConflict(UnauthorizedException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex, new GenericErrorDTO(ex.getMessage()), new HttpHeaders(),
                HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(value = { AccessDeniedException.class })
    protected ResponseEntity<Object> handleConflict(AccessDeniedException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex, new GenericErrorDTO(ex.getMessage()), new HttpHeaders(),
                HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = { NotFoundException.class })
    protected ResponseEntity<Object> handleConflict(NotFoundException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex, new GenericErrorDTO(ex.getMessage()), new HttpHeaders(),
                HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = { CMAHttpException.class })
    protected ResponseEntity<Object> handleConflict(CMAHttpException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex, new GenericErrorDTO("Contentful exception."), new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * This overridden method handles controller methods being called with invalid
     * arguments It'll spit back a json representation of a FieldErrorsDTO or a
     * ClassErrorsDTO
     */
    @NotNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NotNull MethodArgumentNotValidException ex,
                                                                  @NotNull HttpHeaders headers,
                                                                  @NotNull HttpStatus status,
                                                                  @NotNull WebRequest request) {
        log.error(ex.getMessage(), ex);
        BindingResult bindResults = ex.getBindingResult();

        return constructErrorObjectFromBindResults(bindResults);
    }

    /**
     * This overridden method handles controller methods which cause bind exceptions It'll
     * spit back a json representation of a FieldErrorsDTO or a ClassErrorsDTO
     */
    @NotNull
    @Override
    protected ResponseEntity<Object> handleBindException(@NotNull BindException ex,
                                                         @NotNull HttpHeaders headers,
                                                         @NotNull HttpStatus status,
                                                         @NotNull WebRequest request) {
        log.error(ex.getMessage(), ex);
        BindingResult bindResults = ex.getBindingResult();

        return constructErrorObjectFromBindResults(bindResults);
    }

    private ResponseEntity<Object> constructErrorObjectFromBindResults(BindingResult bindResults) {
        if (bindResults.hasGlobalErrors()) {
            List<ClassError> classErrors = validationErrorMapper
                    .springObjectErrorListToClassErrorList(bindResults.getGlobalErrors());
            ClassErrorsDTO errorResponse = new ClassErrorsDTO(classErrors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        else {
            List<ValidationError> fieldErrors = validationErrorMapper
                    .springFieldErrorListToValidationErrorList(bindResults.getFieldErrors());
            FieldErrorsDTO errorResponse = new FieldErrorsDTO(fieldErrors);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

}
