package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.Builder;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

@Builder
public record UserV2DTO(String gapUserId, String emailAddress, String sub, List<RoleDto> roles, RoleDto role,
        @Nullable DepartmentDto department, @Nullable Instant created) {
}
