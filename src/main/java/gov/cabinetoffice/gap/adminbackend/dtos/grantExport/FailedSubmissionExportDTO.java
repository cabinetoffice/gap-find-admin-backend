package gov.cabinetoffice.gap.adminbackend.dtos.grantExport;

import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionSection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class FailedSubmissionExportDTO {

    private UUID submissionId;

    private Integer schemeId;

    private String schemeName;

    private String legalName;

    private String applicationName;

    private List<SubmissionSection> sections;

    private String attachmentsZipLocation;
}
