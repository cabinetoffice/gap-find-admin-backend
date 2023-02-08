package gov.cabinetoffice.gap.adminbackend.testdata;

import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSectionOverviewPageDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSectionOverviewPagePageDto;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSectionOverviewPageSectionDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSectionResponseStatus;
import gov.cabinetoffice.gap.adminbackend.models.*;

import java.util.List;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_SCHEME_NAME;

public class PagesAdvertControllerTestData {

    public final static Integer ADMIN_ID = 1;

    public final static String SCHEME_ID = "1";

    public final static UUID ADVERT_ID = UUID.fromString("129744d5-0746-403f-8a5f-a8c9558bc4e3");

    public final static String SECTION1_ID = "section1Id";

    public final static String SECTION1_TITLE = "section1Title";

    public final static String SECTION2_ID = "section2Id";

    public final static String SECTION2_TITLE = "section2Title";

    public final static GrantAdvertSectionResponseStatus SECTION_STATUS_NOT_STARTED = GrantAdvertSectionResponseStatus.NOT_STARTED;

    public final static GrantAdvertSectionResponseStatus SECTION_STATUS_COMPLETED = GrantAdvertSectionResponseStatus.COMPLETED;

    public final static String PAGE1_ID = "page1Id";

    public final static String PAGE1_TITLE = "page1Id";

    public final static String PAGE2_ID = "page2Id";

    public final static String PAGE2_TITLE = "page2Id";

    public final static String PAGE3_ID = "page3Id";

    public final static String PAGE3_TITLE = "page3Id";

    public final static String PAGE4_ID = "page4Id";

    public final static String PAGE4_TITLE = "page4Id";

    public final static String SECTION_OVERVIEW_ENDPOINT = "/pages/adverts/section-overview?";

    public final static GrantAdvertPageResponseStatus PAGE_STATUS = GrantAdvertPageResponseStatus.NOT_STARTED;

    public final static AdvertDefinitionPage ADVERT_DEFINITION_PAGE_1 = AdvertDefinitionPage.builder().id(PAGE1_ID)
            .title(PAGE1_TITLE).build();

    public final static AdvertDefinitionPage ADVERT_DEFINITION_PAGE_2 = AdvertDefinitionPage.builder().id(PAGE2_ID)
            .title(PAGE2_TITLE).build();

    public final static AdvertDefinitionPage ADVERT_DEFINITION_PAGE_3 = AdvertDefinitionPage.builder().id(PAGE3_ID)
            .title(PAGE3_TITLE).build();

    public final static AdvertDefinitionPage ADVERT_DEFINITION_PAGE_4 = AdvertDefinitionPage.builder().id(PAGE4_ID)
            .title(PAGE4_TITLE).build();

    public final static AdvertDefinitionSection ADVERT_DEFINITION_SECTION_1 = AdvertDefinitionSection.builder()
            .id(SECTION1_ID).title(SECTION1_TITLE).pages(List.of(ADVERT_DEFINITION_PAGE_1, ADVERT_DEFINITION_PAGE_2))
            .build();

    public final static AdvertDefinitionSection ADVERT_DEFINITION_SECTION_2 = AdvertDefinitionSection.builder()
            .id(SECTION2_ID).title(SECTION2_TITLE).pages(List.of(ADVERT_DEFINITION_PAGE_3, ADVERT_DEFINITION_PAGE_4))
            .build();

    public final static AdvertDefinition ADVERT_DEFINITION = AdvertDefinition.builder()
            .sections(List.of(ADVERT_DEFINITION_SECTION_1, ADVERT_DEFINITION_SECTION_2)).build();

    public final static GrantAdvertPageResponse ADVERT_RESPONSE_PAGE_1 = GrantAdvertPageResponse.builder().id(PAGE1_ID)
            .status(PAGE_STATUS).build();

    public final static GrantAdvertPageResponse ADVERT_RESPONSE_PAGE_2 = GrantAdvertPageResponse.builder().id(PAGE2_ID)
            .status(PAGE_STATUS).build();

    public final static GrantAdvertPageResponse ADVERT_RESPONSE_PAGE_3 = GrantAdvertPageResponse.builder().id(PAGE3_ID)
            .status(PAGE_STATUS).build();

    public final static GrantAdvertPageResponse ADVERT_RESPONSE_PAGE_4 = GrantAdvertPageResponse.builder().id(PAGE4_ID)
            .status(PAGE_STATUS).build();

    public final static GrantAdvertSectionResponse ADVERT_RESPONSE_SECTION_1 = GrantAdvertSectionResponse.builder()
            .id(SECTION1_ID).status(SECTION_STATUS_NOT_STARTED)
            .pages(List.of(ADVERT_RESPONSE_PAGE_1, ADVERT_RESPONSE_PAGE_2)).build();

    public final static GrantAdvertSectionResponse ADVERT_RESPONSE_SECTION_2 = GrantAdvertSectionResponse.builder()
            .id(SECTION2_ID).status(SECTION_STATUS_NOT_STARTED)
            .pages(List.of(ADVERT_RESPONSE_PAGE_3, ADVERT_RESPONSE_PAGE_4)).build();

    public final static GrantAdvertSectionResponse ADVERT_RESPONSE_SECTION_1_STATUS_COMPLETED = GrantAdvertSectionResponse
            .builder().id(SECTION1_ID).status(SECTION_STATUS_COMPLETED)
            .pages(List.of(ADVERT_RESPONSE_PAGE_1, ADVERT_RESPONSE_PAGE_2)).build();

    public final static GrantAdvertSectionResponse ADVERT_RESPONSE_SECTION_2_STATUS_COMPLETED = GrantAdvertSectionResponse
            .builder().id(SECTION2_ID).status(SECTION_STATUS_COMPLETED)
            .pages(List.of(ADVERT_RESPONSE_PAGE_3, ADVERT_RESPONSE_PAGE_4)).build();

    public final static GrantAdvertResponse ADVERT_RESPONSE = GrantAdvertResponse.builder()
            .sections(List.of(ADVERT_RESPONSE_SECTION_1, ADVERT_RESPONSE_SECTION_2)).build();

    public final static GrantAdvertResponse ADVERT_RESPONSE_SECTION_COMPLETED = GrantAdvertResponse.builder()
            .sections(List.of(ADVERT_RESPONSE_SECTION_1_STATUS_COMPLETED, ADVERT_RESPONSE_SECTION_2_STATUS_COMPLETED))
            .build();

    public final static GrantAdvert GRANT_ADVERT = GrantAdvert.builder().id(ADVERT_ID).response(ADVERT_RESPONSE)
            .build();

    public final static GrantAdvert GRANT_ADVERT_SECTION_COMPLETED = GrantAdvert.builder().id(ADVERT_ID)
            .response(ADVERT_RESPONSE_SECTION_COMPLETED).build();

    public final static AdvertSectionOverviewPagePageDto ADVERT_DTO_PAGE_1 = AdvertSectionOverviewPagePageDto.builder()
            .id(PAGE1_ID).title(PAGE1_TITLE).status(PAGE_STATUS).build();

    public final static AdvertSectionOverviewPagePageDto ADVERT_DTO_PAGE_2 = AdvertSectionOverviewPagePageDto.builder()
            .id(PAGE2_ID).title(PAGE2_TITLE).status(PAGE_STATUS).build();

    public final static AdvertSectionOverviewPagePageDto ADVERT_DTO_PAGE_3 = AdvertSectionOverviewPagePageDto.builder()
            .id(PAGE3_ID).title(PAGE3_TITLE).status(PAGE_STATUS).build();

    public final static AdvertSectionOverviewPagePageDto ADVERT_DTO_PAGE_4 = AdvertSectionOverviewPagePageDto.builder()
            .id(PAGE4_ID).title(PAGE4_TITLE).status(PAGE_STATUS).build();

    public final static AdvertSectionOverviewPageSectionDto ADVERT_DTO_SECTION_1 = AdvertSectionOverviewPageSectionDto
            .builder().id(SECTION1_ID).title(SECTION1_TITLE).status(SECTION_STATUS_NOT_STARTED)
            .pages(List.of(ADVERT_DTO_PAGE_1, ADVERT_DTO_PAGE_2)).build();

    public final static AdvertSectionOverviewPageSectionDto ADVERT_DTO_SECTION_2 = AdvertSectionOverviewPageSectionDto
            .builder().id(SECTION2_ID).title(SECTION2_TITLE).status(SECTION_STATUS_NOT_STARTED)
            .pages(List.of(ADVERT_DTO_PAGE_3, ADVERT_DTO_PAGE_4)).build();

    public final static AdvertSectionOverviewPageSectionDto ADVERT_DTO_SECTION_1_STATUS_COMPLETED = AdvertSectionOverviewPageSectionDto
            .builder().id(SECTION1_ID).title(SECTION1_TITLE).status(SECTION_STATUS_COMPLETED)
            .pages(List.of(ADVERT_DTO_PAGE_1, ADVERT_DTO_PAGE_2)).build();

    public final static AdvertSectionOverviewPageSectionDto ADVERT_DTO_SECTION_2_STATUS_COMPLETED = AdvertSectionOverviewPageSectionDto
            .builder().id(SECTION2_ID).title(SECTION2_TITLE).status(SECTION_STATUS_COMPLETED)
            .pages(List.of(ADVERT_DTO_PAGE_3, ADVERT_DTO_PAGE_4)).build();

    public final static AdvertSectionOverviewPageDTO EXPECTED_SECTION_OVERVIEW_CONTENT = AdvertSectionOverviewPageDTO
            .builder().grantSchemeName(SAMPLE_SCHEME_NAME).isPublishDisabled(true)
            .sections(List.of(ADVERT_DTO_SECTION_1, ADVERT_DTO_SECTION_2)).build();

    public final static AdvertSectionOverviewPageDTO EXPECTED_SECTION_OVERVIEW_CONTENT_IS_PUBLISH_DISABLE_FALSE = AdvertSectionOverviewPageDTO
            .builder().grantSchemeName(SAMPLE_SCHEME_NAME).isPublishDisabled(false)
            .sections(List.of(ADVERT_DTO_SECTION_1_STATUS_COMPLETED, ADVERT_DTO_SECTION_2_STATUS_COMPLETED)).build();

}
