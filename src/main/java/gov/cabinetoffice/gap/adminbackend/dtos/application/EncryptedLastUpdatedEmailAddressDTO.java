package gov.cabinetoffice.gap.adminbackend.dtos.application;

import lombok.Builder;

@Builder
public record EncryptedLastUpdatedEmailAddressDTO(byte[] encryptedLastUpdatedEmail, boolean deletedUser) {

}
