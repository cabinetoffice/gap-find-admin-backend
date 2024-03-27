package gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEditor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class GetSchemeEditorsException extends RuntimeException {
    public GetSchemeEditorsException(String message) {
        super(message);
    }
}