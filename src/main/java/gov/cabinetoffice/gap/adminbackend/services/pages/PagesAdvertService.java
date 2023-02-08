package gov.cabinetoffice.gap.adminbackend.services.pages;

import gov.cabinetoffice.gap.adminbackend.dtos.pages.*;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSectionResponseStatus;
import gov.cabinetoffice.gap.adminbackend.models.*;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.services.SchemeService;
import gov.cabinetoffice.gap.adminbackend.utils.CurrencyFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.*;
import static java.lang.Integer.parseInt;

@Service
@Log4j2
@RequiredArgsConstructor
public class PagesAdvertService {

    private final AdvertDefinition definition;

    private final SchemeService schemeService;

    private final GrantAdvertService grantAdvertService;

    public static final String FURTHER_INFORMATION_SECTION_ID = "furtherInformation";

    public AdvertSectionOverviewPageDTO buildSectionOverviewPageContent(String schemeId, UUID advertId) {
        final String grantSchemeName = schemeService.getSchemeBySchemeId(parseInt(schemeId)).getName();
        boolean isPublishDisabled = true;

        final List<AdvertSectionOverviewPageSectionDto> dtoSectionsList = new ArrayList<>();
        final List<AdvertDefinitionSection> statelessSections = definition.getSections();

        GrantAdvert grantAdvert = grantAdvertService.getAdvertById(advertId, false);

        final List<GrantAdvertSectionResponse> sectionsWithStatus = grantAdvert.getResponse() != null
                ? grantAdvert.getResponse().getSections() : new ArrayList<>();

        // builds the Sections needed to the DTO
        populateSectionsListForDto(dtoSectionsList, statelessSections, sectionsWithStatus);

        if (!dtoSectionsList.isEmpty()) {
            final List<AdvertSectionOverviewPageSectionDto> nonCompletedSections = dtoSectionsList.stream().filter(
                    joinedSection -> !joinedSection.getStatus().equals(GrantAdvertSectionResponseStatus.COMPLETED))
                    .toList();
            isPublishDisabled = !nonCompletedSections.isEmpty();
        }

        // builds the dto needed to the frontend
        final AdvertSectionOverviewPageDTO response = AdvertSectionOverviewPageDTO.builder().sections(dtoSectionsList)
                .advertName(grantAdvert.getGrantAdvertName()).grantSchemeName(grantSchemeName)
                .isPublishDisabled(isPublishDisabled).build();
        log.info("{} with id {} and advert id {}, section-overview page content, successfully created", grantSchemeName,
                schemeId, advertId);
        return response;
    }

    public AdvertSummaryPageDTO buildSummaryPageContent(String schemeId, UUID advertId) {

        final AdvertSummaryPageDTO advertSummaryPageDTO = new AdvertSummaryPageDTO();
        final GrantAdvert grantAdvert = grantAdvertService.getAdvertById(advertId, false);
        final List<AdvertDefinitionSection> advertDefinitionSections = definition.getSections();

        advertSummaryPageDTO.setId(grantAdvert.getId());
        advertSummaryPageDTO.setAdvertName(grantAdvert.getGrantAdvertName());
        advertSummaryPageDTO.setSections(mergeDefinitionAndQuestionResponseForSummaryPage(advertDefinitionSections,
                advertSummaryPageDTO, grantAdvert));
        advertSummaryPageDTO.setStatus(grantAdvert.getStatus());

        return advertSummaryPageDTO;
    }

    private List<AdvertSummaryPageDTO.AdvertSummaryPageSectionDTO> mergeDefinitionAndQuestionResponseForSummaryPage(
            List<AdvertDefinitionSection> advertDefinitionSections, AdvertSummaryPageDTO advertSummaryPageDTO,
            GrantAdvert grantAdvert) {
        return advertDefinitionSections.stream().map(advertDefinitionSection -> {
            AdvertSummaryPageDTO.AdvertSummaryPageSectionDTO sectionDTO = advertSummaryPageDTO.new AdvertSummaryPageSectionDTO();
            sectionDTO.setTitle(advertDefinitionSection.getTitle());
            sectionDTO.setId(advertDefinitionSection.getId());

            GrantAdvertSectionResponse grantAdvertSectionResponse = grantAdvert.getResponse() != null
                    ? grantAdvert.getResponse().getSectionById(advertDefinitionSection.getId()).orElse(null) : null;

            List<AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO> pageDTOs = advertDefinitionSection.getPages()
                    .stream().map(advertDefinitionPage -> {

                        AdvertSummaryPageDTO.AdvertSummaryPageSectionPageDTO pageDTO = advertSummaryPageDTO.new AdvertSummaryPageSectionPageDTO();
                        pageDTO.setTitle(advertDefinitionPage.getTitle());
                        pageDTO.setId(advertDefinitionPage.getId());

                        GrantAdvertPageResponse grantAdvertPageResponse = grantAdvertSectionResponse != null
                                ? grantAdvertSectionResponse.getPageById(advertDefinitionPage.getId()).orElse(null)
                                : null;

                        List<AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO> pageQuestionDTOs = advertDefinitionPage
                                .getQuestions().stream().map(advertDefinitionQuestion -> {
                                    AdvertSummaryPageDTO.AdvertSummaryPageQuestionDTO questionDTO = advertSummaryPageDTO.new AdvertSummaryPageQuestionDTO();
                                    GrantAdvertQuestionResponse grantAdvertQuestionResponse = grantAdvertPageResponse != null
                                            ? grantAdvertPageResponse.getQuestionById(advertDefinitionQuestion.getId())
                                                    .orElse(null)
                                            : null;

                                    String response = null;
                                    if (advertDefinitionQuestion
                                            .getResponseType() == AdvertDefinitionQuestionResponseType.CURRENCY) {
                                        if (grantAdvertQuestionResponse != null
                                                && grantAdvertQuestionResponse.getResponse() != null) {
                                            response = CurrencyFormatter.format(
                                                    Integer.parseInt(grantAdvertQuestionResponse.getResponse()));
                                        }
                                    }
                                    else {
                                        response = grantAdvertQuestionResponse != null
                                                ? grantAdvertQuestionResponse.getResponse() : null;
                                    }
                                    questionDTO.setId(advertDefinitionQuestion.getId());
                                    questionDTO.setResponse(response);
                                    questionDTO.setSummarySuffixText(advertDefinitionQuestion.getSummarySuffixText());
                                    questionDTO.setTitle(advertDefinitionQuestion.getSummaryTitle());
                                    questionDTO.setMultiResponse(grantAdvertQuestionResponse != null
                                            ? grantAdvertQuestionResponse.getMultiResponse() : null);
                                    questionDTO.setResponseType(advertDefinitionQuestion.getResponseType());

                                    return questionDTO;
                                }).collect(Collectors.toList());

                        pageDTO.setQuestions(pageQuestionDTOs);
                        return pageDTO;
                    }).collect(Collectors.toList());

            sectionDTO.setPages(pageDTOs);

            return sectionDTO;
        }).collect(Collectors.toList());
    }

    public void populateSectionsListForDto(List<AdvertSectionOverviewPageSectionDto> dtoSectionsList,
            List<AdvertDefinitionSection> statelessSections, List<GrantAdvertSectionResponse> sectionsWithStatus) {
        for (AdvertDefinitionSection section : statelessSections) {
            final AdvertSectionOverviewPageSectionDto sectionDto = AdvertSectionOverviewPageSectionDto.builder()
                    .build();
            final List<AdvertSectionOverviewPagePageDto> pagesList = new ArrayList<>();
            final List<GrantAdvertSectionResponse> sectionWithSameId = sectionsWithStatus.stream()
                    .filter(sectionWithStatus -> sectionWithStatus.getId().equals(section.getId())).toList();
            final boolean sectionWithSameIdExist = !sectionWithSameId.isEmpty();
            sectionDto.setId(section.getId());
            sectionDto.setTitle(section.getTitle());
            sectionDto.setStatus(sectionWithSameIdExist ? sectionWithSameId.get(0).getStatus()
                    : GrantAdvertSectionResponseStatus.NOT_STARTED);
            for (AdvertDefinitionPage page : section.getPages()) {
                final List<GrantAdvertPageResponse> pageWithSameId = sectionWithSameIdExist
                        ? sectionWithSameId.get(0).getPages().stream()
                                .filter(pageWithStatus -> pageWithStatus.getId().equals(page.getId())).toList()
                        : new ArrayList<>();
                final AdvertSectionOverviewPagePageDto pageDto = AdvertSectionOverviewPagePageDto.builder()
                        .id(page.getId()).title(page.getTitle()).status(pageWithSameId.isEmpty()
                                ? GrantAdvertPageResponseStatus.NOT_STARTED : pageWithSameId.get(0).getStatus())
                        .build();
                pagesList.add(pageDto);
            }
            sectionDto.setPages(pagesList);
            dtoSectionsList.add(sectionDto);
        }
    }

    public AdvertPreviewPageDto buildAdvertPreview(UUID grantAdvertId) {

        final GrantAdvert grantAdvert = grantAdvertService.getAdvertById(grantAdvertId, false);
        final GrantAdvertResponse response = grantAdvert.getResponse();

        // quick check, if the response is null, immediately return with just advert name
        if (response == null) {
            return AdvertPreviewPageDto.builder().grantName(grantAdvert.getGrantAdvertName()).build();
        }

        List<AdvertPreviewTab> advertPreviewTabs = List.of(
                AdvertPreviewTab.builder().name("Summary")
                        .content(returnRichTextHtml(response.nullCheckMultiResponse(FURTHER_INFORMATION_SECTION_ID, "2",
                                "grantSummaryTab")))
                        .build(),
                AdvertPreviewTab.builder().name("Eligibility")
                        .content(returnRichTextHtml(response.nullCheckMultiResponse(FURTHER_INFORMATION_SECTION_ID, "1",
                                "grantEligibilityTab")))
                        .build(),
                AdvertPreviewTab.builder().name("Objectives")
                        .content(returnRichTextHtml(response.nullCheckMultiResponse(FURTHER_INFORMATION_SECTION_ID, "4",
                                "grantObjectivesTab")))
                        .build(),
                AdvertPreviewTab.builder().name("Dates")
                        .content(returnRichTextHtml(
                                response.nullCheckMultiResponse(FURTHER_INFORMATION_SECTION_ID, "3", "grantDatesTab")))
                        .build(),
                AdvertPreviewTab.builder().name("How to apply")
                        .content(returnRichTextHtml(
                                response.nullCheckMultiResponse(FURTHER_INFORMATION_SECTION_ID, "5", "grantApplyTab")))
                        .build(),
                AdvertPreviewTab
                        .builder().name("Supporting information").content(returnRichTextHtml(response
                                .nullCheckMultiResponse(FURTHER_INFORMATION_SECTION_ID, "6", "grantSupportingInfoTab")))
                        .build());

        return AdvertPreviewPageDto.builder().grantName(grantAdvert.getGrantAdvertName())
                .grantShortDescription(response.nullCheckSingleResponse("grantDetails", "1", "grantShortDescription"))
                .grantApplicationOpenDate(buildDateForPreview(
                        response.nullCheckMultiResponse(ADVERT_DATES_SECTION_ID, "1", OPENING_DATE_ID)))
                .grantApplicationCloseDate(buildDateForPreview(
                        response.nullCheckMultiResponse(ADVERT_DATES_SECTION_ID, "1", CLOSING_DATE_ID)))
                .tabs(advertPreviewTabs).build();

    }

    private String returnRichTextHtml(String[] multiResponse) {
        if (multiResponse == null || multiResponse.length < 2) {
            return "";
        }

        return multiResponse[1];
    }

    private String buildDateForPreview(String[] date) {
        if (date == null) {
            return "";
        }

        int[] dateInts = Arrays.stream(date).mapToInt(Integer::parseInt).toArray();
        LocalDateTime castDate = LocalDateTime.of(dateInts[2], dateInts[1], dateInts[0], dateInts[3], dateInts[4]);

        // "30 November 2022, 12:01am"
        String previewDatePattern = "d MMMM u, hh:mma";
        DateTimeFormatter previewFormatter = DateTimeFormatter.ofPattern(previewDatePattern).withLocale(Locale.UK);
        return previewFormatter.format(castDate);

    }

}
