package gov.cabinetoffice.gap.adminbackend.dtos.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ApplicationDefinitionDTO {

    private List<ApplicationFormSectionDTO> sections;

    public ApplicationFormSectionDTO getSectionById(String sectionId) {
        List<ApplicationFormSectionDTO> applicationFormSectionDTOList = this.sections.stream()
                .filter((section) -> Objects.equals(section.getSectionId(), sectionId)).toList();

        if (applicationFormSectionDTOList.size() > 1) {
            throw new ApplicationFormException("Ambiguous reference - more than one section with id " + sectionId);
        }
        else if (applicationFormSectionDTOList.isEmpty()) {
            throw new NotFoundException("Section with id " + sectionId + " does not exist");
        }
        else {
            return applicationFormSectionDTOList.get(0);
        }
    }

}
