package gov.cabinetoffice.gap.adminbackend.exceptions;

public class SchemeEntityException extends RuntimeException {

    public SchemeEntityException() {
    }

    public SchemeEntityException(String message) {
        super(message);
    }

    public SchemeEntityException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

}
