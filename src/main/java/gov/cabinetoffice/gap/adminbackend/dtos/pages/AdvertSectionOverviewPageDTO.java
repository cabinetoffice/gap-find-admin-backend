package gov.cabinetoffice.gap.adminbackend.dtos.pages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class AdvertSectionOverviewPageDTO {

    private String grantSchemeName;

    private String advertName;

    @JsonProperty("isPublishDisabled")
    private boolean isPublishDisabled;

    private List<AdvertSectionOverviewPageSectionDto> sections;

}
