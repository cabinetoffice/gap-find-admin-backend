package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MigrateUserDto {

    private String oneLoginSub;

    private UUID colaSub;

}
