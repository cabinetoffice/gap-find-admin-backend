package gov.cabinetoffice.gap.adminbackend.dtos.pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class AdvertPreviewPageDto {

    @Builder.Default
    private String grantName = "";

    @Builder.Default
    private String grantShortDescription = "";

    @Builder.Default
    private String grantApplicationOpenDate = "";

    @Builder.Default
    private String grantApplicationCloseDate = "";

    @Builder.Default
    private List<AdvertPreviewTab> tabs = List.of(AdvertPreviewTab.builder().name("Summary").build(),
            AdvertPreviewTab.builder().name("Eligibility").build(),
            AdvertPreviewTab.builder().name("Objectives").build(), AdvertPreviewTab.builder().name("Dates").build(),
            AdvertPreviewTab.builder().name("How to apply").build(),
            AdvertPreviewTab.builder().name("Supporting information").build());

}
