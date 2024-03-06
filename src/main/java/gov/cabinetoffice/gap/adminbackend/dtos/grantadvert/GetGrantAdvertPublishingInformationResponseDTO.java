package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;

import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetGrantAdvertPublishingInformationResponseDTO {

    private UUID grantAdvertId;

    private Instant openingDate;

    private Instant closingDate;

    private Instant firstPublishedDate;

    private Instant lastPublishedDate;

    private Instant unpublishedDate;

    private GrantAdvertStatus grantAdvertStatus;

    private String contentfulSlug;

    private String lastUpdatedByEmail;

    private Instant lastUpdated;

    private Instant created;

    private boolean validLastUpdated;

}
