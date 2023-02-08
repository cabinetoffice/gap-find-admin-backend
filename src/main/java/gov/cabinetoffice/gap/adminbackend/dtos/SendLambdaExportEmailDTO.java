package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class SendLambdaExportEmailDTO {

    @Email(message = "The provided email is invalid")
    @NotNull(message = "Must provide an email")
    private String emailAddress;

    @NotNull(message = "Must provide an export id")
    private UUID exportId;

    @NotNull(message = "Must provide a submission id")
    private UUID submissionId;

    private Map<String, String> personalisation;

}
