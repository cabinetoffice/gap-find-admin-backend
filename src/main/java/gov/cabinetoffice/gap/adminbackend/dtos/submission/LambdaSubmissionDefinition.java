package gov.cabinetoffice.gap.adminbackend.dtos.submission;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LambdaSubmissionDefinition {

    private Integer schemeId;

    private String schemeName;

    private String legalName;

    private String gapId;

    private ZonedDateTime submittedDate;

    private List<SubmissionSection> sections;

    private UUID userId;

}
