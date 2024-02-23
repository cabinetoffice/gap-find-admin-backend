package gov.cabinetoffice.gap.adminbackend.dtos.grantExport;

import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ExportedSubmissionsDto {

    private String name;
    private String zipFileLocation;
    private UUID submissionId;
    private GrantExportStatus status;
    private ZonedDateTime submittedDate;
}
