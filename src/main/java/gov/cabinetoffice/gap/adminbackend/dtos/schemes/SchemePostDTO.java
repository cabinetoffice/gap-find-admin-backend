package gov.cabinetoffice.gap.adminbackend.dtos.schemes;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Added this class to keep Swagger correct, as id (schemeId) cannot be present when
 * posting a new object However it's needed for retrieving and updating any objects
 */
@Data
@AllArgsConstructor
public class SchemePostDTO {

    @NotNull(message = "Enter the name of your grant")
    @Size(min = 1, message = "Enter the name of your grant")
    @Size(max = 255, message = "Name should not be greater than 255 characters")
    private String name;

    @NotNull(message = "Enter your GGIS Scheme Reference Number")
    @Size(min = 1, message = "Enter your GGIS Scheme Reference Number")
    @Size(max = 255, message = "GGIS Reference should not be greater than 255 characters")
    private String ggisReference;

    @Email(message = "Enter an email address in the correct format, like name@example.com")
    private String contactEmail;

}
