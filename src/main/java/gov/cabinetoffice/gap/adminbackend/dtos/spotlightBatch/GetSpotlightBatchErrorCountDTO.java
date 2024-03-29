package gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetSpotlightBatchErrorCountDTO {

    private String errorStatus;

    private int errorCount;

    private boolean errorFound;

    @JsonProperty("isValidationErrorPresent")
    private boolean isValidationErrorPresent;

}
