package gov.cabinetoffice.gap.adminbackend.dtos.submission;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SubmissionDto {
    private UUID submissionId;

    private Integer schemeId;

    private String schemeName;

    private String legalName;

    private String applicationName;

    private List<SubmissionSection> sections;
}
