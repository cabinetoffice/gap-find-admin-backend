package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;


import lombok.Builder;

@Builder
public record FullGrantAdvertPublishingInformationResponseDTO(GetGrantAdvertPublishingInformationResponseDTO publishingInfo, String lastUpdatedByEmail) {

}
