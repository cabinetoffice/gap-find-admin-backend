package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.Builder;

@Builder
public record UpdateFundingOrgDto(String sub, String email, String departmentName) {}
