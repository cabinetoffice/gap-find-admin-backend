package gov.cabinetoffice.gap.adminbackend.controllers.pages;

import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.models.CustomErrorCode;
import gov.cabinetoffice.gap.adminbackend.models.CustomErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(assignableTypes = PagesAdvertController.class)
@Order(0)
@Slf4j
public class PagesControllerExceptionsHandler extends ResponseEntityExceptionHandler {

    // 400
    @ExceptionHandler(value = { IllegalArgumentException.class })
    protected ResponseEntity<Object> handleConflict(IllegalArgumentException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex, CustomErrorMessage.builder().code(CustomErrorCode.WRONG_ARGUMENT_TYPE_PASSED)
                .message(ex.getMessage()).build(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    // 403
    @ExceptionHandler(value = { AccessDeniedException.class })
    protected ResponseEntity<Object> handleConflict(AccessDeniedException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex,
                CustomErrorMessage.builder().code(CustomErrorCode.ACCESS_DENIED).message(ex.getMessage()).build(),
                new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    // 404
    @ExceptionHandler(value = { SchemeEntityException.class })
    protected ResponseEntity<Object> handleConflict(SchemeEntityException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex, CustomErrorMessage.builder().code(CustomErrorCode.GRANT_SCHEME_NOT_FOUND)
                .message(ex.getMessage()).build(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = { NotFoundException.class })
    protected ResponseEntity<Object> handleConflict(NotFoundException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return handleExceptionInternal(ex, CustomErrorMessage.builder().code(CustomErrorCode.GRANT_ADVERT_NOT_FOUND)
                .message(ex.getMessage()).build(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

}
