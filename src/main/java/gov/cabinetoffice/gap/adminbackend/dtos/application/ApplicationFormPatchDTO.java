package gov.cabinetoffice.gap.adminbackend.dtos.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationFormPatchDTO {

    private ApplicationStatusEnum applicationStatus;

    private Boolean allowsMultipleSubmissions;

    public ApplicationFormPatchDTO(ApplicationStatusEnum applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public ApplicationFormPatchDTO(ApplicationStatusEnum applicationStatus, Boolean allowsMultipleSubmissions) {
        this.applicationStatus = applicationStatus;
        this.allowsMultipleSubmissions = allowsMultipleSubmissions;
    }

}
