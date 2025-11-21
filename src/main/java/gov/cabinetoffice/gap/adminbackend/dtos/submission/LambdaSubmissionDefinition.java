package gov.cabinetoffice.gap.adminbackend.dtos.submission;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class LambdaSubmissionDefinition {

    private Integer schemeId;

    private String schemeName;

    private String legalName;

    private String gapId;

    private String submissionName;

    private ZonedDateTime submittedDate;

    private List<SubmissionSection> sections;

    private String email;

    private Integer schemeVersion;

    private boolean hasAttachments;

}
