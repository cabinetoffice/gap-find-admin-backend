package gov.cabinetoffice.gap.adminbackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtPayloadV2 {

    private String sub;
    private String roles;
    private String emailAddress;
    private String department;
    private String iss;
    private String aud;
    private int exp;
    private int iat;

}
