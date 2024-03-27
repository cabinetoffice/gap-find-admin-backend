package gov.cabinetoffice.gap.adminbackend.dtos.user;

import lombok.Builder;
import java.util.List;

@Builder
public record UserEmailRequestDto(List<String> userSubs) {

}