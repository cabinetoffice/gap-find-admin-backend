package gov.cabinetoffice.gap.adminbackend.dtos.application;

import gov.cabinetoffice.gap.adminbackend.annotations.NotAllNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NotAllNull(fields = { "grantApplicationId", "grantSchemeId", "applicationName" },
        message = "Request body did not provide any valid searchable fields.")
public class ApplicationFormExistsDTO {

    private Integer grantApplicationId;

    private Integer grantSchemeId;

    private String applicationName;

}
