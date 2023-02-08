package gov.cabinetoffice.gap.adminbackend.exceptions;

public class FieldViolationException extends RuntimeException {

    private String fieldName;

    public FieldViolationException() {
    }

    public FieldViolationException(String message) {
        super(message);
    }

    public FieldViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FieldViolationException(Throwable cause) {
        super(cause);
    }

    public FieldViolationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

}
