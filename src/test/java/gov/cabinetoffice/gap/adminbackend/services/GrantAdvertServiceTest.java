package gov.cabinetoffice.gap.adminbackend.services;

import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.FetchQuery;
import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.ModuleEntries;
import com.contentful.java.cma.model.CMAEntry;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.ContentfulConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.FeatureFlagsConfigurationProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPageResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPublishingInformationResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertStatusResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GrantAdvertPageResponseValidationDto;
import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSectionResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.ConflictException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.GrantAdvertMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.GrantAdvertMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.*;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdvertRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomGrantAdvertGenerators;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_SCHEME_ID;
import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
class GrantAdvertServiceTest {

    @Mock
    GrantAdvertRepository grantAdvertRepository;

    @Mock
    private GrantAdminRepository grantAdminRepository;

    @Mock
    private SchemeRepository schemeRepository;

    @Mock
    private AdvertDefinition advertDefinition;

    @Mock
    private CMAClient contentfulManagementClient;

    @Mock
    private CDAClient contentfulDeliveryClient;

    @Spy
    private GrantAdvertMapper grantAdvertMapper = new GrantAdvertMapperImpl();

    @Spy
    private ContentfulConfigProperties contentfulConfigProperties = ContentfulConfigProperties.builder()
            .accessToken("an-access-token").environmentId("dev").spaceId("a-space-id")
            .deliveryAPIAccessToken("a-delivery-access-token").build();

    @Mock
    private ModuleEntries contentfulEntries;

    @Mock
    private ModuleEntries.Async async;

    @Mock
    private FetchQuery mockedFetchQuery;

    @Mock
    private CDAArray mockCDAArray;

    @Mock
    private FeatureFlagsConfigurationProperties featureFlagsConfigurationProperties;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private Clock clock;

    @Mock
    private UserService userService;

    @Mock
    private OpenSearchService openSearchService;

    @InjectMocks
    @Spy
    private GrantAdvertService grantAdvertService;

    @Nested
    @WithAdminSession
    class createAdvert {

        @Test
        void create_success() {
            final String instantExpected = "2014-12-22T10:15:30Z";
            final Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
            final Instant instant = Instant.now(clock);
            final UUID id = UUID.randomUUID();
            final int grantAdminId = 1;
            final int grantSchemeId = 1;
            final String name = "Test Grant Advert";
            final GrantAdmin grantAdmin = GrantAdmin.builder().id(grantAdminId)
                    .funder(FundingOrganisation.builder().id(1).build()).build();
            final SchemeEntity grantScheme = SchemeEntity.builder().id(grantSchemeId).funderId(1).build();

            try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
                mockedStatic.when(Instant::now).thenReturn(instant);
                GrantAdvert expectedAdvert = GrantAdvert.builder().id(id).grantAdvertName(name).scheme(grantScheme)
                        .createdBy(grantAdmin).created(Instant.now()).lastUpdatedBy(grantAdmin)
                        .lastUpdated(Instant.now()).status(GrantAdvertStatus.DRAFT).version(1).build();

                when(grantAdminRepository.findById(grantAdminId)).thenReturn(Optional.of(grantAdmin));
                when(schemeRepository.findById(grantSchemeId)).thenReturn(Optional.of(grantScheme));
                when(featureFlagsConfigurationProperties.isNewMandatoryQuestionsEnabled()).thenReturn(false);
                doReturn(expectedAdvert).when(grantAdvertService).save(any());

                GrantAdvert outputAdvert = grantAdvertService.create(grantSchemeId, grantAdminId, name);

                assertThat(outputAdvert).isEqualTo(expectedAdvert);
            }

        }

        @Test
        void create_success_existing_advert() {
            final String instantExpected = "2014-12-22T10:15:30Z";
            final Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
            final Instant instant = Instant.now(clock);
            final UUID id = UUID.randomUUID();
            final int grantAdminId = 1;
            final int grantSchemeId = 1;
            final String name = "Test Grant Advert";
            final String updatedName = "Test Grant Advert updated";
            final GrantAdmin grantAdmin = GrantAdmin.builder().id(grantAdminId)
                    .funder(FundingOrganisation.builder().id(1).build()).build();
            final SchemeEntity grantScheme = SchemeEntity.builder().id(grantSchemeId).funderId(1).build();

            try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
                mockedStatic.when(Instant::now).thenReturn(instant);
                final GrantAdvert expectedAdvert = GrantAdvert.builder().id(id).grantAdvertName(name)
                        .scheme(grantScheme).createdBy(grantAdmin).created(Instant.now()).lastUpdatedBy(grantAdmin)
                        .lastUpdated(Instant.now()).status(GrantAdvertStatus.DRAFT).version(1).build();

                when(grantAdminRepository.findById(grantAdminId)).thenReturn(Optional.of(grantAdmin));
                when(schemeRepository.findById(grantSchemeId)).thenReturn(Optional.of(grantScheme));
                when(featureFlagsConfigurationProperties.isNewMandatoryQuestionsEnabled()).thenReturn(false);
                doReturn(expectedAdvert).when(grantAdvertService).save(any());
                when(grantAdvertRepository.findBySchemeId(grantSchemeId))
                        .thenReturn(Optional.ofNullable(expectedAdvert));

                GrantAdvert outputAdvert = grantAdvertService.create(grantSchemeId, grantAdminId, name);

                expectedAdvert.setGrantAdvertName(updatedName);
                assertThat(outputAdvert).isEqualTo(expectedAdvert);
            }

        }

        @Test
        void create_successWhenNewMandatoryQuestionFeatureFlagIsTrue() {
            final String instantExpected = "2014-12-22T10:15:30Z";
            final Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
            final Instant instant = Instant.now(clock);
            final UUID id = UUID.randomUUID();
            final int grantAdminId = 1;
            final int grantSchemeId = 1;
            final String name = "Test Grant Advert";
            final GrantAdmin grantAdmin = GrantAdmin.builder().id(grantAdminId)
                    .funder(FundingOrganisation.builder().id(1).build()).build();
            final SchemeEntity grantScheme = SchemeEntity.builder().id(grantSchemeId).funderId(1).build();

            try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
                mockedStatic.when(Instant::now).thenReturn(instant);
                GrantAdvert expectedAdvert = GrantAdvert.builder().id(id).grantAdvertName(name).scheme(grantScheme)
                        .createdBy(grantAdmin).created(Instant.now()).lastUpdatedBy(grantAdmin)
                        .lastUpdated(Instant.now()).status(GrantAdvertStatus.DRAFT).version(2).build();

                when(grantAdminRepository.findById(grantAdminId)).thenReturn(Optional.of(grantAdmin));
                when(schemeRepository.findById(grantSchemeId)).thenReturn(Optional.of(grantScheme));
                when(featureFlagsConfigurationProperties.isNewMandatoryQuestionsEnabled()).thenReturn(true);
                doReturn(expectedAdvert).when(grantAdvertService).save(any());

                GrantAdvert outputAdvert = grantAdvertService.create(grantSchemeId, grantAdminId, name);

                assertThat(outputAdvert).isEqualTo(expectedAdvert);
            }

        }

        @Test
        void getById_Success() {
            final String instantExpected = "2014-12-22T10:15:30Z";
            final Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
            final Instant instant = Instant.now(clock);
            final UUID id = UUID.randomUUID();
            final int grantAdminId = 1;
            final int grantSchemeId = 1;
            final String name = "Test Grant Advert";
            final GrantAdmin grantAdmin = GrantAdmin.builder().id(grantAdminId)
                    .funder(FundingOrganisation.builder().id(1).build()).build();
            final SchemeEntity grantScheme = SchemeEntity.builder().id(grantSchemeId).funderId(1).build();

            try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
                mockedStatic.when(Instant::now).thenReturn(instant);
                GrantAdvert expectedAdvert = GrantAdvert.builder().id(id).grantAdvertName(name).scheme(grantScheme)
                        .createdBy(grantAdmin).created(Instant.now()).lastUpdatedBy(grantAdmin)
                        .lastUpdated(Instant.now()).status(GrantAdvertStatus.DRAFT).version(1).build();

                when(grantAdvertRepository.findById(id)).thenReturn(Optional.ofNullable(expectedAdvert));

                GrantAdvert response = grantAdvertService.getAdvertById(id);

                assertThat(response).isEqualTo(expectedAdvert);
            }

        }

        @Test
        void getById_ThrowNotFoundException() {
            final int grantAdminId = 1;
            final GrantAdmin grantAdmin = GrantAdmin.builder().id(grantAdminId)
                    .funder(FundingOrganisation.builder().id(1).build()).build();

            final UUID id = UUID.randomUUID();
            when(grantAdvertRepository.findById(id)).thenThrow(new NotFoundException("error"));

            assertThrows(NotFoundException.class, () -> grantAdvertService.getAdvertById(id));
        }

    }

    @Nested
    @WithAdminSession
    class deleteGrantAdvert {

        @Test
        void Successful_deleteRequestedAdvert() {
            final UUID advertId = UUID.randomUUID();
            final Integer adminId = 1;

            when(grantAdvertRepository.deleteByIdAndSchemeEditor(advertId, adminId)).thenReturn(1);

            grantAdvertService.deleteGrantAdvert(advertId);

            verify(grantAdvertRepository).deleteByIdAndSchemeEditor(advertId, adminId);
        }

        @Test
        void NotFound_noAdvertFoundWithUUIDProvided() {
            final UUID advertId = UUID.randomUUID();

            assertThrows(NotFoundException.class, () -> grantAdvertService.deleteGrantAdvert(advertId));
        }

    }

    @Nested
    @WithAdminSession
    class getAdvertBuilderPageData {

        @Test
        void successful_successfullyGettingPageData_SummaryData() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "section-id";
            final String pageId = "page-id";
            final GrantAdvert mockGrantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().build();
            final AdvertDefinitionSection mockAdvertDefinitionSection = RandomGrantAdvertGenerators
                    .randomAdvertDefinitionSection().build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(mockGrantAdvert));

            when(advertDefinition.getSectionById(sectionId)).thenReturn(mockAdvertDefinitionSection);

            GetGrantAdvertPageResponseDTO response = grantAdvertService.getAdvertBuilderPageData(grantAdvertId,
                    sectionId, pageId);

            assertThat(response.getSectionName()).isEqualTo(mockAdvertDefinitionSection.getTitle());
            assertThat(response.getPageTitle()).isEqualTo(mockAdvertDefinitionSection.getPages().get(0).getTitle());
            assertThat(response.getStatus())
                    .isEqualTo(mockGrantAdvert.getResponse().getSections().get(0).getPages().get(0).getStatus());
        }

        @Test
        void successful_successfullyGettingPageData_SinglePageSection() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "section-id";
            final String pageId = "page-id";
            final GrantAdvert mockGrantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().build();
            final AdvertDefinitionSection mockAdvertDefinitionSection = RandomGrantAdvertGenerators
                    .randomAdvertDefinitionSection().build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(mockGrantAdvert));

            when(advertDefinition.getSectionById(sectionId)).thenReturn(mockAdvertDefinitionSection);

            GetGrantAdvertPageResponseDTO response = grantAdvertService.getAdvertBuilderPageData(grantAdvertId,
                    sectionId, pageId);

            assertThat(response.getPreviousPageId()).isNull();
            assertThat(response.getNextPageId()).isNull();
        }

        @Test
        void successful_successfullyGettingPageData_MultiplePageSection() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "section-id";
            final String pageId = "page-two-id";
            final GrantAdvert mockGrantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().build();
            final AdvertDefinitionPage mockPageOne = RandomGrantAdvertGenerators.randomAdvertDefinitionPage()
                    .title("Page One").id("page-one-id").build();
            final AdvertDefinitionPage mockPageTwo = RandomGrantAdvertGenerators.randomAdvertDefinitionPage()
                    .title("Page Two").id("page-two-id").build();
            final AdvertDefinitionPage mockPageThree = RandomGrantAdvertGenerators.randomAdvertDefinitionPage()
                    .title("Page Three").id("page-three-id").build();
            final AdvertDefinitionSection mockSectionDef = RandomGrantAdvertGenerators.randomAdvertDefinitionSection()
                    .pages(List.of(mockPageOne, mockPageTwo, mockPageThree)).build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(mockGrantAdvert));

            when(advertDefinition.getSectionById(sectionId)).thenReturn(mockSectionDef);

            GetGrantAdvertPageResponseDTO response = grantAdvertService.getAdvertBuilderPageData(grantAdvertId,
                    sectionId, pageId);

            assertThat(response.getPreviousPageId()).isEqualTo(mockPageOne.getId());
            assertThat(response.getNextPageId()).isEqualTo(mockPageThree.getId());
        }

        @Test
        void successful_successfullyGettingPageData_QuestionWithResponses() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "section-id";
            final String pageId = "page-id";
            final AdvertDefinitionQuestionValidation mockValidation = RandomGrantAdvertGenerators
                    .randomQuestionValidationGenerator().build();
            final AdvertDefinitionQuestion mockQuestion = RandomGrantAdvertGenerators.randomAdvertDefinitionQuestion()
                    .validation(mockValidation).build();
            final AdvertDefinitionPage mockPage = RandomGrantAdvertGenerators.randomAdvertDefinitionPage()
                    .questions(Collections.singletonList(mockQuestion)).build();
            final AdvertDefinitionSection mockSection = RandomGrantAdvertGenerators.randomAdvertDefinitionSection()
                    .pages(Collections.singletonList(mockPage)).build();

            final GrantAdvert mockGrantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().build();
            final GrantAdvertQuestionResponse mockQuestionResponse = mockGrantAdvert.getResponse().getSections().get(0)
                    .getPages().get(0).getQuestions().get(0);

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(mockGrantAdvert));

            when(advertDefinition.getSectionById(sectionId)).thenReturn(mockSection);

            GetGrantAdvertPageResponseDTO response = grantAdvertService.getAdvertBuilderPageData(grantAdvertId,
                    sectionId, pageId);

            assertThat(response.getQuestions()).hasSize(1);
            assertThat(response.getQuestions().get(0).getResponse()).isEqualTo(mockQuestionResponse);
            assertThat(response.getQuestions().get(0).getQuestionValidation()).isEqualTo(mockValidation);
        }

        @Test
        void successful_successfullyGettingPageData_QuestionWithoutResponses() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "section-id";
            final String pageId = "page-id";
            final GrantAdvertPageResponse mockPageResponse = RandomGrantAdvertGenerators.randomAdvertPageResponse()
                    .questions(Collections.emptyList()).build();
            final GrantAdvertSectionResponse mockSectionResponse = RandomGrantAdvertGenerators
                    .randomAdvertSectionResponse().pages(Collections.singletonList(mockPageResponse)).build();
            final GrantAdvertResponse mockResponse = RandomGrantAdvertGenerators.randomAdvertResponse()
                    .sections(Collections.singletonList(mockSectionResponse)).build();
            final GrantAdvert mockGrantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity()
                    .response(mockResponse).build();
            final AdvertDefinitionSection mockAdvertDefinitionSection = RandomGrantAdvertGenerators
                    .randomAdvertDefinitionSection().build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(mockGrantAdvert));

            when(advertDefinition.getSectionById(sectionId)).thenReturn(mockAdvertDefinitionSection);

            GetGrantAdvertPageResponseDTO response = grantAdvertService.getAdvertBuilderPageData(grantAdvertId,
                    sectionId, pageId);

            assertThat(response.getQuestions()).isNotEmpty().allMatch(question -> question.getResponse() == null);

        }

        @Test
        void forbidden_attemptingToViewGrantAdvertCreatedByAnotherUser() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "section-id";
            final String pageId = "page-id";

            when(grantAdvertRepository.findById(grantAdvertId)).thenThrow(new AccessDeniedException("Access Denied"));

            assertThrows(AccessDeniedException.class,
                    () -> grantAdvertService.getAdvertBuilderPageData(grantAdvertId, sectionId, pageId));
        }

        @Test
        void notFound_attemptingToViewPageForAdvertWhichDoesntExist() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "section-id";
            final String pageId = "page-id";

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> grantAdvertService.getAdvertBuilderPageData(grantAdvertId, sectionId, pageId));
        }

        @Test
        void notFound_attemptingToViewPageForSectionWhichDoesntExist() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "missing-section-id";
            final String pageId = "page-id";

            when(grantAdvertRepository.findById(grantAdvertId))
                    .thenReturn(Optional.of(RandomGrantAdvertGenerators.randomGrantAdvertEntity().build()));

            when(advertDefinition.getSectionById(sectionId)).thenThrow(new NotFoundException());

            assertThrows(NotFoundException.class,
                    () -> grantAdvertService.getAdvertBuilderPageData(grantAdvertId, sectionId, pageId));
        }

        @Test
        void notFound_attemptingToViewPageForPageWhichDoesntExist() {
            final UUID grantAdvertId = UUID.randomUUID();
            final String sectionId = "section-id";
            final String pageId = "missing-page-id";
            final AdvertDefinitionSection mockAdvertDefinitionSection = RandomGrantAdvertGenerators
                    .randomAdvertDefinitionSection().build();

            when(grantAdvertRepository.findById(grantAdvertId))
                    .thenReturn(Optional.of(RandomGrantAdvertGenerators.randomGrantAdvertEntity().build()));

            when(advertDefinition.getSectionById(sectionId)).thenReturn(mockAdvertDefinitionSection);

            assertThrows(NotFoundException.class,
                    () -> grantAdvertService.getAdvertBuilderPageData(grantAdvertId, sectionId, pageId));
        }

    }

    @Nested
    @WithAdminSession
    class updatePageResponse {

        UUID grantAdvertId = UUID.fromString("33bbb645-271f-4a2f-b272-8153e68a8bd7");

        String sectionId = "123";

        String pageId = "987";

        String questionId = "grantShortDescription";

        String expectedResponse = "This is a description";

        GrantAdvertPageResponse samplePageDto = GrantAdvertPageResponse.builder().id(pageId)
                .status(GrantAdvertPageResponseStatus.COMPLETED)
                .questions(Collections.singletonList(GrantAdvertQuestionResponse.builder().id(questionId).seen(true)
                        .response(expectedResponse).build()))
                .build();

        GrantAdvertPageResponseValidationDto pagePatchDto = GrantAdvertPageResponseValidationDto.builder()
                .grantAdvertId(grantAdvertId).sectionId(sectionId).page(samplePageDto).build();

        AdvertDefinitionQuestion definitionQuestion = AdvertDefinitionQuestion.builder().id(questionId)
                .responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
                .validation(AdvertDefinitionQuestionValidation.builder().mandatory(true).build()).build();

        AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id(sectionId)
                .pages(List.of(AdvertDefinitionPage.builder().questions(List.of(definitionQuestion)).build())).build();

        @Test
        void updatePageResponse_HappyPathNoDefinition() {
            final GrantAdvert advert = GrantAdvert.builder().build();

            when(grantAdvertRepository.findById(grantAdvertId))
                    .thenReturn(Optional.of(GrantAdvert.builder().id(grantAdvertId).build()));
            when(advertDefinition.getSectionById(sectionId)).thenReturn(definitionSection);
            doReturn(advert).when(grantAdvertService).save(any());

            grantAdvertService.updatePageResponse(pagePatchDto);

            ArgumentCaptor<GrantAdvert> argument = ArgumentCaptor.forClass(GrantAdvert.class);
            verify(grantAdvertService).save(argument.capture());

            GrantAdvertResponse response = argument.getValue().getResponse();
            assertThat(response).isNotNull();
            Optional<GrantAdvertSectionResponse> sectionById = response.getSectionById(sectionId);
            assertThat(sectionById).isPresent();
            assertThat(sectionById.get().getStatus()).isEqualTo(GrantAdvertSectionResponseStatus.COMPLETED);
            Optional<GrantAdvertPageResponse> pageById = sectionById.get().getPageById(pageId);
            assertThat(pageById).isPresent();
            Optional<GrantAdvertQuestionResponse> questionById = pageById.get().getQuestionById(questionId);
            assertThat(questionById).isPresent();
            assertThat(questionById.get().getResponse()).isEqualTo(expectedResponse);
        }

        @Test
        void updatePageResponse_HappyPathExistingDefinition() {
            final GrantAdvert advert = GrantAdvert.builder().build();

            ArgumentCaptor<GrantAdvert> argument = ArgumentCaptor.forClass(GrantAdvert.class);

            GrantAdvertResponse originalResponse = GrantAdvertResponse.builder()
                    .sections(Collections.singletonList(GrantAdvertSectionResponse.builder().id(sectionId)
                            .pages(Collections.singletonList(GrantAdvertPageResponse.builder().id(pageId)
                                    .status(GrantAdvertPageResponseStatus.IN_PROGRESS)
                                    .questions(Collections.singletonList(GrantAdvertQuestionResponse.builder()
                                            .id(questionId).seen(false).response("Old response").build()))
                                    .build()))
                            .build()))
                    .build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(
                    Optional.of(GrantAdvert.builder().id(grantAdvertId).response(originalResponse).build()));
            when(advertDefinition.getSectionById(sectionId)).thenReturn(definitionSection);
            doReturn(advert).when(grantAdvertService).save(any());

            grantAdvertService.updatePageResponse(pagePatchDto);

            verify(grantAdvertService).save(argument.capture());

            GrantAdvertResponse response = argument.getValue().getResponse();
            assertThat(response).isNotNull();
            Optional<GrantAdvertSectionResponse> sectionById = response.getSectionById(sectionId);
            assertThat(sectionById).isPresent();
            assertThat(sectionById.get().getStatus()).isEqualTo(GrantAdvertSectionResponseStatus.COMPLETED);
            Optional<GrantAdvertPageResponse> pageById = sectionById.get().getPageById(pageId);
            assertThat(pageById).isPresent();
            assertThat(pageById.get().getStatus()).isEqualTo(GrantAdvertPageResponseStatus.COMPLETED);
            Optional<GrantAdvertQuestionResponse> questionById = pageById.get().getQuestionById(questionId);
            assertThat(questionById).isPresent();
            assertThat(questionById.get().getResponse()).isEqualTo(expectedResponse);
            assertThat(questionById.get().getSeen()).isTrue();
        }

        @Test
        void updatePageResponse_SectionNotComplete() {
            final GrantAdvert advert = GrantAdvert.builder().build();

            ArgumentCaptor<GrantAdvert> argument = ArgumentCaptor.forClass(GrantAdvert.class);

            GrantAdvertResponse originalResponse = GrantAdvertResponse.builder()
                    .sections(Collections.singletonList(GrantAdvertSectionResponse.builder().id(sectionId)
                            .pages(Collections.singletonList(GrantAdvertPageResponse.builder().id(pageId)
                                    .status(GrantAdvertPageResponseStatus.IN_PROGRESS)
                                    .questions(Collections.singletonList(GrantAdvertQuestionResponse.builder()
                                            .id(questionId).seen(false).response("Old response").build()))
                                    .build()))
                            .build()))
                    .build();

            AdvertDefinitionSection sectionTwoQuestions = AdvertDefinitionSection.builder().id(sectionId)
                    .pages(List.of(AdvertDefinitionPage.builder().questions(List.of(definitionQuestion)).build(),
                            AdvertDefinitionPage.builder().build()))
                    .build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(
                    Optional.of(GrantAdvert.builder().id(grantAdvertId).response(originalResponse).build()));
            when(advertDefinition.getSectionById(sectionId)).thenReturn(sectionTwoQuestions);
            doReturn(advert).when(grantAdvertService).save(any());

            grantAdvertService.updatePageResponse(pagePatchDto);

            verify(grantAdvertService).save(argument.capture());

            GrantAdvertResponse response = argument.getValue().getResponse();
            assertThat(response).isNotNull();
            Optional<GrantAdvertSectionResponse> sectionById = response.getSectionById(sectionId);
            assertThat(sectionById).isPresent();
            assertThat(sectionById.get().getStatus()).isEqualTo(GrantAdvertSectionResponseStatus.IN_PROGRESS);
        }

        @Test
        void updatePageResponse_NoGrantAdvert() {

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> grantAdvertService.updatePageResponse(pagePatchDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(String.format("GrantAdvert with id %s not found", grantAdvertId));
        }

        @Test
        void updatePageResponse_DateQuestion() {

            final GrantAdvert advert = GrantAdvert.builder().build();

            String[] openingMultiResponse = new String[]{"10", "10", "2010", "13:00"};
            GrantAdvertQuestionResponse openingDateQuestion = GrantAdvertQuestionResponse.builder().id(OPENING_DATE_ID)
                    .multiResponse(openingMultiResponse).build();
            String[] closingMultiResponse = new String[]{"12", "12", "2012", "13:00"};
            GrantAdvertQuestionResponse closingDateQuestion = GrantAdvertQuestionResponse.builder().id(CLOSING_DATE_ID)
                    .multiResponse(closingMultiResponse).build();
            String[] expectedOpeningMultiResponse = new String[]{"10", "10", "2010", "13", "00"};
            String[] expectedClosingMultiResponse = new String[]{"12", "12", "2012", "13", "00"};

            GrantAdvertPageResponse datePage = GrantAdvertPageResponse.builder().id(pageId)
                    .status(GrantAdvertPageResponseStatus.COMPLETED)
                    .questions(List.of(openingDateQuestion, closingDateQuestion)).build();

            GrantAdvertPageResponseValidationDto datePagePatchDto = GrantAdvertPageResponseValidationDto.builder()
                    .grantAdvertId(grantAdvertId).sectionId(ADVERT_DATES_SECTION_ID).page(datePage).build();

            AdvertDefinitionQuestion openDateDefinitionQuestion = AdvertDefinitionQuestion.builder().id(OPENING_DATE_ID)
                    .responseType(AdvertDefinitionQuestionResponseType.DATE)
                    .validation(AdvertDefinitionQuestionValidation.builder().mandatory(true).build()).build();
            AdvertDefinitionQuestion closeDateDefinitionQuestion = AdvertDefinitionQuestion.builder()
                    .id(CLOSING_DATE_ID).responseType(AdvertDefinitionQuestionResponseType.DATE)
                    .validation(AdvertDefinitionQuestionValidation.builder().mandatory(true).build()).build();
            AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id(ADVERT_DATES_SECTION_ID)
                    .pages(List.of(AdvertDefinitionPage.builder().id(pageId)
                            .questions(List.of(openDateDefinitionQuestion, closeDateDefinitionQuestion)).build()))
                    .build();

            when(grantAdvertRepository.findById(grantAdvertId))
                    .thenReturn(Optional.of(GrantAdvert.builder().id(grantAdvertId).build()));
            when(advertDefinition.getSectionById(ADVERT_DATES_SECTION_ID)).thenReturn(definitionSection);
            doReturn(advert).when(grantAdvertService).save(any());

            grantAdvertService.updatePageResponse(datePagePatchDto);

            ArgumentCaptor<GrantAdvert> argument = ArgumentCaptor.forClass(GrantAdvert.class);
            verify(grantAdvertService).save(argument.capture());

            GrantAdvertResponse response = argument.getValue().getResponse();
            assertThat(response).isNotNull();
            Optional<GrantAdvertSectionResponse> sectionById = response.getSectionById(ADVERT_DATES_SECTION_ID);
            assertThat(sectionById).isPresent();
            assertThat(sectionById.get().getStatus()).isEqualTo(GrantAdvertSectionResponseStatus.COMPLETED);
            Optional<GrantAdvertPageResponse> pageById = sectionById.get().getPageById(pageId);
            assertThat(pageById).isPresent();
            Optional<GrantAdvertQuestionResponse> openingQuestion = pageById.get().getQuestionById(OPENING_DATE_ID);
            assertThat(openingQuestion).isPresent();
            assertThat(openingQuestion.get().getMultiResponse()).isEqualTo(expectedOpeningMultiResponse);
            Optional<GrantAdvertQuestionResponse> closingQuestion = pageById.get().getQuestionById(CLOSING_DATE_ID);
            assertThat(closingQuestion).isPresent();
            assertThat(closingQuestion.get().getMultiResponse()).isEqualTo(expectedClosingMultiResponse);
        }

        @Test
        void updatePageResponseThrowsConflictException() {
            final GrantAdvert advert = GrantAdvert.builder().status(GrantAdvertStatus.PUBLISHED).build();
            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(advert));
            GrantAdvertPageResponseValidationDto datePagePatchDto = GrantAdvertPageResponseValidationDto.builder()
                    .grantAdvertId(grantAdvertId).sectionId(ADVERT_DATES_SECTION_ID).page(samplePageDto).build();

            assertThrows(ConflictException.class, () -> grantAdvertService.updatePageResponse(datePagePatchDto));

        }
    }

    // TODO refactor this test and the underlying service methods to be more maintainable
    @Nested
    class publishAdvert {

        final UUID grantAdvertId = UUID.fromString("33bbb645-271f-4a2f-b272-8153e68a8bd7");

        final String contentfulAdvertId = "7gqb4FzwI4W22Ap3X29xsS";

        final AdvertDefinitionQuestion definitionQuestion1 = AdvertDefinitionQuestion.builder()
                .id("grantShortDescription").title("Add a short description of the grant")
                .responseType(AdvertDefinitionQuestionResponseType.LONG_TEXT).build();

        final AdvertDefinitionQuestion definitionQuestion2 = AdvertDefinitionQuestion.builder().id("grantLocation")
                .title("Where is the grant available?").responseType(AdvertDefinitionQuestionResponseType.LIST).build();

        final AdvertDefinitionQuestion definitionQuestion3 = AdvertDefinitionQuestion.builder().id("grantSummaryTab")
                .title("Add a long description of your grant")
                .responseType(AdvertDefinitionQuestionResponseType.RICH_TEXT).build();

        final AdvertDefinitionQuestion definitionQuestion4 = AdvertDefinitionQuestion.builder()
                .id("grantApplicationOpenDate").title("Opening date")
                .responseType(AdvertDefinitionQuestionResponseType.DATE).build();

        final AdvertDefinitionQuestion definitionQuestion5 = AdvertDefinitionQuestion.builder()
                .id("grantApplicationCloseDate").title("Closing date")
                .responseType(AdvertDefinitionQuestionResponseType.DATE).build();

        final AdvertDefinitionQuestion definitionQuestion6 = AdvertDefinitionQuestion.builder()
                .id("grantTotalAwardAmount").title("How much funding is available?")
                .responseType(AdvertDefinitionQuestionResponseType.CURRENCY).build();

        final AdvertDefinitionPage definitionPage1 = AdvertDefinitionPage.builder().id("1").title("Short Description")
                .questions(List.of(definitionQuestion1, definitionQuestion2, definitionQuestion3, definitionQuestion4,
                        definitionQuestion5, definitionQuestion6))
                .build();

        final AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id("grantDetails")
                .title("1. Grant Details").pages(List.of(definitionPage1)).build();

        final AdvertDefinition definition = AdvertDefinition.builder().sections(List.of(definitionSection)).build();

        // Grant Scheme
        final SchemeEntity scheme = SchemeEntity.builder().id(1).name("Homelessness Grant").funderId(1).build();

        // Grant Advert
        final GrantAdvertQuestionResponse response1 = GrantAdvertQuestionResponse.builder().id("grantShortDescription")
                .seen(true).response("A government grant toi provide funding for homelessness charities").build();

        final GrantAdvertQuestionResponse response2 = GrantAdvertQuestionResponse.builder().id("grantLocation")
                .seen(true).multiResponse(new String[] { "Scotland", "Wales" }).build();

        final GrantAdvertQuestionResponse response3 = GrantAdvertQuestionResponse.builder().id("grantSummaryTab")
                .seen(true)
                .multiResponse(new String[] { "<p>one</p><p>two</p>",
                        "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"one\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"two\",\"marks\":[],\"data\":{}}],\"data\":{}}]}" })
                .build();

        final GrantAdvertQuestionResponse response4 = GrantAdvertQuestionResponse.builder()
                .id("grantApplicationOpenDate").seen(true)
                .multiResponse(new String[] { "10", "12", "2022", "00", "01" }).build();

        final ZonedDateTime openingDate = LocalDateTime.of(2022, 12, 10, 0, 1)
                .atZone(ZoneId.of("Europe/London"));

        final GrantAdvertQuestionResponse response5 = GrantAdvertQuestionResponse.builder()
                .id("grantApplicationCloseDate").seen(true)
                .multiResponse(new String[] { "10", "12", "2023", "23", "59" }).build();

        final ZonedDateTime closingDate = LocalDateTime.of(2023, 12, 10, 23, 59)
                .atZone(ZoneId.of("Europe/London"));;

        final GrantAdvertQuestionResponse response6 = GrantAdvertQuestionResponse.builder().id("grantTotalAwardAmount")
                .response("1000000").seen(true).build();

        final GrantAdvertPageResponse page1 = GrantAdvertPageResponse.builder().id("1")
                .status(GrantAdvertPageResponseStatus.COMPLETED)
                .questions(List.of(response1, response2, response3, response4, response5, response6)).build();

        final GrantAdvertSectionResponse responseSection1 = GrantAdvertSectionResponse.builder().id("grantDetails")
                .status(GrantAdvertSectionResponseStatus.COMPLETED).pages(List.of(page1)).build();

        final GrantAdvertPageResponse datesPage = GrantAdvertPageResponse.builder().id("1")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(response4, response5)).build();

        final GrantAdvertSectionResponse responseSectionDates = GrantAdvertSectionResponse.builder()
                .id("applicationDates").status(GrantAdvertSectionResponseStatus.COMPLETED).pages(List.of(datesPage))
                .build();

        final GrantAdvertResponse response = GrantAdvertResponse.builder()
                .sections(List.of(responseSection1, responseSectionDates)).build();

        // Advert in contentful
        final String grantSummaryTabJson = new JSONObject(
                "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"one\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"two\",\"marks\":[],\"data\":{}}],\"data\":{}}]}")
                        .toString();

        final CMAEntry unpublishedContentfulAdvert = new CMAEntry().setId(contentfulAdvertId).setVersion(1)
                .setField("grantShortDescription", "en-US",
                        "A government grant toi provide funding for homelessness charities")
                .setField("grantLocation", "en-US", new String[] { "Scotland", "Wales" })
                .setField("grantSummaryTab", "en-US", grantSummaryTabJson)
                .setField("grantApplicationOpenDate", "en-Us", "2022-12-10T00:00:00")
                .setField("grantApplicationCloseDate", "en-Us", "2023-12-10T23:59:00")
                .setField("grantTotalAwardAmount", "en-US", "1000000");

        final CMAEntry publishedContentfulAdvert = new CMAEntry().setId(contentfulAdvertId).setVersion(2)
                .setField("grantShortDescription", "en-US",
                        "A government grant toi provide funding for homelessness charities")
                .setField("grantLocation", "en-US", new String[] { "Scotland", "Wales" })
                .setField("grantSummaryTab", "en-US", grantSummaryTabJson)
                .setField("grantApplicationOpenDate", "en-Us", "2022-12-10T00:00:00")
                .setField("grantApplicationCloseDate", "en-Us", "2023-12-10T23:59:00")
                .setField("grantTotalAwardAmount", "en-US", 1000000)
                .setField("grantTotalAwardDisplay", "en-US", "£1 million");

        @Test
        @WithAdminSession
        void publishAdvert_successfullyPublishedAdvert() {

            final GrantAdvert mockGrantAdvert = GrantAdvert.builder().id(UUID.randomUUID()).scheme(scheme).version(1)
                    .created(Instant.now()).createdBy(new GrantAdmin(1, null, null, new ArrayList<>())).lastUpdated(Instant.now())
                    .lastUpdatedBy(new GrantAdmin(1, null, null, new ArrayList<>())).status(GrantAdvertStatus.DRAFT)
                    .contentfulEntryId("entry-id").contentfulSlug("contentful-slug")
                    .grantAdvertName("Grant Advert Name").response(response).grantAdvertName("Homelessness Grant")
                    .build();

            when(advertDefinition.getSections()).thenReturn(definition.getSections());

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(mockGrantAdvert));

            when(contentfulManagementClient.entries()).thenReturn(contentfulEntries);

            when(mockedFetchQuery.withContentType(any())).thenReturn(mockedFetchQuery);

            when(mockedFetchQuery.where(any(), any(), any())).thenReturn(mockedFetchQuery);

            when(contentfulDeliveryClient.fetch(any())).thenReturn(mockedFetchQuery);

            when(mockedFetchQuery.all()).thenReturn(mockCDAArray);

            when(contentfulEntries.create(Mockito.eq("grantDetails"), Mockito.any()))
                    .thenReturn(unpublishedContentfulAdvert);

            when(contentfulEntries.fetchOne(contentfulAdvertId)).thenReturn(publishedContentfulAdvert);

            when(contentfulEntries.async()).thenReturn(async);

            doReturn(mockGrantAdvert).when(grantAdvertService).save(any());

            final WebClient webClient = mock(WebClient.class);
            final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
            final WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
            final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            when(webClientBuilder.build()).thenReturn(webClient);
            when(webClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

            final ArgumentCaptor<CMAEntry> entryCaptor = ArgumentCaptor.forClass(CMAEntry.class);

            final ArgumentCaptor<GrantAdvert> grantAdvertArgumentCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            grantAdvertService.publishAdvert(grantAdvertId);

            verify(grantAdvertService).save(grantAdvertArgumentCaptor.capture());

            GrantAdvert savedAdvert = grantAdvertArgumentCaptor.getValue();

            assertThat(savedAdvert.getFirstPublishedDate()).isNotNull();

            assertThat(savedAdvert.getOpeningDate()).isEqualTo(openingDate);

            assertThat(savedAdvert.getClosingDate()).isEqualTo(closingDate);

            // verify values before we save
            verify(contentfulEntries).create(eq("grantDetails"), entryCaptor.capture());

            final CMAEntry capturedBeforeSave = entryCaptor.getValue();

            assertThat(capturedBeforeSave.getId()).isNull();
            assertThat(capturedBeforeSave.getVersion()).isNull();

            verify(webClient).patch();
            verify(requestBodyUriSpec).uri(anyString());
            verify(requestBodyUriSpec).headers(any());
            verify(requestBodyUriSpec).bodyValue(any());
            verify(requestHeadersSpec).retrieve();
            verify(responseSpec).bodyToMono(Void.class);

            // verify that we've refreshed the data after adding RTF data
            verify(contentfulEntries).fetchOne(contentfulAdvertId);

            // verify that we've published
            verify(async).publish(eq(publishedContentfulAdvert), any());
        }

        @Test
        @WithAdminSession
        void publishAdvert_updatesExistingAdvert_IfFirstPublishedDateHasBeenSet() {

            final GrantAdvert grantAvertInDatabase = GrantAdvert.builder().id(UUID.randomUUID()).scheme(scheme)
                    .version(1).created(Instant.now()).createdBy(new GrantAdmin(1, null, null, new ArrayList<>()))
                    .lastUpdated(Instant.now()).lastUpdatedBy(new GrantAdmin(1, null, null, new ArrayList<>()))
                    .status(GrantAdvertStatus.DRAFT).contentfulEntryId(contentfulAdvertId)
                    .contentfulSlug("contentful-slug").grantAdvertName("Grant Advert Name").response(response)
                    .grantAdvertName("Homelessness Grant")
                    .firstPublishedDate(LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC)).build();

            when(advertDefinition.getSections()).thenReturn(definition.getSections());
            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(grantAvertInDatabase));
            when(contentfulManagementClient.entries()).thenReturn(contentfulEntries);
            when(mockedFetchQuery.withContentType(any())).thenReturn(mockedFetchQuery);
            when(mockedFetchQuery.where(any(), any(), any())).thenReturn(mockedFetchQuery);
            when(contentfulDeliveryClient.fetch(any())).thenReturn(mockedFetchQuery);
            when(mockedFetchQuery.all()).thenReturn(mockCDAArray);
            when(contentfulEntries.update(Mockito.any())).thenReturn(publishedContentfulAdvert);
            when(contentfulEntries.fetchOne(contentfulAdvertId)).thenReturn(publishedContentfulAdvert,
                    publishedContentfulAdvert);
            when(contentfulEntries.async()).thenReturn(async);
            doReturn(grantAvertInDatabase).when(grantAdvertService).save(any());

            final WebClient webClient = mock(WebClient.class);
            final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
            final WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
            final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            when(webClientBuilder.build()).thenReturn(webClient);
            when(webClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

            final ArgumentCaptor<GrantAdvert> grantAdvertArgumentCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            grantAdvertService.publishAdvert(grantAdvertId);

            verify(grantAdvertService).save(grantAdvertArgumentCaptor.capture());

            final GrantAdvert savedAdvert = grantAdvertArgumentCaptor.getValue();

            assertThat(savedAdvert.getFirstPublishedDate()).isNotNull();
            assertThat(savedAdvert.getOpeningDate()).isEqualTo(openingDate);
            assertThat(savedAdvert.getClosingDate()).isEqualTo(closingDate);

            verify(webClient).patch();
            verify(requestBodyUriSpec).uri(anyString());
            verify(requestBodyUriSpec).headers(any());
            verify(requestBodyUriSpec).bodyValue(any());
            verify(requestHeadersSpec).retrieve();
            verify(responseSpec).bodyToMono(Void.class);

            verify(contentfulEntries).update(publishedContentfulAdvert);

            // verify that we've refreshed the data after adding RTF data
            verify(contentfulEntries, atLeastOnce()).fetchOne(contentfulAdvertId);

            // verify that we've published
            verify(async).publish(eq(publishedContentfulAdvert), any());
        }

        @Test
        void publishAdvertThroughLambda_successfullyPublishedAdvert() {
            final GrantAdvert mockGrantAdvert = GrantAdvert.builder().id(UUID.randomUUID()).scheme(scheme).version(1)
                    .created(Instant.now()).createdBy(new GrantAdmin(1, null, null, new ArrayList<>())).lastUpdated(Instant.now())
                    .lastUpdatedBy(new GrantAdmin(1, null, null, new ArrayList<>())).status(GrantAdvertStatus.DRAFT)
                    .contentfulEntryId("entry-id").contentfulSlug("contentful-slug")
                    .grantAdvertName("Grant Advert Name").response(response).grantAdvertName("Homelessness Grant")
                    .build();

            final WebClient webClient = mock(WebClient.class);
            final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
            final WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
            final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            when(webClientBuilder.build()).thenReturn(webClient);
            when(webClient.patch()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
            doReturn(mockGrantAdvert).when(grantAdvertService).save(any());

            when(advertDefinition.getSections()).thenReturn(definition.getSections());

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(mockGrantAdvert));

            when(contentfulManagementClient.entries()).thenReturn(contentfulEntries);

            when(mockedFetchQuery.withContentType(any())).thenReturn(mockedFetchQuery);

            when(mockedFetchQuery.where(any(), any(), any())).thenReturn(mockedFetchQuery);

            when(contentfulDeliveryClient.fetch(any())).thenReturn(mockedFetchQuery);

            when(mockedFetchQuery.all()).thenReturn(mockCDAArray);

            when(contentfulEntries.create(Mockito.eq("grantDetails"), Mockito.any()))
                    .thenReturn(unpublishedContentfulAdvert);

            when(contentfulEntries.fetchOne(contentfulAdvertId)).thenReturn(publishedContentfulAdvert);

            when(contentfulEntries.async()).thenReturn(async);

            final ArgumentCaptor<CMAEntry> entryCaptor = ArgumentCaptor.forClass(CMAEntry.class);

            final ArgumentCaptor<GrantAdvert> grantAdvertArgumentCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            grantAdvertService.publishAdvert(grantAdvertId);

            verify(grantAdvertService).save(grantAdvertArgumentCaptor.capture());

            GrantAdvert savedAdvert = grantAdvertArgumentCaptor.getValue();

            assertThat(savedAdvert.getFirstPublishedDate()).isNotNull();

            assertThat(savedAdvert.getOpeningDate()).isEqualTo(openingDate);

            assertThat(savedAdvert.getClosingDate()).isEqualTo(closingDate);

            // verify values before we save
            verify(contentfulEntries).create(eq("grantDetails"), entryCaptor.capture());

            final CMAEntry capturedBeforeSave = entryCaptor.getValue();

            assertThat(capturedBeforeSave.getId()).isNull();
            assertThat(capturedBeforeSave.getVersion()).isNull();

            verify(webClient).patch();
            verify(requestBodyUriSpec).uri(anyString());
            verify(requestBodyUriSpec).headers(any());
            verify(requestBodyUriSpec).bodyValue(any());
            verify(requestHeadersSpec).retrieve();
            verify(responseSpec).bodyToMono(Void.class);

            // verify that we've refreshed the data after adding RTF data
            verify(contentfulEntries).fetchOne(contentfulAdvertId);

            // verify that we've published
            verify(async).publish(eq(publishedContentfulAdvert), any());
        }

    }

    @Nested
    class unpublishAdvert {

        final UUID grantAdvertId = UUID.fromString("fa8f4b1d-d090-4ff6-97be-ccabd3b1d87d");

        final String contentfulAdvertId = "7gqb4FzwI4W22Ap3X29xsS";

        final GrantAdvert grantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().id(grantAdvertId)
                .contentfulEntryId(contentfulAdvertId).build();

        @Mock
        final CMAEntry contentfulAdvert = new CMAEntry().setId(contentfulAdvertId).setVersion(2);
        @Test
        @WithAdminSession
        void unpublishAdvert_UnpublishesAdvertFromContentful_AndSetsStatusToDraftInDb() {
            final GrantAdvert advert = GrantAdvert.builder()
                    .id(grantAdvertId)
                    .status(GrantAdvertStatus.PUBLISHED)
                    .build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(grantAdvert));
            when(contentfulManagementClient.entries()).thenReturn(contentfulEntries);
            when(contentfulEntries.fetchOne(contentfulAdvertId)).thenReturn(contentfulAdvert);
            when(contentfulEntries.unPublish(contentfulAdvert)).thenReturn(contentfulAdvert);
            doReturn(advert).when(grantAdvertService).save(any());
            when(contentfulAdvert.isPublished()).thenReturn(true);

            final ArgumentCaptor<GrantAdvert> advertCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            // maybe overkill to check this here but ensures we can be sure the state has changed
            assertThat(grantAdvert.getUnpublishedDate()).isNull();

            grantAdvertService.unpublishAdvert(grantAdvertId);

            verify(contentfulEntries).unPublish(contentfulAdvert);
            verify(openSearchService).removeIndexEntry(contentfulAdvert);
            verify(grantAdvertService).save(advertCaptor.capture());

            assertThat(advertCaptor.getValue().getId()).isEqualTo(grantAdvertId);
            assertThat(advertCaptor.getValue().getStatus()).isEqualTo(GrantAdvertStatus.DRAFT);
            assertThat(advertCaptor.getValue().getUnpublishedDate()).isNotNull();
            assertThat(advertCaptor.getValue().getContentfulSlug()).isNull();
        }

        @Test
        void unpublishAdvertThroughLambda_successfullyUnpublishedAdvert() {
            final GrantAdvert advert = GrantAdvert.builder()
                    .id(grantAdvertId)
                    .status(GrantAdvertStatus.PUBLISHED)
                    .build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(grantAdvert));
            when(contentfulManagementClient.entries()).thenReturn(contentfulEntries);
            when(contentfulEntries.fetchOne(contentfulAdvertId)).thenReturn(contentfulAdvert);
            when(contentfulEntries.unPublish(contentfulAdvert)).thenReturn(contentfulAdvert);
            doReturn(advert).when(grantAdvertService).save(any());
            when(contentfulAdvert.isPublished()).thenReturn(true);

            final ArgumentCaptor<GrantAdvert> advertCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            // maybe overkill to check this here but ensures we can be sure the state has changed
            assertThat(grantAdvert.getUnpublishedDate()).isNull();

            grantAdvertService.unpublishAdvert(grantAdvertId);

            verify(contentfulEntries).unPublish(contentfulAdvert);
            verify(openSearchService).removeIndexEntry(contentfulAdvert);
            verify(grantAdvertService).save(advertCaptor.capture());

            assertThat(advertCaptor.getValue().getId()).isEqualTo(grantAdvertId);
            assertThat(advertCaptor.getValue().getStatus()).isEqualTo(GrantAdvertStatus.DRAFT);
            assertThat(advertCaptor.getValue().getUnpublishedDate()).isNotNull();
            assertThat(advertCaptor.getValue().getContentfulSlug()).isNull();
        }

    }

    @Nested
    @WithAdminSession
    class getGrantAdvertStatusBySchemeId {

        @Test
        void getGrantAdvertStatusBySchemeId_HappyPath() {

            GrantAdvert grantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().build();

            when(grantAdvertRepository.findBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(Optional.of(grantAdvert));

            GetGrantAdvertStatusResponseDTO actualOutput = grantAdvertService
                    .getGrantAdvertStatusBySchemeId(SAMPLE_SCHEME_ID);

            assertThat(actualOutput.getGrantAdvertId()).isEqualTo(grantAdvert.getId());
            assertThat(actualOutput.getGrantAdvertStatus()).isEqualTo(grantAdvert.getStatus());

            verify(grantAdvertMapper).grantAdvertStatusResponseDtoFromGrantAdvert(grantAdvert);
        }

        @Test
        void getGrantAdvertStatusBySchemeId_GrantAdvertNotFound() {

            when(grantAdvertRepository.findBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(Optional.empty());

            NotFoundException thrown = assertThrows(NotFoundException.class,
                    () -> grantAdvertService.getGrantAdvertStatusBySchemeId(SAMPLE_SCHEME_ID));

            assertThat(thrown.getMessage())
                    .isEqualTo("Grant Advert for Scheme with id " + SAMPLE_SCHEME_ID + " does not exist");
        }

        @Test
        void getGrantAdvertStatusBySchemeId_NotEnoughPermissions() {

            when(grantAdvertRepository.findBySchemeId(SAMPLE_SCHEME_ID)).thenThrow(AccessDeniedException.class);

            assertThrows(AccessDeniedException.class,
                    () -> grantAdvertService.getGrantAdvertStatusBySchemeId(SAMPLE_SCHEME_ID));

        }

    }

    @Nested
    @WithAdminSession
    class getGrantAdvertPublishingInformationBySchemeId {

        @Test
        void getGrantAdvertPublishingInformationBySchemeId_HappyPath() {

            String testEmailAddress = "Test-email@grants.com";

            GrantAdvert grantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().build();

            when(grantAdvertRepository.findBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(Optional.of(grantAdvert));

            when(userService.getEmailAddressForSub(any())).thenReturn(testEmailAddress.getBytes());

            GetGrantAdvertPublishingInformationResponseDTO actualOutput = grantAdvertService
                    .getGrantAdvertPublishingInformationBySchemeId(SAMPLE_SCHEME_ID);

            assertThat(actualOutput.getGrantAdvertId()).isEqualTo(grantAdvert.getId());
            assertThat(actualOutput.getGrantAdvertStatus()).isEqualTo(grantAdvert.getStatus());
            assertThat(actualOutput.getContentfulSlug()).isEqualTo(grantAdvert.getContentfulSlug());
            assertThat(actualOutput.getFirstPublishedDate()).isEqualTo(grantAdvert.getFirstPublishedDate());
            assertThat(actualOutput.getClosingDate()).isEqualTo(grantAdvert.getClosingDate());
            assertThat(actualOutput.getOpeningDate()).isEqualTo(grantAdvert.getOpeningDate());
            assertThat(actualOutput.getUnpublishedDate()).isEqualTo(grantAdvert.getUnpublishedDate());
            assertThat(actualOutput.getLastPublishedDate()).isEqualTo(grantAdvert.getLastPublishedDate());
            assertThat(actualOutput.getLastUpdatedByEmail()).isEqualTo(testEmailAddress.getBytes());

            verify(grantAdvertMapper).grantAdvertPublishInformationResponseDtoFromGrantAdvert(grantAdvert);
        }

        @Test
        void getGrantAdvertPublishingInformationBySchemeId_WebClientResponseException() {
            GrantAdvert grantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().build();

            when(grantAdvertRepository.findBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(Optional.of(grantAdvert));

            doThrow(WebClientResponseException.class).when(userService).getEmailAddressForSub(any());

           assertThrows(WebClientResponseException.class,
                    () -> grantAdvertService.getGrantAdvertPublishingInformationBySchemeId(SAMPLE_SCHEME_ID));
        }

        @Test
        void getGrantAdvertPublishInformationBySchemeId_GrantAdvertNotFound() {

            when(grantAdvertRepository.findBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(Optional.empty());

            NotFoundException thrown = assertThrows(NotFoundException.class,
                    () -> grantAdvertService.getGrantAdvertPublishingInformationBySchemeId(SAMPLE_SCHEME_ID));

            assertThat(thrown.getMessage())
                    .isEqualTo("Grant Advert for Scheme with id " + SAMPLE_SCHEME_ID + " does not exist");
        }

        @Test
        void getGrantAdvertPublishInformationBySchemeId_NotEnoughPermissions() {

            when(grantAdvertRepository.findBySchemeId(SAMPLE_SCHEME_ID)).thenThrow(AccessDeniedException.class);

            assertThrows(AccessDeniedException.class,
                    () -> grantAdvertService.getGrantAdvertPublishingInformationBySchemeId(SAMPLE_SCHEME_ID));

        }

    }

    @Nested
    @WithAdminSession
    class scheduleGrantAdvert {

        final UUID grantAdvertId = UUID.fromString("5b30cb45-7339-466a-a700-270c3983c604");

        final GrantAdvertQuestionResponse response4 = GrantAdvertQuestionResponse.builder()
                .id("grantApplicationOpenDate").seen(true).multiResponse(new String[] { "10", "12", "2022", "0", "1" })
                .build();

        final ZonedDateTime openingDate = LocalDateTime.of(2022, 12, 10, 0, 1)
                .atZone(ZoneId.of("Europe/London"));

        final GrantAdvertQuestionResponse response5 = GrantAdvertQuestionResponse.builder()
                .id("grantApplicationCloseDate").seen(true)
                .multiResponse(new String[] { "10", "12", "2023", "23", "59" }).build();

        final ZonedDateTime closingDate = LocalDateTime.of(2023, 12, 10, 23, 59)
                .atZone(ZoneId.of("Europe/London"));

        final GrantAdvertPageResponse datesPage = GrantAdvertPageResponse.builder().id("1")
                .status(GrantAdvertPageResponseStatus.COMPLETED).questions(List.of(response4, response5)).build();

        final GrantAdvertSectionResponse responseSectionDates = GrantAdvertSectionResponse.builder()
                .id("applicationDates").status(GrantAdvertSectionResponseStatus.COMPLETED).pages(List.of(datesPage))
                .build();

        final GrantAdvertResponse response = GrantAdvertResponse.builder().sections(List.of(responseSectionDates))
                .build();

        final GrantAdvert scheduledGrantAdvert = GrantAdvert.builder().id(grantAdvertId).response(response)
                .status(GrantAdvertStatus.DRAFT).grantAdvertName("Schedule Test Advert")
                .createdBy(new GrantAdmin(1, null, null, new ArrayList<>())).build();

        @Test
        void scheduleGrantAdvert_Success() {

            final GrantAdvert advert = GrantAdvert.builder()
                    .status(GrantAdvertStatus.SCHEDULED)
                    .build();

            doReturn(advert).when(grantAdvertService).save(any());

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(scheduledGrantAdvert));

            final ArgumentCaptor<GrantAdvert> grantAdvertArgumentCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            grantAdvertService.scheduleGrantAdvert(grantAdvertId);

            verify(grantAdvertService).save(grantAdvertArgumentCaptor.capture());

            GrantAdvert savedAdvert = grantAdvertArgumentCaptor.getValue();

            assertThat(savedAdvert.getStatus()).isEqualTo(GrantAdvertStatus.SCHEDULED);

            assertThat(savedAdvert.getOpeningDate()).isEqualTo(openingDate);

            assertThat(savedAdvert.getClosingDate()).isEqualTo(closingDate);

        }

        @Test
        void scheduleGrantAdvert_NotFound() {
            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> grantAdvertService.scheduleGrantAdvert(grantAdvertId))
                    .isInstanceOf(NotFoundException.class).hasMessage("Advert with id " + grantAdvertId + " not found");

        }

    }

    @Nested
    @WithAdminSession
    class unscheduleGrantAdvert {

        final UUID grantAdvertId = UUID.fromString("5b30cb45-7339-466a-a700-270c3983c604");

        final GrantAdvertResponse response = GrantAdvertResponse.builder().build();

        final GrantAdvert scheduledGrantAdvert = GrantAdvert.builder().id(grantAdvertId)
                .status(GrantAdvertStatus.SCHEDULED).grantAdvertName("Scheduled Test Advert")
                .createdBy(new GrantAdmin(1, null, null, new ArrayList<>())).build();

        @Test
        void scheduleGrantAdvert_Success() {
            final GrantAdvert advert = GrantAdvert.builder()
                    .status(GrantAdvertStatus.UNSCHEDULED)
                    .build();

            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.of(scheduledGrantAdvert));
            doReturn(advert).when(grantAdvertService).save(any());

            final ArgumentCaptor<GrantAdvert> grantAdvertArgumentCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            grantAdvertService.unscheduleGrantAdvert(grantAdvertId);

            verify(grantAdvertService).save(grantAdvertArgumentCaptor.capture());

            GrantAdvert savedAdvert = grantAdvertArgumentCaptor.getValue();

            assertThat(savedAdvert.getStatus()).isEqualTo(GrantAdvertStatus.UNSCHEDULED);
        }

        @Test
        void scheduleGrantAdvert_NotFound() {
            when(grantAdvertRepository.findById(grantAdvertId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> grantAdvertService.unscheduleGrantAdvert(grantAdvertId))
                    .isInstanceOf(NotFoundException.class).hasMessage("Advert with id " + grantAdvertId + " not found");
        }

    }

    @Test
    void testRemoveAdminReferenceBySchemeId() {
        GrantAdmin grantAdmin = GrantAdmin.builder().id(SAMPLE_SCHEME_ID).build();


        GrantAdvert grantAdvert = RandomGrantAdvertGenerators.randomGrantAdvertEntity().build();


        when(grantAdvertRepository.findBySchemeId(anyInt())).thenReturn(Optional.of(grantAdvert));

        grantAdvertService.removeAdminReferenceBySchemeId(grantAdmin, 1);

        verify(grantAdvertRepository).save(grantAdvert);
    }


    @Test
    void patchCreatedByUpdatesGrantAdvert() {
        final UUID grantAdvertId = UUID.fromString("5b30cb45-7339-466a-a700-270c3983c604");
        final GrantAdmin testAdmin = GrantAdmin.builder().id(1).build();
        final GrantAdmin patchedAdmin = GrantAdmin.builder().id(2).build();
        GrantAdvert testGrantAdvert = GrantAdvert.builder().id(grantAdvertId).createdBy(testAdmin).build();
        GrantAdvert patchedGrantAdvert = GrantAdvert.builder().id(grantAdvertId).createdBy(patchedAdmin).build();

        when(grantAdvertRepository.findBySchemeId(1))
                .thenReturn(Optional.of(testGrantAdvert));
        when(grantAdminRepository.findById(2))
                .thenReturn(Optional.of(patchedAdmin));
        doReturn(patchedGrantAdvert)
                .when(grantAdvertService).save(any());

        grantAdvertService.updateAdvertOwner(2, 1);

        assertThat(testGrantAdvert.getCreatedBy()).isEqualTo(patchedGrantAdvert.getCreatedBy());
    }

    @Test
    void patchCreatedByDoesNothingIfSchemeIsNotPresent() {
        GrantAdvertServiceTest.this.grantAdvertService.updateAdvertOwner(2, 2);

        verify(grantAdvertRepository, never()).save(any());
    }


    @Nested
    class save {

        @WithAdminSession
        @Test
        void save_WithAdminSession_Success() {
            final GrantAdmin grantAdmin = GrantAdmin.builder()
                    .id(1)
                    .build();
            final String now = "2024-02-27T10:15:30Z";
            final SchemeEntity scheme = SchemeEntity.builder()
                    .grantAdmins(List.of(grantAdmin))
                    .build();
            final GrantAdvert advertToSave = GrantAdvert.builder()
                    .scheme(scheme)
                    .build();

            final ArgumentCaptor<GrantAdvert> advertCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            when(grantAdminRepository.findByGapUserUserSub(any()))
                    .thenReturn(Optional.of(grantAdmin));
            when(Instant.now(clock))
                    .thenReturn(Instant.parse(now));
            when(grantAdvertRepository.save(any()))
                    .thenReturn(advertToSave);

            grantAdvertService.save(advertToSave);

            verify(grantAdvertRepository).save(advertCaptor.capture());

            final GrantAdvert capturedAdvert = advertCaptor.getValue();

            // the @WithAdminSession annotation running in this context will provide an admin ID of 1...trust me bro
            assertThat(capturedAdvert.getLastUpdated()).isEqualTo(Instant.parse(now));
            assertThat(capturedAdvert.getLastUpdatedBy().getId()).isEqualTo(1);
            assertThat(capturedAdvert.getScheme().getLastUpdatedBy()).isEqualTo(1);
            assertThat(capturedAdvert.getScheme().getLastUpdated()).isEqualTo(Instant.parse(now));
        }

        @Test
        void save_WithoutAdminSession_Success() {
            final SchemeEntity scheme = SchemeEntity.builder().build();
            final GrantAdvert advertToSave = GrantAdvert.builder()
                    .scheme(scheme)
                    .build();
            final GrantAdmin grantAdmin = GrantAdmin.builder()
                    .id(1)
                    .build();

            final ArgumentCaptor<GrantAdvert> advertCaptor = ArgumentCaptor.forClass(GrantAdvert.class);

            try (MockedStatic<HelperUtils> mockedHelperUtils = mockStatic(HelperUtils.class)) {
                when(grantAdminRepository.findByGapUserUserSub(any()))
                        .thenReturn(Optional.of(grantAdmin));
                when(grantAdvertRepository.save(any()))
                        .thenReturn(advertToSave);
                mockedHelperUtils.when(HelperUtils::getAdminSessionForAuthenticatedUser)
                        .thenReturn(null);

                grantAdvertService.save(advertToSave);

                verify(grantAdvertRepository).save(advertCaptor.capture());

                final GrantAdvert capturedAdvert = advertCaptor.getValue();

                assertThat(capturedAdvert.getLastUpdated()).isNull();
                assertThat(capturedAdvert.getLastUpdatedBy()).isNull();
                assertThat(capturedAdvert.getScheme().getLastUpdatedBy()).isNull();
                assertThat(capturedAdvert.getScheme().getLastUpdated()).isNull();
            }
        }
    }
}
