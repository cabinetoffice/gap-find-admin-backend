package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.Data;

@Data
public class UserDTO {

    private String firstName;

    private String lastName;

    private String organisationName;

    private String emailAddress;

    private String roles;

}
