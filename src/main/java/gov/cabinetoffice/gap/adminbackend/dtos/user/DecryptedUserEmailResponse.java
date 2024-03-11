package gov.cabinetoffice.gap.adminbackend.dtos.user;

import lombok.Builder;

@Builder
public record DecryptedUserEmailResponse (byte[] emailAddress, String userSub) {

}