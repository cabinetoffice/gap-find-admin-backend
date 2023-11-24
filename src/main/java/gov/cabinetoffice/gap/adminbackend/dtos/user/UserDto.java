package gov.cabinetoffice.gap.adminbackend.dtos.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gov.cabinetoffice.gap.adminbackend.models.Department;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

    private String gapUserId;

    private String emailAddress;

    private Department department;

    @JsonCreator
    public UserDto(@JsonProperty("gapUserId") String gapUserId, @JsonProperty("emailAddress") String emailAddress,
            @JsonProperty("department") Department department) {
        this.gapUserId = gapUserId;
        this.emailAddress = emailAddress;
        this.department = department;
    }

}