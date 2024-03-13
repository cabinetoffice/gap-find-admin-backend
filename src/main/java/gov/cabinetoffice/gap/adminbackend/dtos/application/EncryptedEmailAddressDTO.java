package gov.cabinetoffice.gap.adminbackend.dtos.application;

import lombok.Builder;

@Builder
public record EncryptedEmailAddressDTO(byte[] encryptedEmail, boolean deletedUser) {

}
