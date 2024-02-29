package gov.cabinetoffice.gap.adminbackend.dtos.user;

import lombok.Builder;

@Builder
public record DecryptedUserEmailResponse (String emailAddress, String userSub) {

}