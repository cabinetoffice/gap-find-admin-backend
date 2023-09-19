package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ValidateSessionsRolesRequestBodyDTO {

    private String emailAddress;

    private String roles;

}
