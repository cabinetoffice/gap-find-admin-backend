package gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch;

import gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions.SpotlightSubmissionDto;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpotlightBatchDto {

    private UUID id;

    private SpotlightBatchStatus status;

    private Instant lastSendAttempt;

    private int version;

    private Instant created = Instant.now();

    private Instant lastUpdated;

    private List<SpotlightSubmissionDto> spotlightSubmissions;

}
