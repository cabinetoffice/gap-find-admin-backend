package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.*;
import gov.cabinetoffice.gap.adminbackend.dtos.application.questions.QuestionGenericPatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.ConflictException;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ApplicationFormMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ApplicationFormMapperImpl;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.TemplateApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.testdata.projectionimpls.TestApplicationFormsFoundView;
import gov.cabinetoffice.gap.adminbackend.utils.ApplicationFormUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.*;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators.randomApplicationFormEntity;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators.randomApplicationFormFound;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@WithAdminSession
class ApplicationFormServiceTest {

    @Spy
    private ApplicationFormMapper applicationFormMapper = new ApplicationFormMapperImpl();

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private TemplateApplicationFormRepository templateApplicationFormRepository;

    @Mock
    private ApplicationFormRepository applicationFormRepository;

    @Mock
    private SchemeRepository schemeRepository;

    @Mock
    private OdtService odtService;

    @Mock
    private SessionsService sessionsService;

    @Spy
    @InjectMocks
    private ApplicationFormService applicationFormService;

    @Nested
    class saveApplicationForm {

        @Test
        void saveApplicationFormHappyPathTest_SchemeVersion1() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            when(templateApplicationFormRepository.findById(1))
                    .thenReturn(Optional.of(SAMPLE_TEMPLATE_APPLICATION_FORM_ENTITY));
            doReturn(SAMPLE_APPLICATION_FORM_ENTITY)
                    .when(applicationFormService).save(any());

            final SchemeDTO schemeDTO = SchemeDTO.builder().version("1").build();

            final GenericPostResponseDTO genericPostResponseDTO =
                    applicationFormService.saveApplicationForm(new ApplicationFormPostDTO(SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME), schemeDTO);

            verify(applicationFormService).save(argument.capture());
            assertThat(genericPostResponseDTO.getId())
                    .as("Verify the id returned matches the id of the entity returned by the repository after save")
                    .isEqualTo(SAMPLE_APPLICATION_ID);
            assertThat(argument.getValue().getApplicationName())
                    .as("Verify that application name has been mapped from original DTO")
                    .isEqualTo(SAMPLE_APPLICATION_NAME);
            assertThat(argument.getValue().getDefinition().getSections())
                    .as("Verify that the sections have been mapped from the template").asList()
                    .hasSizeGreaterThanOrEqualTo(1);
            assertThat(argument.getValue().getVersion()).as("Verify that version is 1").isEqualTo(1);

        }

        @Test
        void saveApplicationFormHappyPathTest_SchemeVersion2() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            when(templateApplicationFormRepository.findById(1))
                    .thenReturn(Optional.of(SAMPLE_TEMPLATE_APPLICATION_FORM_ENTITY));
            doReturn(SAMPLE_APPLICATION_FORM_ENTITY)
                    .when(applicationFormService).save(any());
            final SchemeDTO schemeDTO = SchemeDTO.builder().version("2").build();

            final GenericPostResponseDTO genericPostResponseDTO =
                    applicationFormService.saveApplicationForm(new ApplicationFormPostDTO(SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME), schemeDTO);

            verify(applicationFormService).save(argument.capture());
            assertThat(genericPostResponseDTO.getId())
                    .as("Verify the id returned matches the id of the entity returned by the repository after save")
                    .isEqualTo(SAMPLE_APPLICATION_ID);
            assertThat(argument.getValue().getApplicationName())
                    .as("Verify that application name has been mapped from original DTO")
                    .isEqualTo(SAMPLE_APPLICATION_NAME);
            assertThat(argument.getValue().getDefinition().getSections())
                    .as("Verify that the sections have been mapped from the template").asList()
                    .hasSizeGreaterThanOrEqualTo(1);
            assertThat(argument.getValue().getVersion()).as("Verify that version is 2").isEqualTo(2);

        }

        @Test
        void saveApplicationFormUnhappyPathTest_TemplateNotFoundTest() {
            final ApplicationFormPostDTO dto = new ApplicationFormPostDTO(SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME);

            when(templateApplicationFormRepository.findById(any()))
                    .thenReturn(Optional.empty());
            final SchemeDTO schemeDTO = SchemeDTO.builder().version("1").build();

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService.saveApplicationForm(dto, schemeDTO))
                            .as("Could not retrieve template application form")
                            .isInstanceOf(ApplicationFormException.class);
        }

        @Test
        void saveApplicationFormUnhappyPathTest_SaveFailsTest() {
            final ApplicationFormPostDTO dto = new ApplicationFormPostDTO(SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME);

            when(templateApplicationFormRepository.findById(any()))
                    .thenReturn(Optional.of(SAMPLE_TEMPLATE_APPLICATION_FORM_ENTITY));
            when(ApplicationFormServiceTest.this.applicationFormRepository.save(any()))
                    .thenThrow(new RuntimeException("Generic save fails"));
            final SchemeDTO schemeDTO = SchemeDTO.builder().version("1").build();

            assertThatThrownBy(() -> applicationFormService.saveApplicationForm(dto, schemeDTO))
                    .isInstanceOf(ApplicationFormException.class)
                    .hasMessage("Could save application form with name " + SAMPLE_APPLICATION_NAME);
        }
    }

    @Nested
    class checkIfApplicationFormExists {

        private final Integer grantAdminId = 1;

        private final Integer schemeId = 333;

        @Test
        void repositoryFindsApplicationForm() {
            ApplicationFormsFoundView applicationFoundView = new TestApplicationFormsFoundView(1, 0, 0);
            List<ApplicationFormsFoundView> applicationFoundViewList = Collections.singletonList(applicationFoundView);
            ApplicationFormsFoundDTO applicationFormFoundDTO = randomApplicationFormFound().build();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository
                    .findMatchingApplicationForm(null, null, this.schemeId))
                    .thenReturn(applicationFoundViewList);
            List<ApplicationFormsFoundDTO> response = ApplicationFormServiceTest.this.applicationFormService
                    .getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP);
            assertThat(response).asList().hasSize(1);
            assertThat(response.get(0)).isEqualTo(applicationFormFoundDTO);
        }

        @Test
        void noApplicationFormFoundInRepository() {
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository
                    .findMatchingApplicationForm(null, null, this.schemeId))
                    .thenReturn(Collections.emptyList());
            List<ApplicationFormsFoundDTO> response = ApplicationFormServiceTest.this.applicationFormService
                    .getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP);
            assertThat(response).asList().isEmpty();
        }

        @Test
        void multipleApplicationFormsFoundInRepository() {
            ApplicationFormsFoundView applicationFoundView = new TestApplicationFormsFoundView(1, 0, 0);
            ApplicationFormsFoundView secondApplicationFoundView = new TestApplicationFormsFoundView(2, 0, 1);
            List<ApplicationFormsFoundView> applicationFoundViewList = new LinkedList<>(
                    Arrays.asList(applicationFoundView, secondApplicationFoundView));

            ApplicationFormsFoundDTO applicationFormFoundDTO = randomApplicationFormFound().build();
            ApplicationFormsFoundDTO secondApplicationFormFoundDTO = randomApplicationFormFound().applicationId(2)
                    .submissionCount(1).build();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository
                    .findMatchingApplicationForm(null, null, this.schemeId))
                    .thenReturn(applicationFoundViewList);

            List<ApplicationFormsFoundDTO> response = ApplicationFormServiceTest.this.applicationFormService
                    .getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP);
            assertThat(response).asList().hasSize(2);
            assertThat(response.get(0)).isEqualTo(applicationFormFoundDTO);
            assertThat(response.get(1)).isEqualTo(secondApplicationFormFoundDTO);
        }

    }

    @Nested
    class retrieveApplicationFormSummary {

        @Test
        void retrieveApplicationFormSummaryHappyPathTest() {
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));
            ApplicationFormDTO applicationFormDTO = ApplicationFormServiceTest.this.applicationFormService
                    .retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, true, true);

            assertThat(applicationFormDTO.getGrantApplicationId())
                    .isEqualTo(SAMPLE_APPLICATION_FORM_ENTITY.getGrantApplicationId());
            assertThat(applicationFormDTO.getApplicationName())
                    .isEqualTo(SAMPLE_APPLICATION_FORM_ENTITY.getApplicationName());
        }

        @Test
        void retrieveApplicationFormSummaryNotFoundTest() {
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, true, true))
                            .isInstanceOf(ApplicationFormException.class)
                            .hasMessage("No application found with id " + SAMPLE_APPLICATION_ID);
        }

        @Test
        void retrieveApplicationFormSummaryNoQuestionsHappyPathTest() {
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_SECTIONS_NO_QUESTIONS));
            ApplicationFormDTO applicationFormDTO = ApplicationFormServiceTest.this.applicationFormService
                    .retrieveApplicationFormSummary(SAMPLE_APPLICATION_ID, true, false);

            applicationFormDTO.getSections().forEach(section -> assertThat(section.getQuestions() == null));
        }

    }

    @Nested
    class deleteApplicationForm {

        @Test
        void deleteApplicationFormHappyPathTest() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationEntity));

            Mockito.doNothing().when(ApplicationFormServiceTest.this.applicationFormRepository)
                    .delete(testApplicationEntity);

            ApplicationFormServiceTest.this.applicationFormService.deleteApplicationForm(applicationId);
            Mockito.verify(ApplicationFormServiceTest.this.applicationFormRepository).delete(testApplicationEntity);

        }

        @Test
        void deleteApplicationFormApplicationNotFoundTest() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();

            Mockito.doThrow(new EntityNotFoundException("No application found with id " + applicationId))
                    .when(ApplicationFormServiceTest.this.applicationFormRepository).findById(applicationId);

            assertThatThrownBy(
                    () -> ApplicationFormServiceTest.this.applicationFormService.deleteApplicationForm(applicationId))
                            .hasMessage("No application found with id " + applicationId)
                            .isInstanceOf(EntityNotFoundException.class);
        }

    }

    @Nested
    class patchQuestionsValues {

        private MockedStatic<ApplicationFormUtils> utilMock;

        @BeforeEach
        void setupUtilsMock() {
            this.utilMock = mockStatic(ApplicationFormUtils.class);
        }

        @AfterEach
        void closeUtilsMock() {
            this.utilMock.close();
        }

        @Test
        void patchQuestionValuesGenericHappyPathTest() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            when(applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            doReturn(SAMPLE_APPLICATION_FORM_ENTITY)
                    .when(applicationFormService).save(any());

            applicationFormService.patchQuestionValues(SAMPLE_APPLICATION_ID,
                    SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, SAMPLE_QUESTION_GENERIC_PATCH_DTO, new MockHttpSession());

            verify(applicationFormService).save(argument.capture());
            ApplicationFormQuestionDTO updatedQuestion = argument.getValue().getDefinition()
                    .getSectionById(SAMPLE_SECTION_ID).getQuestionById(SAMPLE_QUESTION_ID);

            assertThat(updatedQuestion.getFieldTitle()).isEqualTo(SAMPLE_UPDATED_FIELD_TITLE);

            this.utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), eq(false)));
        }

        @Test
        void patchQuestionValuesOptionsHappyPathTest() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            when(applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_SECOND_APPLICATION_FORM_ENTITY));

            doReturn(SAMPLE_APPLICATION_FORM_ENTITY)
                    .when(applicationFormService).save(any());

            applicationFormService.patchQuestionValues(SAMPLE_APPLICATION_ID,
                    SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, SAMPLE_QUESTION_OPTIONS_PATCH_DTO, new MockHttpSession());

            Mockito.verify(applicationFormService).save(argument.capture());
            ApplicationFormQuestionDTO updatedQuestion = argument.getValue().getDefinition()
                    .getSectionById(SAMPLE_SECTION_ID).getQuestionById(SAMPLE_QUESTION_ID);

            assertThat(updatedQuestion.getOptions()).isEqualTo(SAMPLE_UPDATED_OPTIONS);

            this.utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), eq(false)));
        }

        @Test
        void patchQuestionValuesApplicationNotFoundPathTest() {
            final HttpSession session = new MockHttpSession();
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService.patchQuestionValues(
                    SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, SAMPLE_QUESTION_GENERIC_PATCH_DTO, session))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

        @Test
        void patchQuestionValuesValidationFailedPathTest() {
            final HttpSession session = new MockHttpSession();
            final ApplicationFormQuestionDTO dto =  new ApplicationFormQuestionDTO();

            when(applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));
            when(applicationFormMapper.questionDtoToQuestionGenericPatch(any()))
                    .thenCallRealMethod();
            when(validator.validate(new QuestionGenericPatchDTO()))
                    .thenCallRealMethod();

            assertThatThrownBy(() -> applicationFormService.patchQuestionValues(
                    SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, dto, session))
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Nested
    class addNewQuestion {

        private MockedStatic<ApplicationFormUtils> utilMock;

        @BeforeEach
        void setupUtilsMock() {
            this.utilMock = mockStatic(ApplicationFormUtils.class);
        }

        @AfterEach
        void closeUtilsMock() {
            this.utilMock.close();
        }

        @Test
        void addNewQuestionValuesGenericHappyPathTest() {
            final ApplicationFormEntity form = ApplicationFormEntity.builder().build();
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            when(applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));
            doReturn(form).when(applicationFormService).save(any());

            String questionId = applicationFormService.addQuestionToApplicationForm(
                    SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_GENERIC_POST_DTO, new MockHttpSession());

            verify(applicationFormService).save(argument.capture());
            ApplicationFormQuestionDTO newQuestion = argument.getValue().getDefinition()
                    .getSectionById(SAMPLE_SECTION_ID).getQuestionById(questionId);

            assertThat(newQuestion.getFieldTitle()).isEqualTo(SAMPLE_QUESTION_FIELD_TITLE);

            try {
                UUID.fromString(questionId);
            }
            catch (Exception e) {
                fail("Returned id was was not a UUID");
            }

            this.utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), eq(false)));

        }

        @Test
        void addNewQuestionValuesOptionsHappyPathTest() {
            final ApplicationFormEntity form = ApplicationFormEntity.builder().build();
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            Mockito.when(applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));
            doReturn(form).when(applicationFormService).save(any());

            String questionId = applicationFormService.addQuestionToApplicationForm(
                    SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_OPTIONS_POST_DTO, new MockHttpSession());

            verify(applicationFormService).save(argument.capture());
            ApplicationFormQuestionDTO newQuestion = argument.getValue().getDefinition()
                    .getSectionById(SAMPLE_SECTION_ID).getQuestionById(questionId);

            assertThat(newQuestion.getFieldTitle()).isEqualTo(SAMPLE_QUESTION_FIELD_TITLE);
            assertThat(newQuestion.getOptions()).asList().isNotEmpty();

            try {
                UUID.fromString(questionId);
            }
            catch (Exception e) {
                fail("Returned id was was not a UUID");
            }

            this.utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), eq(false)));
        }

        @Test
        void addNewQuestionValuesApplicationNotFoundTest() {
            final HttpSession session = new MockHttpSession();
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .addQuestionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                            SAMPLE_QUESTION_GENERIC_POST_DTO, session))
                                    .isInstanceOf(NotFoundException.class)
                                    .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

        @Test
        void addNewQuestionGenericValidationFailedPathTest() {
            final HttpSession session = new MockHttpSession();
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .addQuestionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                            SAMPLE_QUESTION_GENERIC_INVALID_POST_DTO, session))
                                    .isInstanceOf(ConstraintViolationException.class)
                                    .hasMessage("fieldTitle: Question title can not be less than 2 characters");

        }

        @Test
        void addNewQuestionOptionsValidationFailedPathTest() {
            final HttpSession session = new MockHttpSession();
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .addQuestionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                            SAMPLE_QUESTION_OPTIONS_INVALID_POST_DTO, session))
                                    .isInstanceOf(ConstraintViolationException.class)
                                    .hasMessage("options: You must have a minimum of two options");

        }

        @Test
        void addNewQuestionOptionValueValidationFailedPathTest() {
            final HttpSession session = new MockHttpSession();
            when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> applicationFormService
                    .addQuestionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                            SAMPLE_QUESTION_OPTIONS_CONTENT_INVALID_POST_DTO, session))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessage("options[2].<list element>: Enter an option");
        }
    }

    @Nested
    class deleteQuestion {

        @Test
        void deleteQuestionHappyPathTest() {
            MockedStatic<ApplicationFormUtils> utilMock = mockStatic(ApplicationFormUtils.class);

            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            final ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = testApplicationFormEntity.getDefinition().getSections().get(0).getSectionId();
            final String questionId = testApplicationFormEntity.getDefinition().getSections().get(0).getQuestions()
                    .get(0).getQuestionId();

            when(applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));
            doReturn(testApplicationFormEntity)
                    .when(applicationFormService).save(any());

            applicationFormService.deleteQuestionFromSection(applicationId, sectionId,
                    questionId, SAMPLE_VERSION);

            verify(applicationFormService).save(argument.capture());
            List<ApplicationFormQuestionDTO> questions = argument.getValue().getDefinition().getSectionById(sectionId)
                    .getQuestions();

            boolean sectionExists = questions.stream()
                    .anyMatch(question -> Objects.equals(question.getQuestionId(), questionId));

            assertThat(sectionExists).isFalse();

            utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), eq(false)));
            utilMock.close();
        }

        @Test
        void deleteQuestionApplicationNotFoundTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, SAMPLE_VERSION))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

        @Test
        void deleteQuestionSectionNotFound() {
            String incorrectId = "incorrectId";

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_DELETE_SECTION));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, incorrectId, SAMPLE_QUESTION_ID, SAMPLE_VERSION))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Section with id " + incorrectId + " does not exist");

        }

        @Test
        void deleteQuestionQuestionNotFound() {
            String incorrectId = "incorrectId";

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_DELETE_SECTION));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, incorrectId, SAMPLE_VERSION))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Question with id " + incorrectId + " does not exist");

        }

        @Test
        void deleteQuestionQuestionVersionConflict() {
            String incorrectId = "incorrectId";

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_DELETE_SECTION));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, 2))
                            .isInstanceOf(ConflictException.class)
                            .hasMessage("MULTIPLE_EDITORS");

        }

    }

    @Nested
    class getQuestion {

        @Test
        void getQuestionHappyPathTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            ApplicationFormQuestionDTO question = ApplicationFormServiceTest.this.applicationFormService
                    .retrieveQuestion(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID);

            assertThat(question.getQuestionId()).isEqualTo(SAMPLE_QUESTION_ID);
            assertThat(question.getFieldTitle()).isEqualTo(SAMPLE_QUESTION_FIELD_TITLE);

        }

        @Test
        void getQuestionQuestionNotFoundTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .retrieveQuestion(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, "differentId"))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Question with id differentId does not exist");

        }

        @Test
        void getQuestionSectionNotFoundTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .retrieveQuestion(SAMPLE_APPLICATION_ID, "differentId", SAMPLE_QUESTION_ID))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Section with id differentId does not exist");

        }

        @Test
        void getQuestionApplicationNotFoundTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .retrieveQuestion(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

    }

    @Nested
    class patchApplicationForm {

        @Test
        void successfullyPatchApplicationForm() {

            MockedStatic<ApplicationFormUtils> utilMock = mockStatic(ApplicationFormUtils.class);

            final ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final ApplicationFormEntity patchedApplicationFormEntity = randomApplicationFormEntity()
                    .applicationStatus(ApplicationStatusEnum.PUBLISHED).build();

            when(applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));
            doReturn(patchedApplicationFormEntity)
                    .when(applicationFormService).save(any());

            applicationFormService.patchApplicationForm(applicationId,
                    SAMPLE_PATCH_APPLICATION_DTO, false);

            verify(applicationFormRepository).findById(applicationId);
            verify(applicationFormMapper).updateApplicationEntityFromPatchDto(SAMPLE_PATCH_APPLICATION_DTO, testApplicationFormEntity);

            utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), eq(false)));
            utilMock.close();
        }

        @Test
        void attemptingToPatchApplicationFormThatCantBeFound() {
            when(applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationFormService
                    .patchApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_PATCH_APPLICATION_DTO, false))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist.");
        }

        @Test
        void attemptingToPatchApplicationFormUnableToSave() {
            final ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final ApplicationFormEntity patchedApplicationFormEntity = randomApplicationFormEntity()
                    .applicationStatus(ApplicationStatusEnum.PUBLISHED).build();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.save(patchedApplicationFormEntity))
                    .thenThrow(new RuntimeException());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .patchApplicationForm(applicationId, SAMPLE_PATCH_APPLICATION_DTO, false))
                            .isInstanceOf(ApplicationFormException.class)
                            .hasMessage("Error occurred when patching application with id of " + applicationId);
        }

    }

    @Nested
    class retrieveApplicationsFromScheme {

        @Test
        void applicationsArePresent() {
            ApplicationFormEntity applicationFormEntity = new ApplicationFormEntity();
            when(applicationFormRepository.findByGrantSchemeId(SAMPLE_SCHEME_ID))
                    .thenReturn(Optional.of(applicationFormEntity));
            Optional<ApplicationFormEntity> response = applicationFormService
                    .getOptionalApplicationFromSchemeId(SAMPLE_SCHEME_ID);
            assertThat(response).contains(applicationFormEntity);
        }

        @Test
        void applicationsAreNotPresent() {
            when(applicationFormRepository.findByGrantSchemeId(SAMPLE_SCHEME_ID)).thenReturn(Optional.empty());
            Optional<ApplicationFormEntity> response = applicationFormService
                    .getOptionalApplicationFromSchemeId(SAMPLE_SCHEME_ID);
            assertThat(response).isEmpty();
        }

    }

    @Test
    void patchCreatedByUpdatesApplication() {
        ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().grantSchemeId(1).createdBy(1)
                .build();
        ApplicationFormEntity patchedApplicationFormEntity = randomApplicationFormEntity().grantSchemeId(1).createdBy(2)
                .build();
        Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findByGrantSchemeId(1))
                .thenReturn(Optional.of(testApplicationFormEntity));
        Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.save(testApplicationFormEntity))
                .thenReturn(patchedApplicationFormEntity);

        ApplicationFormServiceTest.this.applicationFormService.updateApplicationOwner(2, 1);

        assertThat(testApplicationFormEntity.getCreatedBy()).isEqualTo(2);
    }

    @Test
    void patchCreatedByDoesNothingIfAdminIdIsNotFound() {
        ApplicationFormServiceTest.this.applicationFormService.updateApplicationOwner(2, 2);

        verify(applicationFormRepository, never()).save(any());
    }

    @Test
    void getLastUpdatedByByReturnsLastUpdatedByForApplication() {
        ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().lastUpdateBy(2).build();
        Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(1))
                .thenReturn(Optional.of(testApplicationFormEntity));

        ApplicationFormEntity applicationForm = ApplicationFormServiceTest.this.applicationFormService
                .getApplicationById(1);

        assertThat(applicationForm).isEqualTo(testApplicationFormEntity);
    }

    @Test
    void getLastUpdatedByReturnsNFEIfNoApplicationFound() {
        Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService.getApplicationById(1)).isInstanceOf(NotFoundException.class)
                .hasMessage("Application with id 1 does not exist");
    }



    @Nested
    class updateQuestionOrder {

        @Test
        void updateQuestionOrderSuccessful() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();
            List<ApplicationFormSectionDTO> sections = new ArrayList<>(
                    List.of(ApplicationFormSectionDTO.builder().sectionId("Section1").questions(
                            new ArrayList<>(List.of(
                                    ApplicationFormQuestionDTO.builder().questionId("Question1").build(),
                                    ApplicationFormQuestionDTO.builder().questionId("Question2").build(),
                                    ApplicationFormQuestionDTO.builder().questionId("Question3").build(),
                                    ApplicationFormQuestionDTO.builder().questionId("Question4").build()
                            ))
                    ).build())
            );
            testApplicationFormEntity.getDefinition().setSections(sections);

            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = testApplicationFormEntity.getDefinition().getSections().get(0).getSectionId();
            final String questionId = testApplicationFormEntity.getDefinition().getSections().get(0).getQuestions().get(1).getQuestionId();
            final Integer increment = 1;

            when(applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            doReturn(testApplicationFormEntity)
                    .when(applicationFormService).save(any());

            applicationFormService.updateQuestionOrder(applicationId, sectionId, questionId, increment, SAMPLE_VERSION);

            verify(applicationFormService).save(argument.capture());
        }

        @Test
        void updateQuestionOrderOutsideOfRange() {
            ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();
            List<ApplicationFormSectionDTO> sections = new ArrayList<>(
                    List.of(ApplicationFormSectionDTO.builder().sectionId("Section1").questions(
                            new ArrayList<>(List.of(
                                    ApplicationFormQuestionDTO.builder().questionId("Question1").build(),
                                    ApplicationFormQuestionDTO.builder().questionId("Question2").build(),
                                    ApplicationFormQuestionDTO.builder().questionId("Question3").build(),
                                    ApplicationFormQuestionDTO.builder().questionId("Question4").build()
                            ))
                    ).build())
            );
            testApplicationFormEntity.getDefinition().setSections(sections);

            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = testApplicationFormEntity.getDefinition().getSections().get(0).getSectionId();
            final String questionId = testApplicationFormEntity.getDefinition().getSections().get(0).getQuestions().get(0).getQuestionId();
            final Integer increment = -1;

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .updateQuestionOrder(applicationId, sectionId, questionId, increment, SAMPLE_VERSION))
                    .isInstanceOf(FieldViolationException.class)
                    .hasMessage("Question is already at the top");
        }

        @Test
        void updateQuestionOrderUnauthorised() {
            final ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().createdBy(2).build();
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = "test-section-id";
            final String questionId = "test-question-id";
            final Integer increment = 1;

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .updateQuestionOrder(applicationId, sectionId, questionId, increment, SAMPLE_VERSION))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Application with id " + applicationId + " does not exist or insufficient permissions");
        }

        @Test
        void updateQuestionOrderOutdatedVersion() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();

            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final Integer increment = 1;

            when(applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            doReturn(testApplicationFormEntity).when(applicationFormService).save(any());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .updateQuestionOrder(applicationId, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, increment, SAMPLE_VERSION - 1))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("MULTIPLE_EDITORS");
        }

    }

    @Nested
    class getApplicationStatus {

        @Test
        void getApplicationStatusSuccessful() {
            final Integer applicationId =1;
            final ApplicationStatusEnum expectedStatus = ApplicationStatusEnum.PUBLISHED;
            ApplicationFormEntity applicationForm = ApplicationFormEntity.builder()
                    .grantApplicationId(applicationId)
                    .applicationStatus(expectedStatus)
                    .build();

            when(applicationFormRepository.findById(anyInt())).thenReturn(Optional.of(applicationForm));

            final ApplicationStatusEnum response = applicationFormService.getApplicationStatus(applicationId);

            verify(applicationFormRepository).findById(applicationId);
            assertThat(response).isEqualTo(expectedStatus);

        }

        @Test
        void getApplicationStatusNotFoundException() {
            final Integer applicationId = 1;
            when(applicationFormRepository.findById(anyInt())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> applicationFormService.getApplicationStatus(applicationId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Application with id " + applicationId + " does not exist");

        }

    }

    @Nested
    class getApplicationFormExport {
        @Test
        void getApplicationFormExportSuccessful() throws Exception {
            final Integer applicationId = 1;
            final Integer grantSchemeId = 1;
            ApplicationFormEntity applicationForm = ApplicationFormEntity.builder()
                    .grantApplicationId(applicationId)
                    .grantSchemeId(grantSchemeId)
                    .build();
            SchemeEntity scheme = SchemeEntity.builder()
                    .id(grantSchemeId)
                    .build();

            when(applicationFormRepository.findById(anyInt())).thenReturn(Optional.of(applicationForm));
            when(schemeRepository.findById(anyInt())).thenReturn(Optional.of(scheme));
            OdfTextDocument odfTextDocument = OdfTextDocument.newTextDocument();
            when(odtService.generateSingleOdt(scheme, applicationForm)).thenReturn(odfTextDocument);

            final OdfTextDocument response = applicationFormService.getApplicationFormExport(applicationId);

            verify(applicationFormRepository).findById(applicationId);
            verify(schemeRepository).findById(applicationId);
            assertThat(response).isInstanceOf(OdfTextDocument.class);

        }
    }
}
