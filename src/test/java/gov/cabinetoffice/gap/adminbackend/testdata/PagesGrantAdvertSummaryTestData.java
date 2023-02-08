package gov.cabinetoffice.gap.adminbackend.testdata;

import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSummaryPageDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import gov.cabinetoffice.gap.adminbackend.models.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PagesGrantAdvertSummaryTestData {

    public static final UUID GRANT_ADVERT_ID = UUID.randomUUID();

    public static final String GRANT_SCHEME_ID = "1";

    public static final String GRANT_ADVERT_NAME = "Test Advert";

    public static final String ADVERT_DEFINITION_QUESTION_1_ID = "shortTextQuestion";

    public static final String ADVERT_DEFINITION_QUESTION_1_TITLE = "Short Text Question";

    public static final String ADVERT_DEFINITION_QUESTION_1_SUMMARY_TITLE = "Short Text Summary Text";

    public static final String ADVERT_DEFINITION_QUESTION_1_SUMMARY_SUFFIX = "Short Text Summary Suffix";

    public static final String ADVERT_DEFINITION_QUESTION_2_ID = "integerQuestion";

    public static final String ADVERT_DEFINITION_QUESTION_2_TITLE = "Integer Question";

    public static final String ADVERT_DEFINITION_QUESTION_2_SUMMARY_TITLE = "Integer Summary Text";

    public static final String ADVERT_DEFINITION_QUESTION_2_SUMMARY_SUFFIX = "Integer Summary Suffix";

    public static final String ADVERT_DEFINITION_QUESTION_3_ID = "currencyQuestion";

    public static final String ADVERT_DEFINITION_QUESTION_3_TITLE = "Currency Question";

    public static final String ADVERT_DEFINITION_QUESTION_3_SUMMARY_TITLE = "Currency Summary Text";

    public static final String ADVERT_DEFINITION_QUESTION_3_SUMMARY_SUFFIX = "Currency Summary Suffix";

    public static final String ADVERT_DEFINITION_QUESTION_4_ID = "richTextQuestion";

    public static final String ADVERT_DEFINITION_QUESTION_4_TITLE = "Rich Text Question";

    public static final String ADVERT_DEFINITION_QUESTION_4_SUMMARY_TITLE = "Rich Text Summary Text";

    public static final String ADVERT_DEFINITION_QUESTION_4_SUMMARY_SUFFIX = "Rich Text Summary Suffix";

    public static final String ADVERT_DEFINITION_QUESTION_5_ID = "dateTimeQuestion";

    public static final String ADVERT_DEFINITION_QUESTION_5_TITLE = "Date Time Question";

    public static final String ADVERT_DEFINITION_QUESTION_5_SUMMARY_TITLE = "Date Time Summary Text";

    public static final String ADVERT_DEFINITION_QUESTION_5_SUMMARY_SUFFIX = "Date Time Summary Suffix";

    public static final String ADVERT_DEFINITION_QUESTION_6_ID = "listQuestion";

    public static final String ADVERT_DEFINITION_QUESTION_6_TITLE = "List Question";

    public static final String ADVERT_DEFINITION_QUESTION_6_SUMMARY_TITLE = "List Summary Text";

    public static final String ADVERT_DEFINITION_QUESTION_6_SUMMARY_SUFFIX = "List Summary Suffix";

    public static final String ADVERT_DEFINITION_QUESTION_7_ID = "longTextQuestion";

    public static final String ADVERT_DEFINITION_QUESTION_7_TITLE = "List Question";

    public static final String ADVERT_DEFINITION_QUESTION_7_SUMMARY_TITLE = "Long Text Summary Text";

    public static final String ADVERT_DEFINITION_QUESTION_7_SUMMARY_SUFFIX = "Long Text Summary Suffix";

    public static final String ADVERT_DEFINITION_SECTION_1_PAGE_1_ID = "1";

    public static final String ADVERT_DEFINITION_SECTION_1_PAGE_1_TITLE = "Section 1 Page 1";

    public static final String ADVERT_DEFINITION_SECTION_1_PAGE_2_ID = "2";

    public static final String ADVERT_DEFINITION_SECTION_1_PAGE_2_TITLE = "Section 1 Page 2";

    public static final String ADVERT_DEFINITION_SECTION_1_PAGE_3_ID = "3";

    public static final String ADVERT_DEFINITION_SECTION_1_PAGE_3_TITLE = "Section 1 Page 3";

    public static final String ADVERT_DEFINITION_SECTION_2_PAGE_1_ID = "1";

    public static final String ADVERT_DEFINITION_SECTION_2_PAGE_1_TITLE = "Section 2 Page 1";

    public static final String ADVERT_DEFINITION_SECTION_1_ID = "section1";

    public static final String ADVERT_DEFINITION_SECTION_1_TITLE = "Section 1";

    public static final String ADVERT_DEFINITION_SECTION_2_ID = "section2";

    public static final String ADVERT_DEFINITION_SECTION_2_TITLE = "Section 2";

    public static final String QUESTION_1_RESPONSE = "Short Text Response";

    public static final String QUESTION_2_RESPONSE = "100";

    public static final String QUESTION_3_RESPONSE = "10000";

    public static final String QUESTION_3_FORMATTED_RESPONSE = "£10,000";

    public static final String QUESTION_4_RESPONSE = "Rich Text Response";

    public static final String[] QUESTION_5_RESPONSE = { "5", "11", "2022", "10", "00" };

    public static final String[] QUESTION_6_RESPONSE = { "List Response 1", "List Response 2" };

    public static final String QUESTION_7_RESPONSE = "Long Text Response";

    public static final AdvertDefinitionQuestion ADVERT_DEFINITION_QUESTION_1 = AdvertDefinitionQuestion.builder()
            .id(ADVERT_DEFINITION_QUESTION_1_ID).responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
            .title(ADVERT_DEFINITION_QUESTION_1_TITLE).summaryTitle(ADVERT_DEFINITION_QUESTION_1_SUMMARY_TITLE)
            .summarySuffixText(ADVERT_DEFINITION_QUESTION_1_SUMMARY_SUFFIX).build();

    public static final AdvertDefinitionQuestion ADVERT_DEFINITION_QUESTION_2 = AdvertDefinitionQuestion.builder()
            .id(ADVERT_DEFINITION_QUESTION_2_ID).responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .title(ADVERT_DEFINITION_QUESTION_2_TITLE).summaryTitle(ADVERT_DEFINITION_QUESTION_2_SUMMARY_TITLE)
            .summarySuffixText(ADVERT_DEFINITION_QUESTION_2_SUMMARY_SUFFIX).build();

    public static final AdvertDefinitionQuestion ADVERT_DEFINITION_QUESTION_3 = AdvertDefinitionQuestion.builder()
            .id(ADVERT_DEFINITION_QUESTION_3_ID).responseType(AdvertDefinitionQuestionResponseType.CURRENCY)
            .title(ADVERT_DEFINITION_QUESTION_3_TITLE).summaryTitle(ADVERT_DEFINITION_QUESTION_3_SUMMARY_TITLE)
            .summarySuffixText(ADVERT_DEFINITION_QUESTION_3_SUMMARY_SUFFIX).build();

    public static final AdvertDefinitionQuestion ADVERT_DEFINITION_QUESTION_4 = AdvertDefinitionQuestion.builder()
            .id(ADVERT_DEFINITION_QUESTION_4_ID).responseType(AdvertDefinitionQuestionResponseType.RICH_TEXT)
            .title(ADVERT_DEFINITION_QUESTION_4_TITLE).summaryTitle(ADVERT_DEFINITION_QUESTION_4_SUMMARY_TITLE)
            .summarySuffixText(ADVERT_DEFINITION_QUESTION_4_SUMMARY_SUFFIX).build();

    public static final AdvertDefinitionQuestion ADVERT_DEFINITION_QUESTION_5 = AdvertDefinitionQuestion.builder()
            .id(ADVERT_DEFINITION_QUESTION_5_ID).responseType(AdvertDefinitionQuestionResponseType.DATE)
            .title(ADVERT_DEFINITION_QUESTION_5_TITLE).summaryTitle(ADVERT_DEFINITION_QUESTION_5_SUMMARY_TITLE)
            .summarySuffixText(ADVERT_DEFINITION_QUESTION_5_SUMMARY_SUFFIX).build();

    public static final AdvertDefinitionQuestion ADVERT_DEFINITION_QUESTION_6 = AdvertDefinitionQuestion.builder()
            .id(ADVERT_DEFINITION_QUESTION_6_ID).responseType(AdvertDefinitionQuestionResponseType.LIST)
            .title(ADVERT_DEFINITION_QUESTION_6_TITLE).summaryTitle(ADVERT_DEFINITION_QUESTION_6_SUMMARY_TITLE)
            .summarySuffixText(ADVERT_DEFINITION_QUESTION_6_SUMMARY_SUFFIX).build();

    public static final AdvertDefinitionQuestion ADVERT_DEFINITION_QUESTION_7 = AdvertDefinitionQuestion.builder()
            .id(ADVERT_DEFINITION_QUESTION_7_ID).responseType(AdvertDefinitionQuestionResponseType.LONG_TEXT)
            .title(ADVERT_DEFINITION_QUESTION_7_TITLE).summaryTitle(ADVERT_DEFINITION_QUESTION_7_SUMMARY_TITLE)
            .summarySuffixText(ADVERT_DEFINITION_QUESTION_7_SUMMARY_SUFFIX).build();

    public static final AdvertDefinitionPage ADVERT_DEFINITION_SECTION_1_PAGE_1 = AdvertDefinitionPage.builder()
            .id(ADVERT_DEFINITION_SECTION_1_PAGE_1_ID).title(ADVERT_DEFINITION_SECTION_1_PAGE_1_TITLE)
            .questions(Arrays.asList(ADVERT_DEFINITION_QUESTION_1, ADVERT_DEFINITION_QUESTION_2)).build();

    public static final AdvertDefinitionPage ADVERT_DEFINITION_SECTION_1_PAGE_2 = AdvertDefinitionPage.builder()
            .id(ADVERT_DEFINITION_SECTION_1_PAGE_2_ID).title(ADVERT_DEFINITION_SECTION_1_PAGE_2_TITLE)
            .questions(Arrays.asList(ADVERT_DEFINITION_QUESTION_3)).build();

    public static final AdvertDefinitionPage ADVERT_DEFINITION_SECTION_1_PAGE_3 = AdvertDefinitionPage.builder()
            .id(ADVERT_DEFINITION_SECTION_1_PAGE_3_ID).title(ADVERT_DEFINITION_SECTION_1_PAGE_3_TITLE)
            .questions(Arrays.asList(ADVERT_DEFINITION_QUESTION_4)).build();

    public static final AdvertDefinitionPage ADVERT_DEFINITION_SECTION_2_PAGE_1 = AdvertDefinitionPage.builder()
            .id(ADVERT_DEFINITION_SECTION_2_PAGE_1_ID).title(ADVERT_DEFINITION_SECTION_2_PAGE_1_TITLE).questions(Arrays
                    .asList(ADVERT_DEFINITION_QUESTION_5, ADVERT_DEFINITION_QUESTION_6, ADVERT_DEFINITION_QUESTION_7))
            .build();

    public static final AdvertDefinitionSection ADVERT_DEFINITION_SECTION_1 = AdvertDefinitionSection.builder()
            .id(ADVERT_DEFINITION_SECTION_1_ID).title(ADVERT_DEFINITION_SECTION_1_TITLE)
            .pages(List.of(ADVERT_DEFINITION_SECTION_1_PAGE_1, ADVERT_DEFINITION_SECTION_1_PAGE_2,
                    ADVERT_DEFINITION_SECTION_1_PAGE_3))
            .build();

    public static final AdvertDefinitionSection ADVERT_DEFINITION_SECTION_2 = AdvertDefinitionSection.builder()
            .id(ADVERT_DEFINITION_SECTION_2_ID).title(ADVERT_DEFINITION_SECTION_2_TITLE)
            .pages(List.of(ADVERT_DEFINITION_SECTION_2_PAGE_1)).build();

    public static final GrantAdvert GRANT_ADVERT = GrantAdvert.builder().id(GRANT_ADVERT_ID)
            .grantAdvertName(GRANT_ADVERT_NAME).status(GrantAdvertStatus.DRAFT).build();

    public static GrantAdvert buildGrantAdvertWithResponses() {
        GrantAdvertQuestionResponse question1 = GrantAdvertQuestionResponse.builder()
                .id(ADVERT_DEFINITION_QUESTION_1_ID).response(QUESTION_1_RESPONSE).build();
        GrantAdvertQuestionResponse question2 = GrantAdvertQuestionResponse.builder()
                .id(ADVERT_DEFINITION_QUESTION_2_ID).response(QUESTION_2_RESPONSE).build();
        GrantAdvertQuestionResponse question3 = GrantAdvertQuestionResponse.builder()
                .id(ADVERT_DEFINITION_QUESTION_3_ID).response(QUESTION_3_RESPONSE).build();
        GrantAdvertQuestionResponse question4 = GrantAdvertQuestionResponse.builder()
                .id(ADVERT_DEFINITION_QUESTION_4_ID).response(QUESTION_4_RESPONSE).build();
        GrantAdvertQuestionResponse question5 = GrantAdvertQuestionResponse.builder()
                .id(ADVERT_DEFINITION_QUESTION_5_ID).multiResponse(QUESTION_5_RESPONSE).build();
        GrantAdvertQuestionResponse question6 = GrantAdvertQuestionResponse.builder()
                .id(ADVERT_DEFINITION_QUESTION_6_ID).multiResponse(QUESTION_6_RESPONSE).build();
        GrantAdvertQuestionResponse question7 = GrantAdvertQuestionResponse.builder()
                .id(ADVERT_DEFINITION_QUESTION_7_ID).response(QUESTION_7_RESPONSE).build();

        GrantAdvertPageResponse page1 = GrantAdvertPageResponse.builder().id(ADVERT_DEFINITION_SECTION_1_PAGE_1_ID)
                .questions(Arrays.asList(question1, question2)).build();
        GrantAdvertPageResponse page2 = GrantAdvertPageResponse.builder().id(ADVERT_DEFINITION_SECTION_1_PAGE_2_ID)
                .questions(Arrays.asList(question3)).build();
        GrantAdvertPageResponse page3 = GrantAdvertPageResponse.builder().id(ADVERT_DEFINITION_SECTION_1_PAGE_3_ID)
                .questions(Arrays.asList(question4)).build();
        GrantAdvertPageResponse page4 = GrantAdvertPageResponse.builder().id(ADVERT_DEFINITION_SECTION_2_PAGE_1_ID)
                .questions(Arrays.asList(question5, question6, question7)).build();

        GrantAdvertSectionResponse section1 = GrantAdvertSectionResponse.builder().id(ADVERT_DEFINITION_SECTION_1_ID)
                .pages(Arrays.asList(page1, page2, page3)).build();

        GrantAdvertSectionResponse section2 = GrantAdvertSectionResponse.builder().id(ADVERT_DEFINITION_SECTION_2_ID)
                .pages(Arrays.asList(page4)).build();

        GrantAdvertResponse grantAdvertResponse = GrantAdvertResponse.builder()
                .sections(Arrays.asList(section1, section2)).build();

        GrantAdvert grantAdvert = GrantAdvert.builder().id(GRANT_ADVERT_ID).grantAdvertName(GRANT_ADVERT_NAME)
                .response(grantAdvertResponse).status(GrantAdvertStatus.DRAFT).build();

        return grantAdvert;
    }

    public static AdvertSummaryPageDTO buildGrantAdvertSummaryPageDtoWithoutMergedData() {
        AdvertSummaryPageDTO advertSummaryPageDTO = AdvertSummaryPageDTO.builder().id(GRANT_ADVERT_ID)
                .advertName(GRANT_ADVERT_NAME).status(GrantAdvertStatus.DRAFT).build();

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO1 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO1.setId(ADVERT_DEFINITION_QUESTION_1_ID);
        questionDTO1.setTitle(ADVERT_DEFINITION_QUESTION_1_SUMMARY_TITLE);
        questionDTO1.setResponseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT);
        questionDTO1.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_1_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO2 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO2.setId(ADVERT_DEFINITION_QUESTION_2_ID);
        questionDTO2.setTitle(ADVERT_DEFINITION_QUESTION_2_SUMMARY_TITLE);
        questionDTO2.setResponseType(AdvertDefinitionQuestionResponseType.INTEGER);
        questionDTO2.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_2_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO3 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO3.setId(ADVERT_DEFINITION_QUESTION_3_ID);
        questionDTO3.setTitle(ADVERT_DEFINITION_QUESTION_3_SUMMARY_TITLE);
        questionDTO3.setResponseType(AdvertDefinitionQuestionResponseType.CURRENCY);
        questionDTO3.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_3_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO4 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO4.setId(ADVERT_DEFINITION_QUESTION_4_ID);
        questionDTO4.setTitle(ADVERT_DEFINITION_QUESTION_4_SUMMARY_TITLE);
        questionDTO4.setResponseType(AdvertDefinitionQuestionResponseType.RICH_TEXT);
        questionDTO4.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_4_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO5 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO5.setId(ADVERT_DEFINITION_QUESTION_5_ID);
        questionDTO5.setTitle(ADVERT_DEFINITION_QUESTION_5_SUMMARY_TITLE);
        questionDTO5.setResponseType(AdvertDefinitionQuestionResponseType.DATE);
        questionDTO5.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_5_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO6 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO6.setId(ADVERT_DEFINITION_QUESTION_6_ID);
        questionDTO6.setTitle(ADVERT_DEFINITION_QUESTION_6_SUMMARY_TITLE);
        questionDTO6.setResponseType(AdvertDefinitionQuestionResponseType.LIST);
        questionDTO6.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_6_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO7 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO7.setId(ADVERT_DEFINITION_QUESTION_7_ID);
        questionDTO7.setTitle(ADVERT_DEFINITION_QUESTION_7_SUMMARY_TITLE);
        questionDTO7.setResponseType(AdvertDefinitionQuestionResponseType.LONG_TEXT);
        questionDTO7.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_7_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO page1DTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
        page1DTO.setId(ADVERT_DEFINITION_SECTION_1_PAGE_1_ID);
        page1DTO.setTitle(ADVERT_DEFINITION_SECTION_1_PAGE_1_TITLE);
        page1DTO.setQuestions(Arrays.asList(questionDTO1, questionDTO2));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO page2DTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
        page2DTO.setId(ADVERT_DEFINITION_SECTION_1_PAGE_2_ID);
        page2DTO.setTitle(ADVERT_DEFINITION_SECTION_1_PAGE_2_TITLE);
        page2DTO.setQuestions(Arrays.asList(questionDTO3));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO page3DTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
        page3DTO.setId(ADVERT_DEFINITION_SECTION_1_PAGE_3_ID);
        page3DTO.setTitle(ADVERT_DEFINITION_SECTION_1_PAGE_3_TITLE);
        page3DTO.setQuestions(Arrays.asList(questionDTO4));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO page4DTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
        page4DTO.setId(ADVERT_DEFINITION_SECTION_2_PAGE_1_ID);
        page4DTO.setTitle(ADVERT_DEFINITION_SECTION_2_PAGE_1_TITLE);
        page4DTO.setQuestions(Arrays.asList(questionDTO5, questionDTO6, questionDTO7));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionDTO sectionDTO1 = advertSummaryPageDTO.new AdvertSummaryPageSectionDTO();
        sectionDTO1.setId(ADVERT_DEFINITION_SECTION_1_ID);
        sectionDTO1.setTitle(ADVERT_DEFINITION_SECTION_1_TITLE);
        sectionDTO1.setPages(Arrays.asList(page1DTO, page2DTO, page3DTO));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionDTO sectionDTO2 = advertSummaryPageDTO.new AdvertSummaryPageSectionDTO();
        sectionDTO2.setId(ADVERT_DEFINITION_SECTION_2_ID);
        sectionDTO2.setTitle(ADVERT_DEFINITION_SECTION_2_TITLE);
        sectionDTO2.setPages(Arrays.asList(page4DTO));

        advertSummaryPageDTO.setSections(Arrays.asList(sectionDTO1, sectionDTO2));

        return advertSummaryPageDTO;
    }

    public static AdvertSummaryPageDTO buildGrantAdvertSummaryPageDtoWithMergedData() {
        AdvertSummaryPageDTO advertSummaryPageDTO = AdvertSummaryPageDTO.builder().id(GRANT_ADVERT_ID)
                .advertName(GRANT_ADVERT_NAME).status(GrantAdvertStatus.DRAFT).build();

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO1 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO1.setId(ADVERT_DEFINITION_QUESTION_1_ID);
        questionDTO1.setTitle(ADVERT_DEFINITION_QUESTION_1_SUMMARY_TITLE);
        questionDTO1.setResponse(QUESTION_1_RESPONSE);
        questionDTO1.setResponseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT);
        questionDTO1.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_1_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO2 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO2.setId(ADVERT_DEFINITION_QUESTION_2_ID);
        questionDTO2.setTitle(ADVERT_DEFINITION_QUESTION_2_SUMMARY_TITLE);
        questionDTO2.setResponse(QUESTION_2_RESPONSE);
        questionDTO2.setResponseType(AdvertDefinitionQuestionResponseType.INTEGER);
        questionDTO2.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_2_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO3 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO3.setId(ADVERT_DEFINITION_QUESTION_3_ID);
        questionDTO3.setTitle(ADVERT_DEFINITION_QUESTION_3_SUMMARY_TITLE);
        questionDTO3.setResponse(QUESTION_3_FORMATTED_RESPONSE);
        questionDTO3.setResponseType(AdvertDefinitionQuestionResponseType.CURRENCY);
        questionDTO3.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_3_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO4 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO4.setId(ADVERT_DEFINITION_QUESTION_4_ID);
        questionDTO4.setTitle(ADVERT_DEFINITION_QUESTION_4_SUMMARY_TITLE);
        questionDTO4.setResponse(QUESTION_4_RESPONSE);
        questionDTO4.setResponseType(AdvertDefinitionQuestionResponseType.RICH_TEXT);
        questionDTO4.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_4_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO5 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO5.setId(ADVERT_DEFINITION_QUESTION_5_ID);
        questionDTO5.setTitle(ADVERT_DEFINITION_QUESTION_5_SUMMARY_TITLE);
        questionDTO5.setMultiResponse(QUESTION_5_RESPONSE);
        questionDTO5.setResponseType(AdvertDefinitionQuestionResponseType.DATE);
        questionDTO5.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_5_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO6 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO6.setId(ADVERT_DEFINITION_QUESTION_6_ID);
        questionDTO6.setTitle(ADVERT_DEFINITION_QUESTION_6_SUMMARY_TITLE);
        questionDTO6.setMultiResponse(QUESTION_6_RESPONSE);
        questionDTO6.setResponseType(AdvertDefinitionQuestionResponseType.LIST);
        questionDTO6.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_6_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO7 = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
        questionDTO7.setId(ADVERT_DEFINITION_QUESTION_7_ID);
        questionDTO7.setTitle(ADVERT_DEFINITION_QUESTION_7_SUMMARY_TITLE);
        questionDTO7.setResponse(QUESTION_7_RESPONSE);
        questionDTO7.setResponseType(AdvertDefinitionQuestionResponseType.LONG_TEXT);
        questionDTO7.setSummarySuffixText(ADVERT_DEFINITION_QUESTION_7_SUMMARY_SUFFIX);

        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO page1DTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
        page1DTO.setId(ADVERT_DEFINITION_SECTION_1_PAGE_1_ID);
        page1DTO.setTitle(ADVERT_DEFINITION_SECTION_1_PAGE_1_TITLE);
        page1DTO.setQuestions(Arrays.asList(questionDTO1, questionDTO2));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO page2DTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
        page2DTO.setId(ADVERT_DEFINITION_SECTION_1_PAGE_2_ID);
        page2DTO.setTitle(ADVERT_DEFINITION_SECTION_1_PAGE_2_TITLE);
        page2DTO.setQuestions(Arrays.asList(questionDTO3));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO page3DTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
        page3DTO.setId(ADVERT_DEFINITION_SECTION_1_PAGE_3_ID);
        page3DTO.setTitle(ADVERT_DEFINITION_SECTION_1_PAGE_3_TITLE);
        page3DTO.setQuestions(Arrays.asList(questionDTO4));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO page4DTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
        page4DTO.setId(ADVERT_DEFINITION_SECTION_2_PAGE_1_ID);
        page4DTO.setTitle(ADVERT_DEFINITION_SECTION_2_PAGE_1_TITLE);
        page4DTO.setQuestions(Arrays.asList(questionDTO5, questionDTO6, questionDTO7));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionDTO sectionDTO1 = advertSummaryPageDTO.new AdvertSummaryPageSectionDTO();
        sectionDTO1.setId(ADVERT_DEFINITION_SECTION_1_ID);
        sectionDTO1.setTitle(ADVERT_DEFINITION_SECTION_1_TITLE);
        sectionDTO1.setPages(Arrays.asList(page1DTO, page2DTO, page3DTO));

        AdvertSummaryPageDTO.AdvertSummaryPageSectionDTO sectionDTO2 = advertSummaryPageDTO.new AdvertSummaryPageSectionDTO();
        sectionDTO2.setId(ADVERT_DEFINITION_SECTION_2_ID);
        sectionDTO2.setTitle(ADVERT_DEFINITION_SECTION_2_TITLE);
        sectionDTO2.setPages(Arrays.asList(page4DTO));

        advertSummaryPageDTO.setSections(Arrays.asList(sectionDTO1, sectionDTO2));

        return advertSummaryPageDTO;
    }

}
