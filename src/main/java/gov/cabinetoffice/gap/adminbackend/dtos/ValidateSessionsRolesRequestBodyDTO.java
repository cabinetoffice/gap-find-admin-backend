package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.Data;

@Data
public class ValidateSessionsRolesRequestBodyDTO {

    private String emailAddress;

    private String roles;

}
