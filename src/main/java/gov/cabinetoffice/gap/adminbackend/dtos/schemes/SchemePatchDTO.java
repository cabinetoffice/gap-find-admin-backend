package gov.cabinetoffice.gap.adminbackend.dtos.schemes;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.annotations.NotAllNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@NotAllNull(fields = { "name", "ggisReference", "contactEmail" })
public class SchemePatchDTO {

    @Size(min = 1, message = "Enter the name of your grant")
    @Size(max = 255, message = "Name should not be greater than 255 characters")
    private String name;

    @Size(min = 1, message = "Enter your GGIS Scheme Reference Number")
    @Size(max = 255, message = "GGIS Reference should not be greater than 255 characters")
    private String ggisReference;

    @Email(message = "Enter an email address in the correct format, like name@example.com")
    private String contactEmail;

}
