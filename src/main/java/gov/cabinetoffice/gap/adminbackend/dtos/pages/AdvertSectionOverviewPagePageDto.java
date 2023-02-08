package gov.cabinetoffice.gap.adminbackend.dtos.pages;

import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AdvertSectionOverviewPagePageDto {

    private String id;

    private String title;

    private GrantAdvertPageResponseStatus status;

}
