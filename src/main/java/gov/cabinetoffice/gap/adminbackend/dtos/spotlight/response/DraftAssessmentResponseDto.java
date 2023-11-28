package gov.cabinetoffice.gap.adminbackend.dtos.spotlight.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DraftAssessmentResponseDto {

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("ApplicationNumber")
    private String applicationNumber;

}
