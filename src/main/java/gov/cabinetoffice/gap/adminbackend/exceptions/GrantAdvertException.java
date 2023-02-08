package gov.cabinetoffice.gap.adminbackend.exceptions;

public class GrantAdvertException extends RuntimeException {

    public GrantAdvertException() {
    }

    public GrantAdvertException(String message) {
        super(message);
    }

    public GrantAdvertException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrantAdvertException(Throwable cause) {
        super(cause);
    }

}
