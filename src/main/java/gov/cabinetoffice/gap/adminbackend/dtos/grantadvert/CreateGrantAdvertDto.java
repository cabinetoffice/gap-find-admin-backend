package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGrantAdvertDto {

    @NotNull(message = "A valid Scheme ID must be provided")
    @Positive(message = "A valid Scheme ID must be provided")
    private Integer grantSchemeId;

    @NotBlank(message = "Enter the name of your grant")
    @Size(max = 255, message = "Grant name cannot be greater than 255 characters")
    private String advertName;

}
