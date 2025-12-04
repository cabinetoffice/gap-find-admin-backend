package gov.cabinetoffice.gap.adminbackend.dtos.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.validation.AtLeastOneFieldNotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = false)
@AtLeastOneFieldNotNull
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
