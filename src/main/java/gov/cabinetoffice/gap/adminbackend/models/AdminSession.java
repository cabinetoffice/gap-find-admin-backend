package gov.cabinetoffice.gap.adminbackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminSession implements Serializable {

    private Integer grantAdminId;

    private Integer funderId;

    private String firstName;

    private String lastName;

    private String organisationName;

    private String emailAddress;

    private String roles;

    private boolean isV2Payload;

    public AdminSession(Integer grantAdminId, Integer funderId, boolean isV2Payload, JwtPayload jwtPayload) {
        this.grantAdminId = grantAdminId;
        this.funderId = funderId;
        this.firstName = jwtPayload.getGivenName();
        this.lastName = jwtPayload.getFamilyName();
        this.organisationName = jwtPayload.getDepartmentName();
        this.isV2Payload = isV2Payload;
        this.emailAddress = jwtPayload.getEmailAddress();
        this.roles = jwtPayload.getRoles();
    }

}
