package gov.cabinetoffice.gap.adminbackend.exceptions;

public class ConvertHtmlToMdException extends RuntimeException {

    public ConvertHtmlToMdException() {
    }

    public ConvertHtmlToMdException(String message) {
        super(message);
    }

    public ConvertHtmlToMdException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConvertHtmlToMdException(Throwable cause) {
        super(cause);
    }

}
