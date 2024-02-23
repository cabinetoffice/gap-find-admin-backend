package gov.cabinetoffice.gap.adminbackend.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class UserEmailResponseDto {
    private byte[] emailAddress;
    private String sub;

    public UserEmailResponseDto(byte[] emailAddress, String sub) {
        this.emailAddress = emailAddress;
        this.sub = sub;
    }
}