package gov.cabinetoffice.gap.adminbackend.exceptions;

public class SpotlightExportException extends RuntimeException {

    public SpotlightExportException() {
    }

    public SpotlightExportException(String message) {
        super(message);
    }

    public SpotlightExportException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpotlightExportException(Throwable cause) {
        super(cause);
    }

}
