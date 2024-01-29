package gov.cabinetoffice.gap.adminbackend.dtos.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApplicationSectionOrderPatchDto {

    private String sectionId;

    private Boolean upOrDown;
}
