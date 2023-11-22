package gov.cabinetoffice.gap.adminbackend.dtos.spotlight;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpotlightSchemeDto {

    @JsonProperty("GGISSchemeId")
    private String ggisSchemeId;

    @JsonProperty("DraftAssessments")
    private List<DraftAssessmentDto> draftAssessments;

}
