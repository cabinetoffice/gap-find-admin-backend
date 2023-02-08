package gov.cabinetoffice.gap.adminbackend.dtos.pages;

import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSectionResponseStatus;
import gov.cabinetoffice.gap.adminbackend.models.AdvertDefinitionPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class AdvertSectionOverviewPageSectionDto {

    private String id;

    private String title;

    private GrantAdvertSectionResponseStatus status;

    @Builder.Default
    private List<AdvertSectionOverviewPagePageDto> pages = new ArrayList<>();

}