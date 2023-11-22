package gov.cabinetoffice.gap.adminbackend.dtos.spotlight;

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

    private String GGISSchemeId;

    private List<DraftAssessmentDto> DraftAssessments;

}
