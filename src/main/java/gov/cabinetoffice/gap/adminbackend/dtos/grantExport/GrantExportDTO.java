package gov.cabinetoffice.gap.adminbackend.dtos.grantExport;

import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class GrantExportDTO {
    private UUID exportBatchId;
    private UUID submissionId;
    private Integer applicationId;
    private GrantExportStatus status;
    private String emailAddress;
    private Instant created;
    private Integer createdBy;
    private Instant lastUpdated;
    private String location;
}
