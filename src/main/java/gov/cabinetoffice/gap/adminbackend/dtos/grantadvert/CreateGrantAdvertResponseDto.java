package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;

import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CreateGrantAdvertResponseDto {

    private UUID id;

    private Integer schemeId;

    private Integer version;

    private Instant created;

    private Integer createdById;

    private Instant lastUpdated;

    private Integer lastUpdatedById;

    private GrantAdvertStatus status;

    private String contentfulEntryId;

    private String contentfulSlug;

    private String grantAdvertName;

}
