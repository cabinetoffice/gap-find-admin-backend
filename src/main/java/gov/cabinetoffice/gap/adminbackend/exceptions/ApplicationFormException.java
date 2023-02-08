package gov.cabinetoffice.gap.adminbackend.exceptions;

public class ApplicationFormException extends RuntimeException {

    public ApplicationFormException() {
    }

    public ApplicationFormException(String message) {
        super(message);
    }

    public ApplicationFormException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationFormException(Throwable cause) {
        super(cause);
    }

}
