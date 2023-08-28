package gov.cabinetoffice.gap.adminbackend.dtos;

import io.micrometer.core.lang.Nullable;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserDTO {

    private String firstName;

    private String lastName;

    private String organisationName;

    private String emailAddress;

    private String gapUserId;

    private String sub;

    @Nullable
    private Instant created;

}
