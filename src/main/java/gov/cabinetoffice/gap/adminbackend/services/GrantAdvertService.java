package gov.cabinetoffice.gap.adminbackend.services;

import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.QueryOperation;
import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.model.CMAEntry;
import com.contentful.java.cma.model.rich.CMARichDocument;
import gov.cabinetoffice.gap.adminbackend.config.ContentfulConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.FeatureFlagsConfigurationProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPageResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPublishingInformationResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertStatusResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GrantAdvertPageResponseValidationDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSectionResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.GrantAdvertException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.GrantAdvertMapper;
import gov.cabinetoffice.gap.adminbackend.models.*;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdvertRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.utils.CurrencyFormatter;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.ADVERT_DATES_SECTION_ID;
import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.CLOSING_DATE_ID;
import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.OPENING_DATE_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrantAdvertService {

    private static final String CONTENTFUL_LOCALE = "en-US";

    private static final String CONTENTFUL_GRANT_TYPE_ID = "grantDetails";

    private final AdvertDefinition advertDefinition;

    private final GrantAdvertRepository grantAdvertRepository;

    private final GrantAdminRepository grantAdminRepository;

    private final SchemeRepository schemeRepository;

    private final GrantAdvertMapper grantAdvertMapper;

    private final CMAClient contentfulManagementClient;

    private final CDAClient contentfulDeliveryClient;

    private final RestTemplate restTemplate;

    private final ContentfulConfigProperties contentfulProperties;

    private final FeatureFlagsConfigurationProperties featureFlagsProperties;

    public GrantAdvert create(Integer grantSchemeId, Integer grantAdminId, String name) {
        final GrantAdmin grantAdmin = grantAdminRepository.findById(grantAdminId).orElseThrow();
        final SchemeEntity scheme = schemeRepository.findById(grantSchemeId).orElseThrow();
        if (!scheme.getFunderId().equals(grantAdmin.getFunder().getId())) {
            throw new AccessDeniedException(
                    "User " + grantAdminId + " is unable to access scheme with id " + scheme.getId());
        }
        final Integer version = featureFlagsProperties.isNewMandatoryQuestionsEnabled() ? 2 : 1;
        final GrantAdvert grantAdvert = GrantAdvert.builder().grantAdvertName(name).scheme(scheme).createdBy(grantAdmin)
                .created(Instant.now()).lastUpdatedBy(grantAdmin).lastUpdated(Instant.now())
                .status(GrantAdvertStatus.DRAFT).version(version).build();
        return this.grantAdvertRepository.save(grantAdvert);
    }

    /**
     * This method includes access control check, only allowing admins to view their own
     * adverts
     */
    public GrantAdvert getAdvertById(UUID advertId, boolean lambdaCall) {

        GrantAdvert advert = grantAdvertRepository.findById(advertId)
                .orElseThrow(() -> new NotFoundException("Advert with id " + advertId + " not found"));

        if (!lambdaCall) {
            final AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
            if (!advert.getCreatedBy().getId().equals(session.getGrantAdminId())) {
                throw new AccessDeniedException(
                        "User " + session.getGrantAdminId() + " is unable to access advert with id " + advert.getId());
            }
        }

        log.debug("Advert with id {} found", advertId);
        return advert;
    }

    public GetGrantAdvertPageResponseDTO getAdvertBuilderPageData(UUID grantAdvertId, String sectionId, String pageId) {

        GrantAdvert grantAdvert = getAdvertById(grantAdvertId, false);

        GetGrantAdvertPageResponseDTO viewResponse = new GetGrantAdvertPageResponseDTO();

        // get advert definition from spring context
        AdvertDefinition definition = advertDefinition;

        // get section information
        AdvertDefinitionSection sectionDefiniton = definition.getSectionById(sectionId);

        viewResponse.setSectionName(sectionDefiniton.getTitle());

        // get page information
        AdvertDefinitionPage pageDefinition = sectionDefiniton.getPageById(pageId);

        Integer currentPageIndex = sectionDefiniton.getIndexOfPage(pageDefinition);
        sectionDefiniton.getPageByIndex(currentPageIndex - 1)
                .ifPresent(page -> viewResponse.setPreviousPageId(page.getId()));
        sectionDefiniton.getPageByIndex(currentPageIndex + 1)
                .ifPresent(page -> viewResponse.setNextPageId(page.getId()));

        viewResponse.setPageTitle(pageDefinition.getTitle());

        // get question information
        List<AdvertBuilderQuestionView> questionViews = grantAdvertMapper
                .advertBuilderQuestionViewListFromDefintionQuestionList(pageDefinition.getQuestions());

        // get previous responses for questions
        Optional<GrantAdvertResponse> advertResponse = Optional.ofNullable(grantAdvert.getResponse());

        advertResponse
                .flatMap(response -> response.getSectionById(sectionId).flatMap(section -> section.getPageById(pageId)))
                .ifPresent(page -> {
                    viewResponse.setStatus(page.getStatus());

                    page.getQuestions().forEach(questionResponse -> questionViews.forEach(questionView -> {
                        if (questionView.getQuestionId().equals(questionResponse.getId()))
                            questionView.setResponse(questionResponse);
                    }));
                });

        viewResponse.setQuestions(questionViews);

        return viewResponse;
    }

    public void updatePageResponse(GrantAdvertPageResponseValidationDto pagePatchDto) {
        GrantAdvert grantAdvert = grantAdvertRepository.findById(pagePatchDto.getGrantAdvertId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("GrantAdvert with id %s not found", pagePatchDto.getGrantAdvertId())));
        // adds the static opening and closing time to the date question
        addStaticTimeToDateQuestion(pagePatchDto);

        // if response/section/page does not exist, create it. If it does exist, update it
        GrantAdvertResponse response = Optional.ofNullable(grantAdvert.getResponse()).orElseGet(() -> {
            GrantAdvertResponse newResponse = GrantAdvertResponse.builder().build();
            grantAdvert.setResponse(newResponse);
            return newResponse;
        });
        GrantAdvertSectionResponse section = response.getSectionById(pagePatchDto.getSectionId()).orElseGet(() -> {
            GrantAdvertSectionResponse newSection = GrantAdvertSectionResponse.builder().id(pagePatchDto.getSectionId())
                    .status(GrantAdvertSectionResponseStatus.IN_PROGRESS).build();
            response.getSections().add(newSection);
            return newSection;
        });
        GrantAdvertPageResponse page = section.getPageById(pagePatchDto.getPage().getId()).orElseGet(() -> {
            GrantAdvertPageResponse newPage = GrantAdvertPageResponse.builder().id(pagePatchDto.getPage().getId())
                    .build();
            section.getPages().add(newPage);
            return newPage;
        });

        // ideally I'd use the mapper to do this but mapstruct seems to struggle
        // with updating iterables and maps etc.
        page.setQuestions(pagePatchDto.getPage().getQuestions());
        page.setStatus(pagePatchDto.getPage().getStatus());

        // update section status
        updateSectionStatus(section);

        grantAdvertRepository.save(grantAdvert);

    }

    private void addStaticTimeToDateQuestion(GrantAdvertPageResponseValidationDto pagePatchDto) {
        if (pagePatchDto.getSectionId().equals(ADVERT_DATES_SECTION_ID)) {
            pagePatchDto.getPage().getQuestions().forEach(question -> {
                String[] multiResponse = question.getMultiResponse();
                if (question.getId().equals(OPENING_DATE_ID)) {
                    String[] opening = new String[] { multiResponse[0], multiResponse[1], multiResponse[2], "00",
                            "01" };
                    pagePatchDto.getPage().getQuestions().get(0).setMultiResponse(opening);
                }
                else if (question.getId().equals(CLOSING_DATE_ID)) {
                    String[] closing = new String[] { multiResponse[0], multiResponse[1], multiResponse[2], "23",
                            "59" };
                    pagePatchDto.getPage().getQuestions().get(1).setMultiResponse(closing);
                }
            });
        }
    }

    private void updateSectionStatus(GrantAdvertSectionResponse section) {
        AdvertDefinitionSection definitionSection = advertDefinition.getSectionById(section.getId());
        if (section.getPages().isEmpty()) {
            // don't think this should ever trigger realistically
            section.setStatus(GrantAdvertSectionResponseStatus.NOT_STARTED);
        }
        else if (section.getPages().size() == definitionSection.getPages().size()
                && section.getPages().stream().allMatch((grantAdvertPageResponse -> grantAdvertPageResponse
                        .getStatus() == GrantAdvertPageResponseStatus.COMPLETED))) {
            section.setStatus(GrantAdvertSectionResponseStatus.COMPLETED);
        }
        else {
            section.setStatus(GrantAdvertSectionResponseStatus.IN_PROGRESS);
        }
    }

    @Transactional
    public void deleteGrantAdvert(UUID grantAdvertId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        Long deletedCount = grantAdvertRepository.deleteByIdAndCreatedById(grantAdvertId, session.getGrantAdminId());

        if (deletedCount == 0)
            throw new NotFoundException("Grant Advert not found with id of " + grantAdvertId);
    }

    @Transactional
    public GrantAdvert publishAdvert(UUID advertId, boolean lambdaCall) {
        final GrantAdvert advert = getAdvertById(advertId, lambdaCall);

        final CMAEntry contentfulAdvert;

        // if advert has not been published previously
        if (advert.getFirstPublishedDate() == null) {
            contentfulAdvert = createAdvertInContentful(advert);
            advert.setFirstPublishedDate(Instant.now());
        }
        else {
            contentfulAdvert = updateAdvertInContentful(advert);
            advert.setLastPublishedDate(Instant.now());
        }

        contentfulManagementClient.entries().publish(contentfulAdvert);

        advert.setStatus(GrantAdvertStatus.PUBLISHED);
        advert.setContentfulSlug(contentfulAdvert.getField("label", CONTENTFUL_LOCALE));
        advert.setContentfulEntryId(contentfulAdvert.getId());
        updateGrantAdvertApplicationDates(advert);

        return grantAdvertRepository.save(advert);
    }

    public void unpublishAdvert(UUID advertId, boolean lambdaCall) {
        final GrantAdvert advert = this.getAdvertById(advertId, lambdaCall);

        final CMAEntry contentfulAdvert = contentfulManagementClient.entries().fetchOne(advert.getContentfulEntryId());

        contentfulManagementClient.entries().unPublish(contentfulAdvert);
        advert.setStatus(GrantAdvertStatus.DRAFT);
        advert.setUnpublishedDate(Instant.now());

        this.grantAdvertRepository.save(advert);
    }

    private CMAEntry createAdvertInContentful(final GrantAdvert grantAdvert) {
        final CMAEntry contentfulAdvert = new CMAEntry();

        grantAdvert.getResponse().getSections().stream().flatMap(s -> s.getPages().stream())
                .flatMap(p -> p.getQuestions().stream()).forEach(r -> addFieldToContentfulAdvert(contentfulAdvert, r));

        contentfulAdvert.setField("grantName", CONTENTFUL_LOCALE, grantAdvert.getGrantAdvertName());
        contentfulAdvert.setField("label", CONTENTFUL_LOCALE, generateUniqueSlug(grantAdvert));

        final CMAEntry createdAAdvert = contentfulManagementClient.entries().create(CONTENTFUL_GRANT_TYPE_ID,
                contentfulAdvert);
        createRichTextQuestionsInContentful(grantAdvert, createdAAdvert);

        /*
         * hate this but because we create a new advert and then immediately update it the
         * version number in contentful is bumped up so we need to refresh the data to
         * prevent errors when publishing the advert :(.
         *
         * Absolutely begging to be rate limited by getting this loose with the number of
         * requests to their API.
         */
        return contentfulManagementClient.entries().fetchOne(createdAAdvert.getId());
    }

    private CMAEntry updateAdvertInContentful(final GrantAdvert grantAdvert) {
        final CMAEntry contentfulAdvert = contentfulManagementClient.entries()
                .fetchOne(grantAdvert.getContentfulEntryId());

        grantAdvert.getResponse().getSections().stream().flatMap(s -> s.getPages().stream())
                .flatMap(p -> p.getQuestions().stream()).forEach(r -> addFieldToContentfulAdvert(contentfulAdvert, r));

        contentfulAdvert.setField("grantName", CONTENTFUL_LOCALE, grantAdvert.getGrantAdvertName());
        contentfulAdvert.setField("label", CONTENTFUL_LOCALE, generateUniqueSlug(grantAdvert));

        final CMAEntry updatedAdvert = contentfulManagementClient.entries().update(contentfulAdvert);
        createRichTextQuestionsInContentful(grantAdvert, updatedAdvert);

        return contentfulManagementClient.entries().fetchOne(updatedAdvert.getId());
    }

    private String generateUniqueSlug(final GrantAdvert grantAdvert) {

        String currentSlug = grantAdvert.getScheme().getName().toLowerCase().replaceAll("[^a-z\\d\\- ]", "").trim()
                .replace(" ", "-");

        final CDAArray allAdvertEntries = contentfulDeliveryClient.fetch(CDAEntry.class)
                .withContentType(CONTENTFUL_GRANT_TYPE_ID).where("fields.label", QueryOperation.Matches, currentSlug)
                .all();

        if (allAdvertEntries.entries().isEmpty()) {
            return currentSlug.concat("-1");
        }

        return currentSlug.concat("-" + findMaxInteger(allAdvertEntries.entries().values()));
    }

    private int findMaxInteger(Collection<CDAEntry> entries) {
        return entries.stream().mapToInt(entry -> {
            String fieldValue = entry.getField("label");
            String substring = fieldValue.substring(fieldValue.lastIndexOf("-") + 1);

            try {
                return Integer.parseInt(substring);
            }
            catch (NumberFormatException e) {
                return 0;
            }
        }).max().getAsInt() + 1;
    }

    private void addFieldToContentfulAdvert(final CMAEntry contentfulAdvert,
            final GrantAdvertQuestionResponse questionResponse) {
        final AdvertDefinitionQuestionResponseType answerType = advertDefinition.getSections().stream()
                .flatMap(s -> s.getPages().stream()).flatMap(p -> p.getQuestions().stream())
                .filter(q -> q.getId().equals(questionResponse.getId())).findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format("No question with ID %s found in advert definition", questionResponse.getId())))
                .getResponseType();

        switch (questionResponse.getId()) {
            case "grantTotalAwardAmount":
                contentfulAdvert.setField("grantTotalAwardDisplay", CONTENTFUL_LOCALE,
                        CurrencyFormatter.format(Integer.parseInt(questionResponse.getResponse())));
                break;

            case "grantMinimumAward":
                contentfulAdvert.setField("grantMinimumAwardDisplay", CONTENTFUL_LOCALE,
                        CurrencyFormatter.format(Integer.parseInt(questionResponse.getResponse())));
                break;

            case "grantMaximumAward":
                contentfulAdvert.setField("grantMaximumAwardDisplay", CONTENTFUL_LOCALE,
                        CurrencyFormatter.format(Integer.parseInt(questionResponse.getResponse())));
                break;

        }

        final Object contentfulValue = convertQuestionResponseToContentfulFormat(answerType, questionResponse);
        contentfulAdvert.setField(questionResponse.getId(), CONTENTFUL_LOCALE, contentfulValue);
    }

    // TODO ideally this is just a stop gap until we can use the CMA library to convert
    // the rich text strings to CMARichDocument objects
    private void createRichTextQuestionsInContentful(final GrantAdvert advert, final CMAEntry contentfulAdvert) {

        // get all the rich text responses
        final List<GrantAdvertQuestionResponse> responses = advert.getResponse().getSections().stream()
                .flatMap(s -> s.getPages().stream()).flatMap(p -> p.getQuestions().stream())
                .filter(q -> advertDefinition.getSections().stream().flatMap(s -> s.getPages().stream())
                        .flatMap(p -> p.getQuestions().stream())
                        .filter(qr -> qr.getResponseType().equals(AdvertDefinitionQuestionResponseType.RICH_TEXT))
                        .anyMatch(qr -> qr.getId().equals(q.getId())))
                .toList();

        if (responses != null && !responses.isEmpty()) {
            final String requestBody = buildRichTextPatchRequestBody(responses);

            final HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", String.format("Bearer %s", contentfulProperties.getAccessToken()));
            headers.add("Content-Type", "application/json-patch+json");
            headers.add("X-Contentful-Version", contentfulAdvert.getVersion().toString());

            // send to contentful
            final String url = String.format("https://api.contentful.com/spaces/%1$s/environments/%2$s/entries/%3$s",
                    contentfulProperties.getSpaceId(), contentfulProperties.getEnvironmentId(),
                    contentfulAdvert.getId());
            log.debug(url);

            restTemplate.patchForObject(url, new HttpEntity<>(requestBody, headers), CMAEntry.class);
        }
    }

    // built to replicate
    // https://www.contentful.com/developers/docs/references/content-management-api/#/reference/entries/entry/patch-an-entry/console/curl
    private String buildRichTextPatchRequestBody(final List<GrantAdvertQuestionResponse> responses) {
        final JSONArray req = new JSONArray();

        responses.forEach(response -> {
            final JSONObject patch = new JSONObject();
            patch.put("op", "add");
            patch.put("path", String.format("/fields/%s/en-US", response.getId()));
            patch.put("value", new JSONObject(response.getMultiResponse()[1]));

            req.put(patch);
        });

        log.debug(req.toString());

        return req.toString();
    }

    private Object convertQuestionResponseToContentfulFormat(final AdvertDefinitionQuestionResponseType answerType,
            final GrantAdvertQuestionResponse questionResponse) {
        return switch (answerType) {
            case DATE -> buildDateFromResponse(questionResponse);
            // This is a required step even though we update the value later. Do not
            // delete
            case RICH_TEXT -> new CMARichDocument();
            case CURRENCY -> Integer.valueOf(questionResponse.getResponse());
            case LIST -> questionResponse.getMultiResponse();
            default -> questionResponse.getResponse();
        };
    }

    private String buildDateFromResponse(final GrantAdvertQuestionResponse questionResponse) {

        // convert to ISO-2601 as per
        // https://www.contentful.com/developers/docs/concepts/data-model/#:~:text=3.14-,Date%20and%20time%202,-Date
        final String day = questionResponse.getMultiResponse()[0];
        final String month = questionResponse.getMultiResponse()[1];
        final String year = questionResponse.getMultiResponse()[2];
        final String hour = questionResponse.getMultiResponse()[3];
        final String minute = questionResponse.getMultiResponse()[4];

        final String dateStr = new StringBuilder(year).append("-").append(month).append("-").append(day).append("T")
                .append(hour).append(":").append(minute).append(":").append("00").toString();

        log.debug(dateStr);
        // Date formatter to handle single digit Month and Day values
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d'T'HH:mm:ss");
        return LocalDateTime.parse(dateStr, formatter).toString();
    }

    public GetGrantAdvertPublishingInformationResponseDTO getGrantAdvertPublishingInformationBySchemeId(
            Integer grantSchemeId) {
        GrantAdvert grantAdvert = grantAdvertRepository.findBySchemeId(grantSchemeId).orElseThrow(
                () -> new NotFoundException("Grant Advert for Scheme with id " + grantSchemeId + " does not exist"));

        return this.grantAdvertMapper.grantAdvertPublishInformationResponseDtoFromGrantAdvert(grantAdvert);
    }

    public GetGrantAdvertStatusResponseDTO getGrantAdvertStatusBySchemeId(Integer grantSchemeId) {
        GrantAdvert grantAdvert = grantAdvertRepository.findBySchemeId(grantSchemeId).orElseThrow(
                () -> new NotFoundException("Grant Advert for Scheme with id " + grantSchemeId + " does not exist"));

        return this.grantAdvertMapper.grantAdvertStatusResponseDtoFromGrantAdvert(grantAdvert);

    }

    public void scheduleGrantAdvert(final UUID grantAdvertId) {
        GrantAdvert grantAdvert = getAdvertById(grantAdvertId, false);

        grantAdvert.setStatus(GrantAdvertStatus.SCHEDULED);
        updateGrantAdvertApplicationDates(grantAdvert);

        grantAdvertRepository.save(grantAdvert);
    }

    private void updateGrantAdvertApplicationDates(final GrantAdvert grantAdvert) {
        // these should never be null at this point, but just in case.
        // should also keep our test data in check
        GrantAdvertSectionResponse applicationDatesSection = grantAdvert.getResponse()
                .getSectionById(ADVERT_DATES_SECTION_ID)
                .orElseThrow(() -> new GrantAdvertException("Advert is missing application dates section"));
        GrantAdvertPageResponse applicationDatesPage = applicationDatesSection.getPageById("1")
                .orElseThrow(() -> new GrantAdvertException("Advert is missing application dates page"));
        GrantAdvertQuestionResponse openingDateQuestion = applicationDatesPage.getQuestionById(OPENING_DATE_ID)
                .orElseThrow(() -> new GrantAdvertException("Advert is missing opening date question"));
        GrantAdvertQuestionResponse closingDateQuestion = applicationDatesPage.getQuestionById(CLOSING_DATE_ID)
                .orElseThrow(() -> new GrantAdvertException("Advert is missing closing date question"));

        if (openingDateQuestion.getMultiResponse().length < 5 || closingDateQuestion.getMultiResponse().length < 5) {
            throw new GrantAdvertException("Advert dates are not fully populated");
        }

        // convert the string[] to int[], to easily build Calendars
        int[] openingResponse = Arrays.stream(openingDateQuestion.getMultiResponse()).mapToInt(Integer::parseInt)
                .toArray();
        int[] closingResponse = Arrays.stream(closingDateQuestion.getMultiResponse()).mapToInt(Integer::parseInt)
                .toArray();

        // build LocalDateTimes, convert to Instant
        Instant openingDateInstant = LocalDateTime
                .of(openingResponse[2], openingResponse[1], openingResponse[0], openingResponse[3], openingResponse[4])
                .atZone(ZoneId.of("Z")).toInstant();

        Instant closingDateInstant = LocalDateTime
                .of(closingResponse[2], closingResponse[1], closingResponse[0], closingResponse[3], closingResponse[4])
                .atZone(ZoneId.of("Z")).toInstant();

        // set dates on advert
        grantAdvert.setOpeningDate(openingDateInstant);
        grantAdvert.setClosingDate(closingDateInstant);

    }

    public void unscheduleGrantAdvert(final UUID advertId) {
        final GrantAdvert advert = getAdvertById(advertId, false);
        advert.setStatus(GrantAdvertStatus.UNSCHEDULED);
        grantAdvertRepository.save(advert);
    }

    public void patchCreatedBy(Integer adminId, Integer schemeId) {
        GrantAdvert advert = this.grantAdvertRepository.findBySchemeId(schemeId).orElseThrow(
                () -> new NotFoundException("Grant Advert for Scheme with id " + schemeId + " does not exist"));
            advert.setCreatedBy(this.grantAdminRepository.findById(adminId).orElseThrow());
            this.grantAdvertRepository.save(advert);
        }

}
