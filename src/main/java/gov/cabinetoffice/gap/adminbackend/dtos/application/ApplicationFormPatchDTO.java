package gov.cabinetoffice.gap.adminbackend.dtos.application;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationFormPatchDTO {

    @NotNull
    private ApplicationStatusEnum applicationStatus;

}
