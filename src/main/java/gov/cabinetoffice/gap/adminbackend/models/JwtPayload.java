package gov.cabinetoffice.gap.adminbackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtPayload {

    private String sub;

    private String givenName;

    private String familyName;

    private String departmentName;

    private String emailAddress;

    private String roles;

    private String department;

    private String iss;

    private String aud;

    private int exp;

    private int iat;

}
