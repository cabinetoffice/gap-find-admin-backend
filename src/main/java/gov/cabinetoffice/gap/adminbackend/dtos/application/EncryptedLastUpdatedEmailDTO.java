package gov.cabinetoffice.gap.adminbackend.dtos.application;

import lombok.Builder;
import lombok.Data;

@Builder
public record EncryptedLastUpdatedEmailDTO (byte[] encryptedLastUpdatedEmail) {

}
