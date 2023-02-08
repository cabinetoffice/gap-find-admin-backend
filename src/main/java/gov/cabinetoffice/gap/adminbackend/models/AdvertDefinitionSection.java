package gov.cabinetoffice.gap.adminbackend.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class AdvertDefinitionSection {

    private String id;

    private String title;

    @Builder.Default
    private List<AdvertDefinitionPage> pages = new ArrayList<>();

    public Optional<AdvertDefinitionPage> getPageByIndex(int pageIndex) {

        try {
            return Optional.of(pages.get(pageIndex));
        }
        catch (IndexOutOfBoundsException iobe) {
            return Optional.empty();
        }
    }

    public Integer getIndexOfPage(AdvertDefinitionPage page) {
        return pages.indexOf(page);

    }

    public AdvertDefinitionPage getPageById(String pageId) {
        return pages.stream().filter(page -> Objects.equals(page.getId(), pageId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Page with id " + pageId + " does not exist"));

    }

}
