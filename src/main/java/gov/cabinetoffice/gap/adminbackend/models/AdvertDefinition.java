package gov.cabinetoffice.gap.adminbackend.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class AdvertDefinition {

    @Builder.Default
    private List<AdvertDefinitionSection> sections = new ArrayList<>();

    public AdvertDefinitionSection getSectionById(String sectionId) {
        return sections.stream().filter(section -> Objects.equals(section.getId(), sectionId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Section with id " + sectionId + " does not exist"));
    }

}
