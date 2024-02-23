package gov.cabinetoffice.gap.adminbackend.exceptions;

public class GrantExportBatchException extends RuntimeException {

    public GrantExportBatchException() {
    }

    public GrantExportBatchException(String message) {
        super(message);
    }

    public GrantExportBatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrantExportBatchException(Throwable cause) {
        super(cause);
    }

}
