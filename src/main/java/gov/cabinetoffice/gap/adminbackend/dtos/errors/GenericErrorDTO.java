package gov.cabinetoffice.gap.adminbackend.dtos.errors;

import gov.cabinetoffice.gap.adminbackend.models.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericErrorDTO {

    private ErrorMessage error; // TODO replace this map with ErrorMessage class

    public GenericErrorDTO(String errorMessage) {
        this.error = new ErrorMessage(errorMessage);
    }

}
