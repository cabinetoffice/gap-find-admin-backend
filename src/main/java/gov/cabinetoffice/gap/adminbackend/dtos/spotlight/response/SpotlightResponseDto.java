package gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response;

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
public class SpotlightResponseDto {

    @JsonProperty("MasterSchemeStatus")
    private MasterSchemeStatusDto masterSchemeStatus;

    @JsonProperty("GGISSchemeID")
    private String ggisSchemeId;

    @JsonProperty("DraftAssessmentsResults")
    private List<DraftAssessmentResponseDto> draftAssessmentsResults;

}
