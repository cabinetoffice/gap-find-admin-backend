package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;

import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.models.AdvertBuilderQuestionView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetGrantAdvertPageResponseDTO {

    private String sectionName;

    private String pageTitle;

    @Builder.Default
    private GrantAdvertPageResponseStatus status = GrantAdvertPageResponseStatus.NOT_STARTED;

    private List<AdvertBuilderQuestionView> questions;

    private String previousPageId;

    private String nextPageId;

}
