package gov.cabinetoffice.gap.adminbackend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CustomErrorMessage {

    private HttpStatus status;

    private ZonedDateTime date;

    private String message;

    private String description;

    private CustomErrorResponseBody body;

    private CustomErrorCode code;

}
