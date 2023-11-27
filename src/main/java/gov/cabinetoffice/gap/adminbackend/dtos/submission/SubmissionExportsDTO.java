package gov.cabinetoffice.gap.adminbackend.dtos.submission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionExportsDTO {

    private String label;

    private String s3key;

}
