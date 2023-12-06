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
public class MasterSchemeStatusDto {

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Exists")
    private boolean exists;

}
