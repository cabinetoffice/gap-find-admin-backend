package gov.cabinetoffice.gap.adminbackend.dtos.pages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class AdvertPreviewTab {

    private String name;

    @Builder.Default
    private String content = "";

}
