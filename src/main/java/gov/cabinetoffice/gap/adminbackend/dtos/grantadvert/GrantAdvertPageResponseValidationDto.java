package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;

import gov.cabinetoffice.gap.adminbackend.models.GrantAdvertPageResponse;
import gov.cabinetoffice.gap.adminbackend.validation.annotations.ValidPageResponse;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@ValidPageResponse
public class GrantAdvertPageResponseValidationDto {

    @NotNull
    private UUID grantAdvertId;

    @NotNull
    private String sectionId;

    private GrantAdvertPageResponse page;

}
