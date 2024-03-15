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
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.*;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSectionResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.ConflictException;
import gov.cabinetoffice.gap.adminbackend.exceptions.GrantAdvertException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UserNotFoundException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.*;

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

    private final UserService userService;

    private final WebClient.Builder webClientBuilder;

    private final Clock clock;

    private final ContentfulConfigProperties contentfulProperties;

    private final FeatureFlagsConfigurationProperties featureFlagsProperties;

    public GrantAdvert save(GrantAdvert advert) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional.ofNullable(auth)
                .ifPresentOrElse(authentication -> {
                    final AdminSession adminSession = (AdminSession) authentication.getPrincipal();
                    final GrantAdmin admin = grantAdminRepository.findByGapUserUserSub(adminSession.getUserSub())
                            .orElseThrow(() -> new UserNotFoundException("Could not find an admin with sub " + adminSession.getUserSub()));

                    if (advert.getScheme().getGrantAdmins().contains(admin)) {
                        final Instant updatedAt = Instant.now(clock);
                        advert.setLastUpdated(updatedAt);
                        advert.setLastUpdatedBy(admin);

                        advert.getScheme().setLastUpdated(updatedAt);
                        advert.getScheme().setLastUpdatedBy(adminSession.getGrantAdminId());

                        advert.setValidLastUpdated(true);
                    }
                }, () -> log.warn("Admin session was null. Update must have been performed by a lambda."));

        return grantAdvertRepository.save(advert);
    }

    public GrantAdvert create(Integer grantSchemeId, Integer grantAdminId, String name) {
        final GrantAdmin grantAdmin = grantAdminRepository.findById(grantAdminId).orElseThrow();
        final SchemeEntity scheme = schemeRepository.findById(grantSchemeId).orElseThrow();
        final Integer version = featureFlagsProperties.isNewMandatoryQuestionsEnabled() ? 2 : 1;
        final boolean doesAdvertExist = grantAdvertRepository.findBySchemeId(grantSchemeId).isPresent();

        if (!doesAdvertExist) {
            final GrantAdvert grantAdvert = GrantAdvert.builder().grantAdvertName(name).scheme(scheme)
                    .createdBy(grantAdmin).created(Instant.now()).lastUpdatedBy(grantAdmin).lastUpdated(Instant.now())
                    .status(GrantAdvertStatus.DRAFT).version(version).validLastUpdated(true).build();
            return save(grantAdvert);
        }
        final GrantAdvert existingAdvert = grantAdvertRepository.findBySchemeId(grantSchemeId).get();
        existingAdvert.setGrantAdvertName(name);

        return save(existingAdvert);
    }

    /**
     * This method includes access control check, only allowing admins to view their own
     * adverts
     */
    public GrantAdvert getAdvertById(UUID advertId) {

        GrantAdvert advert = grantAdvertRepository.findById(advertId)
                .orElseThrow(() -> new NotFoundException("Advert with id " + advertId + " not found"));

        log.debug("Advert with id {} found", advertId);
        return advert;
    }

    public GetGrantAdvertPageResponseDTO getAdvertBuilderPageData(UUID grantAdvertId, String sectionId, String pageId) {

        GrantAdvert grantAdvert = getAdvertById(grantAdvertId);

        GetGrantAdvertPageResponseDTO viewResponse = new GetGrantAdvertPageResponseDTO();

        // get section information
        AdvertDefinitionSection sectionDefiniton = advertDefinition.getSectionById(sectionId);

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

        validateAdvertStatus(grantAdvert);
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

        save(grantAdvert);
    }

    private void addStaticTimeToDateQuestion(GrantAdvertPageResponseValidationDto pagePatchDto) {
        if (pagePatchDto.getSectionId().equals(ADVERT_DATES_SECTION_ID)) {
            pagePatchDto.getPage().getQuestions().forEach(question -> {
                String[] multiResponse = question.getMultiResponse();
                if (question.getId().equals(OPENING_DATE_ID)) {
                    String[] openingTime = multiResponse[3].split(":");
                    String[] openingDateTime = new String[] { multiResponse[0], multiResponse[1], multiResponse[2],
                            openingTime[0], openingTime[1] };
                    pagePatchDto.getPage().getQuestions().get(0).setMultiResponse(openingDateTime);
                }
                else if (question.getId().equals(CLOSING_DATE_ID)) {
                    String[] closingTime = multiResponse[3].split(":");
                    String[] closingDateTime = new String[] { multiResponse[0], multiResponse[1], multiResponse[2],
                            closingTime[0], closingTime[1] };

                    if (multiResponse[3].equals("23:59")) {
                        closingDateTime = adjustToMidnightNextDay(multiResponse, closingTime);
                    }

                    pagePatchDto.getPage().getQuestions().get(1).setMultiResponse(closingDateTime);
                }
            });
        }
    }

    private String[] adjustToMidnightNextDay(String[] multiResponse, String[] closingTime) {
        // increment date by 1 day and set time to 00:00
        LocalDateTime dateTime = convertToDateTime(multiResponse, closingTime);
        LocalDateTime incrementedDateTime = dateTime.plusDays(1);
        int incrementedDay = incrementedDateTime.getDayOfMonth();
        int incrementedMonth = incrementedDateTime.getMonthValue();
        int incrementedYear = incrementedDateTime.getYear();

        return new String[] { String.format("%02d", incrementedDay), String.valueOf(incrementedMonth),
                String.valueOf(incrementedYear), "00", "00" };
    }

    private static LocalDateTime convertToDateTime(String[] date, String[] time) {
        int day = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]);
        int year = Integer.parseInt(date[2]);
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);

        return LocalDateTime.of(year, Month.of(month), day, hour, minute);
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

        int deletedCount = grantAdvertRepository.deleteByIdAndSchemeEditor(grantAdvertId, session.getGrantAdminId());

        if (deletedCount == 0)
            throw new NotFoundException("Grant Advert not found with id of " + grantAdvertId);
    }

    @Transactional
    public GrantAdvert publishAdvert(UUID advertId) {
        final GrantAdvert advert = getAdvertById(advertId);

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

        return save(advert);
    }

    public void unpublishAdvert(UUID advertId) {
        final GrantAdvert advert = this.getAdvertById(advertId);

        final CMAEntry contentfulAdvert = contentfulManagementClient.entries().fetchOne(advert.getContentfulEntryId());

        contentfulManagementClient.entries().unPublish(contentfulAdvert);
        advert.setStatus(GrantAdvertStatus.DRAFT);
        advert.setUnpublishedDate(Instant.now());

        save(advert);
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
         * version number in contentful is bumped up, so we need to refresh the data to
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
        contentfulAdvert.setField("grantUpdated", CONTENTFUL_LOCALE, true);

        final CMAEntry updatedAdvert = contentfulManagementClient.entries().update(contentfulAdvert);
        createRichTextQuestionsInContentful(grantAdvert, updatedAdvert);

        return contentfulManagementClient.entries().fetchOne(updatedAdvert.getId());
    }

    private String generateUniqueSlug(final GrantAdvert grantAdvert) {

        String currentSlug = grantAdvert.getScheme().getName().toLowerCase(Locale.UK).replaceAll("[^a-z\\d\\- ]", "")
                .trim().replace(" ", "-");

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
            String substring = fieldValue.substring(fieldValue.lastIndexOf('-') + 1);

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
            case "grantTotalAwardAmount" -> contentfulAdvert.setField("grantTotalAwardDisplay", CONTENTFUL_LOCALE,
                    CurrencyFormatter.format(Integer.parseInt(questionResponse.getResponse())));
            case "grantMinimumAward" -> contentfulAdvert.setField("grantMinimumAwardDisplay", CONTENTFUL_LOCALE,
                    CurrencyFormatter.format(Integer.parseInt(questionResponse.getResponse())));
            case "grantMaximumAward" -> contentfulAdvert.setField("grantMaximumAwardDisplay", CONTENTFUL_LOCALE,
                    CurrencyFormatter.format(Integer.parseInt(questionResponse.getResponse())));
        }

        final Object contentfulValue = convertQuestionResponseToContentfulFormat(answerType, questionResponse);
        contentfulAdvert.setField(questionResponse.getId(), CONTENTFUL_LOCALE, contentfulValue);
    }

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

            // send to contentful
            final String url = String.format("https://api.contentful.com/spaces/%1$s/environments/%2$s/entries/%3$s",
                    contentfulProperties.getSpaceId(), contentfulProperties.getEnvironmentId(),
                    contentfulAdvert.getId());
            log.debug(url);

            webClientBuilder.build()
                    .patch()
                    .uri(url)
                    .headers(h -> {
                        h.set("Authorization", String.format("Bearer %s", contentfulProperties.getAccessToken()));
                        h.set("Content-Type", "application/json-patch+json");
                        h.set("X-Contentful-Version", contentfulAdvert.getVersion().toString());
                    })
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnError(exception -> log.error("createRichTextQuestionsInContentful failed on PATCH to {}, with message: {}", url, exception.getMessage()))
                    .block();
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

        final String dateStr = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + "00";

        log.debug(dateStr);
        // Date formatter to handle single digit Month and Day values
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d'T'HH:mm:ss");
        return LocalDateTime.parse(dateStr, formatter).toString();
    }

    public GetGrantAdvertPublishingInformationResponseDTO getGrantAdvertPublishingInformationBySchemeId(
            Integer grantSchemeId) {
        GrantAdvert grantAdvert = grantAdvertRepository.findBySchemeId(grantSchemeId).orElseThrow(
                () -> new NotFoundException("Grant Advert for Scheme with id " + grantSchemeId + " does not exist"));

        GetGrantAdvertPublishingInformationResponseDTO publishingInfo = this.grantAdvertMapper
                .grantAdvertPublishInformationResponseDtoFromGrantAdvert(grantAdvert);

        if (grantAdvert.getLastUpdatedBy() != null) {
            String adminSub = grantAdvert.getLastUpdatedBy().getGapUser().getUserSub();

            byte[] emailAddress = userService.getEmailAddressForSub(adminSub);

            publishingInfo.setLastUpdatedByEmail(emailAddress);
        }

        return publishingInfo;
    }

    public GetGrantAdvertStatusResponseDTO getGrantAdvertStatusBySchemeId(Integer grantSchemeId) {
        GrantAdvert grantAdvert = grantAdvertRepository.findBySchemeId(grantSchemeId).orElseThrow(
                () -> new NotFoundException("Grant Advert for Scheme with id " + grantSchemeId + " does not exist"));

        return this.grantAdvertMapper.grantAdvertStatusResponseDtoFromGrantAdvert(grantAdvert);

    }

    public void scheduleGrantAdvert(final UUID grantAdvertId) {
        GrantAdvert grantAdvert = getAdvertById(grantAdvertId);

        grantAdvert.setStatus(GrantAdvertStatus.SCHEDULED);
        updateGrantAdvertApplicationDates(grantAdvert);

        save(grantAdvert);
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
        final GrantAdvert advert = getAdvertById(advertId);
        advert.setStatus(GrantAdvertStatus.UNSCHEDULED);
        save(advert);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void updateAdvertOwner(Integer adminId, Integer schemeId) {
        this.grantAdvertRepository.findBySchemeId(schemeId)
                .ifPresent(advert -> {
                    advert.setCreatedBy(this.grantAdminRepository.findById(adminId).orElseThrow());
                    save(advert);
                });
    }

    public void removeAdminReferenceBySchemeId(GrantAdmin grantAdmin, Integer schemeId) {
        grantAdvertRepository.findBySchemeId(schemeId)
                .ifPresent(advert -> {
                    if (advert.getLastUpdatedBy() != null && advert.getLastUpdatedBy() == grantAdmin) {
                        advert.setLastUpdatedBy(null);
                    }
                    if (advert.getCreatedBy() != null && advert.getCreatedBy() == grantAdmin) {
                        advert.setCreatedBy(null);
                    }

                    grantAdvertRepository.save(advert);
                });
    }

    public static void validateAdvertStatus(GrantAdvert grantAdvert) {
        if (grantAdvert.getStatus() == GrantAdvertStatus.PUBLISHED || grantAdvert.getStatus() == GrantAdvertStatus.SCHEDULED) {
            throw new ConflictException("GRANT_ADVERT_MULTIPLE_EDITORS");
        }
    }
}
