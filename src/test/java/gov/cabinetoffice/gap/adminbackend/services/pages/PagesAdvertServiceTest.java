package gov.cabinetoffice.gap.adminbackend.services.pages;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertPreviewPageDto;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSectionOverviewPageDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSectionOverviewPageSectionDto;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSummaryPageDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSectionResponseStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.models.*;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.services.SchemeService;
import gov.cabinetoffice.gap.adminbackend.testdata.PagesGrantAdvertSummaryTestData;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.testdata.PagesAdvertControllerTestData.*;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_SCHEME_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SCHEME_DTO_EXAMPLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class PagesAdvertServiceTest {

    @Mock
    private AdvertDefinition advertDefinition;

    @Mock
    private SchemeService schemeService;

    @Mock
    private GrantAdvertService grantAdvertService;

    @InjectMocks
    private PagesAdvertService serviceUnderTest;

    @Test
    void populateSectionsListForDto() {
        final List<AdvertSectionOverviewPageSectionDto> dtoSectionsList = new ArrayList<>();
        final List<AdvertDefinitionSection> statelessSection = ADVERT_DEFINITION.getSections();
        final List<GrantAdvertSectionResponse> sectionsWithStatus = GRANT_ADVERT.getResponse().getSections();

        serviceUnderTest.populateSectionsListForDto(dtoSectionsList, statelessSection, sectionsWithStatus);

        assertEquals(dtoSectionsList, EXPECTED_SECTION_OVERVIEW_CONTENT.getSections());
    }

    @Test
    void buildSectionOverviewPageContent__success() {
        when(this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(SCHEME_DTO_EXAMPLE);
        when(this.advertDefinition.getSections()).thenReturn(ADVERT_DEFINITION.getSections());
        when(this.grantAdvertService.getAdvertById(ADVERT_ID, false)).thenReturn(GRANT_ADVERT);

        AdvertSectionOverviewPageDTO result = serviceUnderTest
                .buildSectionOverviewPageContent(String.valueOf(SAMPLE_SCHEME_ID), ADVERT_ID);

        assertEquals(EXPECTED_SECTION_OVERVIEW_CONTENT, result);
    }

    @Test
    void buildSectionOverviewPageContent__IsPublishDisableFalse() throws Exception {
        when(this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(SCHEME_DTO_EXAMPLE);
        when(this.advertDefinition.getSections()).thenReturn(ADVERT_DEFINITION.getSections());
        when(this.grantAdvertService.getAdvertById(ADVERT_ID, false)).thenReturn(GRANT_ADVERT_SECTION_COMPLETED);

        AdvertSectionOverviewPageDTO result = serviceUnderTest
                .buildSectionOverviewPageContent(String.valueOf(SAMPLE_SCHEME_ID), ADVERT_ID);

        assertEquals(EXPECTED_SECTION_OVERVIEW_CONTENT_IS_PUBLISH_DISABLE_FALSE, result);
    }

    @Test
    void buildSectionOverviewPageContent__GrantAdvertGetByIdReturnEmpty() throws Exception {
        when(this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(SCHEME_DTO_EXAMPLE);
        when(this.advertDefinition.getSections()).thenReturn(ADVERT_DEFINITION.getSections());
        when(this.grantAdvertService.getAdvertById(ADVERT_ID, false))
                .thenThrow(new NotFoundException("Advert with id " + ADVERT_ID + " not found"));

        assertThatThrownBy(
                () -> serviceUnderTest.buildSectionOverviewPageContent(String.valueOf(SAMPLE_SCHEME_ID), ADVERT_ID))
                        .isInstanceOf(NotFoundException.class).hasMessage("Advert with id " + ADVERT_ID + " not found");

    }

    @Test
    void buildSectionOverviewPageContent__BeanException() throws Exception {
        when(this.advertDefinition.getSections()).thenThrow(new BeanCreationException("error"));
        when(this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(SCHEME_DTO_EXAMPLE);
        when(this.grantAdvertService.getAdvertById(ADVERT_ID, false)).thenReturn(GRANT_ADVERT);

        assertThrows(BeanCreationException.class,
                () -> serviceUnderTest.buildSectionOverviewPageContent("1", ADVERT_ID));
    }

    @Nested
    class buildSummaryPage {

        @Test
        void success_emptyGrantAdvert() {
            AdvertDefinitionSection advertDefinitionSection1 = PagesGrantAdvertSummaryTestData.ADVERT_DEFINITION_SECTION_1;
            AdvertDefinitionSection advertDefinitionSection2 = PagesGrantAdvertSummaryTestData.ADVERT_DEFINITION_SECTION_2;
            UUID grantAdvertId = PagesGrantAdvertSummaryTestData.GRANT_ADVERT_ID;
            GrantAdvert grantAdvert = PagesGrantAdvertSummaryTestData.GRANT_ADVERT;
            String grantSchemeId = PagesGrantAdvertSummaryTestData.GRANT_SCHEME_ID;

            AdvertSummaryPageDTO expectedDto = PagesGrantAdvertSummaryTestData
                    .buildGrantAdvertSummaryPageDtoWithoutMergedData();

            when(advertDefinition.getSections())
                    .thenReturn(Arrays.asList(advertDefinitionSection1, advertDefinitionSection2));
            when(grantAdvertService.getAdvertById(grantAdvertId, false)).thenReturn(grantAdvert);

            AdvertSummaryPageDTO result = serviceUnderTest.buildSummaryPageContent(grantSchemeId, grantAdvertId);

            assertEquals(expectedDto, result);
        }

        @Test
        void success_fullGrantAdvert() {
            AdvertDefinitionSection advertDefinitionSection1 = PagesGrantAdvertSummaryTestData.ADVERT_DEFINITION_SECTION_1;
            AdvertDefinitionSection advertDefinitionSection2 = PagesGrantAdvertSummaryTestData.ADVERT_DEFINITION_SECTION_2;
            UUID grantAdvertId = PagesGrantAdvertSummaryTestData.GRANT_ADVERT_ID;
            GrantAdvert grantAdvert = PagesGrantAdvertSummaryTestData.buildGrantAdvertWithResponses();
            String grantSchemeId = PagesGrantAdvertSummaryTestData.GRANT_SCHEME_ID;

            AdvertSummaryPageDTO expectedDto = PagesGrantAdvertSummaryTestData
                    .buildGrantAdvertSummaryPageDtoWithMergedData();

            when(advertDefinition.getSections())
                    .thenReturn(Arrays.asList(advertDefinitionSection1, advertDefinitionSection2));
            when(grantAdvertService.getAdvertById(grantAdvertId, false)).thenReturn(grantAdvert);

            AdvertSummaryPageDTO result = serviceUnderTest.buildSummaryPageContent(grantSchemeId, grantAdvertId);

            assertEquals(expectedDto, result);

        }

    }

    @Nested
    class buildAdvertPreview {

        UUID grantAdvertId = UUID.randomUUID();

        final String grantAdvertName = "Test Advert Name";

        final String grantShortDescription = "A government grant to provide funding for homelessness charities";

        final String richTextTemplate = "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"%s\",\"marks\":[],\"data\":{}}],\"data\":{}}]}";

        final GrantAdvertQuestionResponse shortDescriptionResponse = GrantAdvertQuestionResponse.builder()
                .id("grantShortDescription").seen(true).response(grantShortDescription).build();

        final GrantAdvertPageResponse page1 = GrantAdvertPageResponse.builder().id("1")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(shortDescriptionResponse)).build();

        final GrantAdvertSectionResponse responseGrantDetails = GrantAdvertSectionResponse.builder().id("grantDetails")
                .status(GrantAdvertSectionResponseStatus.COMPLETED).pages(List.of(page1)).build();

        final GrantAdvertQuestionResponse openingDateResponse = GrantAdvertQuestionResponse.builder()
                .id("grantApplicationOpenDate").seen(true)
                .multiResponse(new String[] { "10", "12", "2022", "00", "01" }).build();

        final GrantAdvertQuestionResponse closingDateResponse = GrantAdvertQuestionResponse.builder()
                .id("grantApplicationCloseDate").seen(true)
                .multiResponse(new String[] { "10", "12", "2023", "23", "59" }).build();

        final GrantAdvertPageResponse datesPage = GrantAdvertPageResponse.builder().id("1")
                .status(GrantAdvertPageResponseStatus.COMPLETED)
                .questions(List.of(openingDateResponse, closingDateResponse)).build();

        final GrantAdvertSectionResponse responseSectionDates = GrantAdvertSectionResponse.builder()
                .id("applicationDates").status(GrantAdvertSectionResponseStatus.COMPLETED).pages(List.of(datesPage))
                .build();

        final GrantAdvertQuestionResponse eligibilityResponse = GrantAdvertQuestionResponse.builder()
                .id("grantEligibilityTab").seen(true)
                .multiResponse(new String[] { "", String.format(richTextTemplate, "eligibility") }).build();

        final GrantAdvertPageResponse eligibilityPage = GrantAdvertPageResponse.builder().id("1")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(eligibilityResponse)).build();

        final GrantAdvertQuestionResponse summaryResponse = GrantAdvertQuestionResponse.builder().id("grantSummaryTab")
                .seen(true).multiResponse(new String[] { "", String.format(richTextTemplate, "summary") }).build();

        final GrantAdvertPageResponse summaryPage = GrantAdvertPageResponse.builder().id("2")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(summaryResponse)).build();

        final GrantAdvertQuestionResponse datesInfoResponse = GrantAdvertQuestionResponse.builder().id("grantDatesTab")
                .seen(true).multiResponse(new String[] { "", String.format(richTextTemplate, "dates") }).build();

        final GrantAdvertPageResponse datesInfoPage = GrantAdvertPageResponse.builder().id("3")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(datesInfoResponse)).build();

        final GrantAdvertQuestionResponse objectivesResponse = GrantAdvertQuestionResponse.builder()
                .id("grantObjectivesTab").seen(true)
                .multiResponse(new String[] { "", String.format(richTextTemplate, "objectives") }).build();

        final GrantAdvertPageResponse objectivesPage = GrantAdvertPageResponse.builder().id("4")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(objectivesResponse)).build();

        final GrantAdvertQuestionResponse applyResponse = GrantAdvertQuestionResponse.builder().id("grantApplyTab")
                .seen(true).multiResponse(new String[] { "", String.format(richTextTemplate, "howToApply") }).build();

        final GrantAdvertPageResponse applyPage = GrantAdvertPageResponse.builder().id("5")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(applyResponse)).build();

        final GrantAdvertQuestionResponse supportingInfoResponse = GrantAdvertQuestionResponse.builder()
                .id("grantSupportingInfoTab").seen(true)
                .multiResponse(new String[] { "", String.format(richTextTemplate, "supportingInformation") }).build();

        final GrantAdvertPageResponse supportingInfoPage = GrantAdvertPageResponse.builder().id("6")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(supportingInfoResponse)).build();

        final GrantAdvertSectionResponse responseSectionFurtherInfo = GrantAdvertSectionResponse.builder()
                .id("furtherInformation").status(GrantAdvertSectionResponseStatus.COMPLETED).pages(List
                        .of(eligibilityPage, summaryPage, datesInfoPage, objectivesPage, applyPage, supportingInfoPage))
                .build();

        final GrantAdvertResponse response = GrantAdvertResponse.builder()
                .sections(List.of(responseGrantDetails, responseSectionDates, responseSectionFurtherInfo)).build();

        @Test
        void buildAdvertPreview_SuccessFullAdvert() {
            GrantAdvert grantAdvert = GrantAdvert.builder().grantAdvertName(grantAdvertName).id(grantAdvertId)
                    .response(response).build();

            when(grantAdvertService.getAdvertById(grantAdvertId, false)).thenReturn(grantAdvert);

            AdvertPreviewPageDto advertPreviewPageDto = serviceUnderTest.buildAdvertPreview(grantAdvertId);

            assertThat(advertPreviewPageDto.getGrantName()).isEqualTo(grantAdvertName);
            assertThat(advertPreviewPageDto.getGrantShortDescription()).isEqualTo(grantShortDescription);
            assertThat(advertPreviewPageDto.getGrantApplicationOpenDate()).isEqualTo("10 December 2022, 12:01am");
            assertThat(advertPreviewPageDto.getGrantApplicationCloseDate()).isEqualTo("10 December 2023, 11:59pm");
            assertThat(advertPreviewPageDto.getTabs().get(0).getContent())
                    .isEqualTo(String.format(richTextTemplate, "summary"));
            assertThat(advertPreviewPageDto.getTabs().get(1).getContent())
                    .isEqualTo(String.format(richTextTemplate, "eligibility"));
            assertThat(advertPreviewPageDto.getTabs().get(2).getContent())
                    .isEqualTo(String.format(richTextTemplate, "objectives"));
            assertThat(advertPreviewPageDto.getTabs().get(3).getContent())
                    .isEqualTo(String.format(richTextTemplate, "dates"));
            assertThat(advertPreviewPageDto.getTabs().get(4).getContent())
                    .isEqualTo(String.format(richTextTemplate, "howToApply"));
            assertThat(advertPreviewPageDto.getTabs().get(5).getContent())
                    .isEqualTo(String.format(richTextTemplate, "supportingInformation"));
        }

        @Test
        void buildAdvertPreview_SuccessEmptyAdvert() {
            GrantAdvert grantAdvert = GrantAdvert.builder().grantAdvertName(grantAdvertName).id(grantAdvertId).build();

            when(grantAdvertService.getAdvertById(grantAdvertId, false)).thenReturn(grantAdvert);

            AdvertPreviewPageDto advertPreviewPageDto = serviceUnderTest.buildAdvertPreview(grantAdvertId);

            assertThat(advertPreviewPageDto.getGrantName()).isEqualTo(grantAdvertName);
            assertThat(advertPreviewPageDto.getGrantShortDescription()).isEmpty();
            assertThat(advertPreviewPageDto.getGrantApplicationOpenDate()).isEmpty();
            assertThat(advertPreviewPageDto.getGrantApplicationCloseDate()).isEmpty();
            assertThat(advertPreviewPageDto.getTabs().get(0).getContent()).isEmpty(); // summary
            assertThat(advertPreviewPageDto.getTabs().get(1).getContent()).isEmpty(); // eligibility
            assertThat(advertPreviewPageDto.getTabs().get(2).getContent()).isEmpty(); // objectives
            assertThat(advertPreviewPageDto.getTabs().get(3).getContent()).isEmpty(); // dates
            assertThat(advertPreviewPageDto.getTabs().get(4).getContent()).isEmpty(); // howToApply
            assertThat(advertPreviewPageDto.getTabs().get(5).getContent()).isEmpty(); // supportingInfo
        }

        @Test
        void buildAdvertPreview_SuccessPartialAdvert() {
            final GrantAdvertSectionResponse responseSectionFurtherInfo = GrantAdvertSectionResponse.builder()
                    .id("furtherInformation").status(GrantAdvertSectionResponseStatus.CHANGED)
                    .pages(List.of(eligibilityPage, summaryPage, datesInfoPage)).build();

            // no dates, partially filled in further info
            final GrantAdvertResponse response = GrantAdvertResponse.builder()
                    .sections(List.of(responseGrantDetails, responseSectionFurtherInfo)).build();

            GrantAdvert grantAdvert = GrantAdvert.builder().grantAdvertName(grantAdvertName).id(grantAdvertId)
                    .response(response).build();

            when(grantAdvertService.getAdvertById(grantAdvertId, false)).thenReturn(grantAdvert);

            AdvertPreviewPageDto advertPreviewPageDto = serviceUnderTest.buildAdvertPreview(grantAdvertId);

            assertThat(advertPreviewPageDto.getGrantName()).isEqualTo(grantAdvertName);
            assertThat(advertPreviewPageDto.getGrantShortDescription()).isEqualTo(grantShortDescription);
            assertThat(advertPreviewPageDto.getTabs().get(0).getContent())
                    .isEqualTo(String.format(richTextTemplate, "summary"));
            assertThat(advertPreviewPageDto.getTabs().get(1).getContent())
                    .isEqualTo(String.format(richTextTemplate, "eligibility"));
            assertThat(advertPreviewPageDto.getTabs().get(3).getContent())
                    .isEqualTo(String.format(richTextTemplate, "dates"));

            // unanswered questions
            assertThat(advertPreviewPageDto.getGrantApplicationOpenDate()).isEmpty();
            assertThat(advertPreviewPageDto.getGrantApplicationCloseDate()).isEmpty();
            assertThat(advertPreviewPageDto.getTabs().get(2).getContent()).isEmpty();
            assertThat(advertPreviewPageDto.getTabs().get(4).getContent()).isEmpty();
            assertThat(advertPreviewPageDto.getTabs().get(5).getContent()).isEmpty();
        }

        @Test
        void buildAdvertPreview_AdvertNotFound() {

            when(grantAdvertService.getAdvertById(grantAdvertId, false)).thenThrow(NotFoundException.class);

            assertThatThrownBy(() -> serviceUnderTest.buildAdvertPreview(grantAdvertId))
                    .isInstanceOf(NotFoundException.class);

        }

    }

}