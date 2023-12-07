package gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetSpotlightSubmissionManageDueDiligenceDataDto {

    private Long count;

    private String lastUpdatedDate;

}
