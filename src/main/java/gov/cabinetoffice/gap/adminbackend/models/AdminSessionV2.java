package gov.cabinetoffice.gap.adminbackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminSessionV2 implements Serializable {

    private Integer grantAdminId;

    private Integer funderId;

    private String firstName;

    private String lastName;

    private String organisationName;

    private String emailAddress;

    public AdminSessionV2(Integer grantAdminId, Integer funderId, JwtPayloadV2 jwtPayload) {
        this.grantAdminId = grantAdminId;
        this.funderId = funderId;
        this.emailAddress = jwtPayload.getEmailAddress();
        this.organisationName = jwtPayload.getDepartment();
        this.emailAddress = jwtPayload.getEmailAddress();
    }

}
