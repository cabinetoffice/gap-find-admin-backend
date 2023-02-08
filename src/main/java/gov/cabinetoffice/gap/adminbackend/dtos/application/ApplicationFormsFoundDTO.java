package gov.cabinetoffice.gap.adminbackend.dtos.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApplicationFormsFoundDTO {

    private Integer applicationId;

    private Integer inProgressCount;

    private Integer submissionCount;

}
