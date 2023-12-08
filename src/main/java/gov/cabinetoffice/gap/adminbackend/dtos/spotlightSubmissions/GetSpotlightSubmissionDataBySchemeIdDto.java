package gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetSpotlightSubmissionDataBySchemeIdDto {

    @Builder.Default
    private Long sentCount = 0L;

    @Builder.Default
    private String sentLastUpdatedDate = "";

    private boolean hasSpotlightSubmissions;

}
