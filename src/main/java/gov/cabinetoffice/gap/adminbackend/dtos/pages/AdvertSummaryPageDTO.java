package gov.cabinetoffice.gap.adminbackend.dtos.pages;

import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvertSummaryPageDTO {

    private UUID id;

    private String advertName;

    private List<AdvertSummaryPageSectionDTO> sections;

    private GrantAdvertStatus status;

    private ZonedDateTime openingDate;
    private ZonedDateTime closingDate;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class AdvertSummaryPageSectionDTO {

        private String id;

        private String title;

        private List<AdvertSummaryPageSectionPageDTO> pages;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class AdvertSummaryPageSectionPageDTO {

        private String id;

        private String title;

        private List<AdvertSummaryPageQuestionDTO> questions;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class AdvertSummaryPageQuestionDTO {

        private String id;

        private String title;

        private String summarySuffixText;

        private String response;

        private String[] multiResponse;

        private AdvertDefinitionQuestionResponseType responseType;

    }

}
