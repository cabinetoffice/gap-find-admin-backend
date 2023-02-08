package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;

import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class GetGrantAdvertStatusResponseDTO {

    private UUID grantAdvertId;

    private GrantAdvertStatus grantAdvertStatus;

    private String contentfulSlug;

}
