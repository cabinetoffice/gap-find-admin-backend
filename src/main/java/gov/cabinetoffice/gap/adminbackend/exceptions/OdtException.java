package gov.cabinetoffice.gap.adminbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OdtException extends RuntimeException {

    public OdtException() {
    }

    public OdtException(String message) {
        super(message);
    }

    public OdtException(String message, Throwable cause) {
        super(message, cause);
    }

    public OdtException(Throwable cause) {
        super(cause);
    }
}
