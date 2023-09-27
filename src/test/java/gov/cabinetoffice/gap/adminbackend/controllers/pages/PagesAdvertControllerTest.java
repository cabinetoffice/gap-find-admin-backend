package gov.cabinetoffice.gap.adminbackend.controllers.pages;

import gov.cabinetoffice.gap.adminbackend.controllers.ControllerExceptionHandler;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPageResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertPreviewPageDto;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertPreviewTab;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSummaryPageDTO;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.services.pages.PagesAdvertService;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomGrantAdvertGenerators;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.testdata.PagesAdvertControllerTestData.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagesAdvertController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { PagesAdvertController.class, PagesControllerExceptionsHandler.class,
        ControllerExceptionHandler.class })
class PagesAdvertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PagesAdvertService pagesAdvertService;

    @MockBean
    private GrantAdvertService grantAdvertService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Test
    void testGetSectionOverviewContent__Success() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("schemeId", SCHEME_ID);
        params.add("advertId", ADVERT_ID.toString());

        when(this.pagesAdvertService.buildSectionOverviewPageContent(SCHEME_ID, ADVERT_ID))
                .thenReturn(EXPECTED_SECTION_OVERVIEW_CONTENT);

        this.mockMvc.perform(get(SECTION_OVERVIEW_ENDPOINT).params(params)).andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(EXPECTED_SECTION_OVERVIEW_CONTENT)));
    }

    @Test
    void testGetSectionOverviewContent__badRequest_MissingRequestParams() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("advertId", ADVERT_ID.toString());

        mockMvc.perform(get(SECTION_OVERVIEW_ENDPOINT).params(params)).andExpect(status().isBadRequest());
    }

    @Test
    void testGetSectionOverviewContent__accessDenied_AttemptingToAccessAdvertCreatedByAnotherAdmin() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("schemeId", SCHEME_ID);
        params.add("advertId", ADVERT_ID.toString());

        when(pagesAdvertService.buildSectionOverviewPageContent(SCHEME_ID, ADVERT_ID))
                .thenThrow(new AccessDeniedException("Access Denied"));

        mockMvc.perform(get(SECTION_OVERVIEW_ENDPOINT).params(params)).andExpect(status().isForbidden());
    }

    @Test
    void testGetSectionOverviewContent__notFound_AttemptingToAccessAdvertWhichDoesNotExist() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("schemeId", SCHEME_ID);
        params.add("advertId", ADVERT_ID.toString());

        when(pagesAdvertService.buildSectionOverviewPageContent(SCHEME_ID, ADVERT_ID))
                .thenThrow(new NotFoundException());

        mockMvc.perform(get(SECTION_OVERVIEW_ENDPOINT).params(params)).andExpect(status().isNotFound());
    }

    @Nested
    class getQuestionsResponsePage {

        @Test
        void successfulRequest_ReturningAllViewDataForAdvertBuilderPages() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "mock-section-id";
            final String pageId = "mock-page-id";

            final GetGrantAdvertPageResponseDTO viewDataDTO = RandomGrantAdvertGenerators
                    .randomAdvertBuilderPageResponse().build();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("advertSectionId", sectionId);
            params.add("advertPageId", pageId);

            when(grantAdvertService.getAdvertBuilderPageData(grantAdvertId, sectionId, pageId)).thenReturn(viewDataDTO);

            mockMvc.perform(get("/pages/adverts/" + grantAdvertId + "/questions-page").params(params))
                    .andExpect(status().isOk()).andExpect(content().json(HelperUtils.asJsonString(viewDataDTO)));
        }

        @Test
        void badRequest_MissingRequestParams() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();

            mockMvc.perform(get("/pages/adverts/" + grantAdvertId + "/questions-page"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void accessDenied_AttemptingToAccessAdvertCreatedByAnotherAdmin() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "mock-section-id";
            final String pageId = "mock-page-id";

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("advertSectionId", sectionId);
            params.add("advertPageId", pageId);

            when(grantAdvertService.getAdvertBuilderPageData(grantAdvertId, sectionId, pageId))
                    .thenThrow(new AccessDeniedException("Access Denied"));

            mockMvc.perform(get("/pages/adverts/" + grantAdvertId + "/questions-page").params(params))
                    .andExpect(status().isForbidden());
        }

        @Test
        void notFound_AttemptingToAccessAdvertWhichDoesNotExist() throws Exception {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "mock-section-id";
            final String pageId = "mock-page-id";

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("advertSectionId", sectionId);
            params.add("advertPageId", pageId);

            when(grantAdvertService.getAdvertBuilderPageData(grantAdvertId, sectionId, pageId))
                    .thenThrow(new NotFoundException());

            mockMvc.perform(get("/pages/adverts/" + grantAdvertId + "/questions-page").params(params))
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    class getAdvertSummaryPage {

        final String endpoint = "/pages/adverts/summary";

        final UUID grantAdvertId = UUID.randomUUID();

        final String schemeId = "1";

        final String advertName = "Advert Name";

        final String sectionId = "mock-section-id";

        final String sectionTitle = "Mock Section Title";

        final String pageId = "mock-page-id";

        final String pageTitle = "Mock Page Title";

        final String questionId = "mock-question-id";

        final String questionTitle = "Mock Question Title";

        final String questionResponse = "Mock Question Response";

        final String[] questionMultiResponse = { "MultiResponse 1", "MultiResponse 2" };

        final String questionSummarySuffixTest = "Mock Summary Suffix Text";

        @Test
        void success() throws Exception {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
            params.add("schemeId", schemeId);
            params.add("advertId", grantAdvertId.toString());

            AdvertSummaryPageDTO expectedDto = AdvertSummaryPageDTO.builder().id(grantAdvertId).advertName(advertName)
                    .build();

            AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO = expectedDto.new AdvertSummaryPageQuestionDTO();
            questionDTO.setId(questionId);
            questionDTO.setTitle(questionTitle);
            questionDTO.setResponse(questionResponse);
            questionDTO.setMultiResponse(questionMultiResponse);
            questionDTO.setSummarySuffixText(questionSummarySuffixTest);
            questionDTO.setResponseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT);

            AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO pageDTO = expectedDto.new AdvertSummaryPageSectionPageDTO();
            pageDTO.setId(pageId);
            pageDTO.setTitle(pageTitle);
            pageDTO.setQuestions(Arrays.asList(questionDTO));

            AdvertSummaryPageDTO.AdvertSummaryPageSectionDTO sectionDTO = expectedDto.new AdvertSummaryPageSectionDTO();
            sectionDTO.setId(sectionId);
            sectionDTO.setTitle(sectionTitle);
            sectionDTO.setPages(Arrays.asList(pageDTO));

            expectedDto.setSections(Arrays.asList(sectionDTO));
            expectedDto.setStatus(GrantAdvertStatus.DRAFT);

            when(pagesAdvertService.buildSummaryPageContent(schemeId, grantAdvertId)).thenReturn(expectedDto);

            mockMvc.perform(get(endpoint).params(params)).andExpect(status().isOk())
                    .andExpect(content().json(HelperUtils.asJsonString(expectedDto)));
        }

        @Test
        void badRequest_MissingRequestParams() throws Exception {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
            params.add("advertId", grantAdvertId.toString());

            mockMvc.perform(get(endpoint).params(params)).andExpect(status().isBadRequest());
        }

        @Test
        void accessDenied_AttemptingToAccessAdvertCreatedByAnotherAdmin() throws Exception {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
            params.add("schemeId", schemeId);
            params.add("advertId", grantAdvertId.toString());

            when(pagesAdvertService.buildSummaryPageContent(schemeId, grantAdvertId))
                    .thenThrow(new AccessDeniedException("Access Denied"));

            mockMvc.perform(get(endpoint).params(params)).andExpect(status().isForbidden());
        }

        @Test
        void testGetSectionOverviewContent__notFound_AttemptingToAccessAdvertWhichDoesNotExist() throws Exception {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
            params.add("schemeId", schemeId);
            params.add("advertId", grantAdvertId.toString());

            when(pagesAdvertService.buildSummaryPageContent(schemeId, grantAdvertId))
                    .thenThrow(new NotFoundException());

            mockMvc.perform(get(endpoint).params(params)).andExpect(status().isNotFound());
        }

    }

    @Nested
    class buildAdvertPreview {

        final UUID grantAdvertId = UUID.randomUUID();

        final String grantAdvertName = "Test Advert Name";

        final String grantShortDescription = "A government grant to provide funding for homelessness charities";

        final String openingDate = "10 December 2022, 12:01am";

        final String closingDate = "10 December 2023, 11:59pm";

        final String richTextTemplate = "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"%s\",\"marks\":[],\"data\":{}}],\"data\":{}}]}";

        List<AdvertPreviewTab> advertPreviewTabs = List.of(
                AdvertPreviewTab.builder().name("Summary").content(String.format(richTextTemplate, "summary")).build(),
                AdvertPreviewTab.builder().name("Eligibility").content(String.format(richTextTemplate, "eligibility"))
                        .build(),
                AdvertPreviewTab.builder().name("Objectives").content(String.format(richTextTemplate, "objectives"))
                        .build(),
                AdvertPreviewTab.builder().name("Dates").content(String.format(richTextTemplate, "dates")).build(),
                AdvertPreviewTab.builder().name("How to apply").content(String.format(richTextTemplate, "howToApply"))
                        .build(),
                AdvertPreviewTab.builder().name("Supporting information")
                        .content(String.format(richTextTemplate, "supportingInformation")).build());

        AdvertPreviewPageDto advertPreviewPageDto = AdvertPreviewPageDto.builder().grantName(grantAdvertName)
                .grantShortDescription(grantShortDescription).grantApplicationOpenDate(openingDate)
                .grantApplicationCloseDate(closingDate).tabs(advertPreviewTabs).build();

        @Test
        void buildAdvertPreview_Success() throws Exception {

            when(pagesAdvertService.buildAdvertPreview(grantAdvertId)).thenReturn(advertPreviewPageDto);

            mockMvc.perform(get(String.format("/pages/adverts/%s/preview", grantAdvertId))).andExpect(status().isOk())
                    .andExpect(content().json(HelperUtils.asJsonString(advertPreviewPageDto)));
        }

        @Test
        void buildAdvertPreview_NotFound() throws Exception {

            when(pagesAdvertService.buildAdvertPreview(grantAdvertId)).thenThrow(NotFoundException.class);

            mockMvc.perform(get(String.format("/pages/adverts/%s/preview", grantAdvertId)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void buildAdvertPreview_Forbidden() throws Exception {

            when(pagesAdvertService.buildAdvertPreview(grantAdvertId)).thenThrow(AccessDeniedException.class);

            mockMvc.perform(get(String.format("/pages/adverts/%s/preview", grantAdvertId)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void buildAdvertPreview_GenericException() throws Exception {

            when(pagesAdvertService.buildAdvertPreview(grantAdvertId)).thenThrow(RuntimeException.class);

            mockMvc.perform(get(String.format("/pages/adverts/%s/preview", grantAdvertId)))
                    .andExpect(status().isInternalServerError());
        }

    }

}
