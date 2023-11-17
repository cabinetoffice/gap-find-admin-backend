package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormPostDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormQuestionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormsFoundDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormsFoundView;
import gov.cabinetoffice.gap.adminbackend.dtos.application.questions.QuestionGenericPatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ApplicationFormMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ApplicationFormMapperImpl;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.TemplateApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.testdata.projectionimpls.TestApplicationFormsFoundView;
import gov.cabinetoffice.gap.adminbackend.utils.ApplicationFormUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_APPLICATION_FORM_ENTITY;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_APPLICATION_FORM_ENTITY_DELETE_SECTION;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_APPLICATION_FORM_ENTITY_SECTIONS_NO_QUESTIONS;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_APPLICATION_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_APPLICATION_NAME;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_PATCH_APPLICATION_DTO;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_FIELD_TITLE;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_GENERIC_INVALID_POST_DTO;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_GENERIC_PATCH_DTO;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_GENERIC_POST_DTO;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_OPTIONS_CONTENT_INVALID_POST_DTO;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_OPTIONS_INVALID_POST_DTO;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_OPTIONS_PATCH_DTO;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_QUESTION_OPTIONS_POST_DTO;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_SCHEME_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_SECOND_APPLICATION_FORM_ENTITY;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_SECTION_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_TEMPLATE_APPLICATION_FORM_ENTITY;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_UPDATED_FIELD_TITLE;
import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.SAMPLE_UPDATED_OPTIONS;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators.randomApplicationFormEntity;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators.randomApplicationFormFound;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private SessionsService sessionsService;

    @InjectMocks
    private ApplicationFormService applicationFormService;

    @Nested
    class saveApplicationForm {

        @Test
        void saveApplicationFormHappyPathTest_SchemeVersion1() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            Mockito.when(ApplicationFormServiceTest.this.templateApplicationFormRepository.findById(1))
                    .thenReturn(Optional.of(SAMPLE_TEMPLATE_APPLICATION_FORM_ENTITY));
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.save(argument.capture()))
                    .thenReturn(SAMPLE_APPLICATION_FORM_ENTITY);
            final SchemeDTO schemeDTO = SchemeDTO.builder().version("1").build();

            final GenericPostResponseDTO genericPostResponseDTO = ApplicationFormServiceTest.this.applicationFormService
                    .saveApplicationForm(new ApplicationFormPostDTO(SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME),
                            schemeDTO);

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

            Mockito.when(ApplicationFormServiceTest.this.templateApplicationFormRepository.findById(1))
                    .thenReturn(Optional.of(SAMPLE_TEMPLATE_APPLICATION_FORM_ENTITY));
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.save(argument.capture()))
                    .thenReturn(SAMPLE_APPLICATION_FORM_ENTITY);
            final SchemeDTO schemeDTO = SchemeDTO.builder().version("2").build();

            final GenericPostResponseDTO genericPostResponseDTO = ApplicationFormServiceTest.this.applicationFormService
                    .saveApplicationForm(new ApplicationFormPostDTO(SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME),
                            schemeDTO);

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
            Mockito.when(ApplicationFormServiceTest.this.templateApplicationFormRepository.findById(any()))
                    .thenReturn(Optional.empty());
            final SchemeDTO schemeDTO = SchemeDTO.builder().version("1").build();
            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService.saveApplicationForm(
                    new ApplicationFormPostDTO(SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME), schemeDTO))
                            .as("Could not retrieve template application form")
                            .isInstanceOf(ApplicationFormException.class);

        }

        @Test
        void saveApplicationFormUnhappyPathTest_SaveFailsTest() {
            Mockito.when(ApplicationFormServiceTest.this.templateApplicationFormRepository.findById(any()))
                    .thenReturn(Optional.of(SAMPLE_TEMPLATE_APPLICATION_FORM_ENTITY));
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.save(any()))
                    .thenThrow(new RuntimeException("Generic save fails"));
            final SchemeDTO schemeDTO = SchemeDTO.builder().version("1").build();
            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService.saveApplicationForm(
                    new ApplicationFormPostDTO(SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME), schemeDTO))
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
                    .findMatchingApplicationForm(this.grantAdminId, null, null, this.schemeId))
                    .thenReturn(applicationFoundViewList);
            List<ApplicationFormsFoundDTO> response = ApplicationFormServiceTest.this.applicationFormService
                    .getMatchingApplicationFormsIds(SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP);
            assertThat(response).asList().hasSize(1);
            assertThat(response.get(0)).isEqualTo(applicationFormFoundDTO);
        }

        @Test
        void noApplicationFormFoundInRepository() {
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository
                    .findMatchingApplicationForm(this.grantAdminId, null, null, this.schemeId))
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
                    .findMatchingApplicationForm(this.grantAdminId, null, null, this.schemeId))
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

        @Test
        void retrieveApplicationFormSummary_AccessDenied() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationEntity));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .retrieveApplicationFormSummary(applicationId, true, true))
                            .isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);
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

        @Test
        void deleteApplicationForminsufficientPermissionsToDeleteThisApplication() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationEntity));

            assertThatThrownBy(
                    () -> ApplicationFormServiceTest.this.applicationFormService.deleteApplicationForm(applicationId))
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId)
                            .isInstanceOf(AccessDeniedException.class);
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

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            ApplicationFormServiceTest.this.applicationFormService.patchQuestionValues(SAMPLE_APPLICATION_ID,
                    SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, SAMPLE_QUESTION_GENERIC_PATCH_DTO);

            Mockito.verify(ApplicationFormServiceTest.this.applicationFormRepository).save(argument.capture());
            ApplicationFormQuestionDTO updatedQuestion = argument.getValue().getDefinition()
                    .getSectionById(SAMPLE_SECTION_ID).getQuestionById(SAMPLE_QUESTION_ID);

            assertThat(updatedQuestion.getFieldTitle()).isEqualTo(SAMPLE_UPDATED_FIELD_TITLE);

            this.utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));

        }

        @Test
        void patchQuestionValuesOptionsHappyPathTest() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_SECOND_APPLICATION_FORM_ENTITY));

            ApplicationFormServiceTest.this.applicationFormService.patchQuestionValues(SAMPLE_APPLICATION_ID,
                    SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, SAMPLE_QUESTION_OPTIONS_PATCH_DTO);

            Mockito.verify(ApplicationFormServiceTest.this.applicationFormRepository).save(argument.capture());
            ApplicationFormQuestionDTO updatedQuestion = argument.getValue().getDefinition()
                    .getSectionById(SAMPLE_SECTION_ID).getQuestionById(SAMPLE_QUESTION_ID);

            assertThat(updatedQuestion.getOptions()).isEqualTo(SAMPLE_UPDATED_OPTIONS);

            this.utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));
        }

        @Test
        void patchQuestionValuesApplicationNotFoundPathTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService.patchQuestionValues(
                    SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, SAMPLE_QUESTION_GENERIC_PATCH_DTO))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

        @Test
        void patchQuestionValuesValidationFailedPathTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));
            Mockito.when(ApplicationFormServiceTest.this.applicationFormMapper.questionDtoToQuestionGenericPatch(any()))
                    .thenCallRealMethod();
            Mockito.when(ApplicationFormServiceTest.this.validator.validate(new QuestionGenericPatchDTO()))
                    .thenCallRealMethod();

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService.patchQuestionValues(
                    SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, new ApplicationFormQuestionDTO()))
                            .isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        void patchQuestionValuesApplication_AccessDeniedTest() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationEntity));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService.patchQuestionValues(
                    applicationId, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID, SAMPLE_QUESTION_GENERIC_PATCH_DTO))
                            .isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);

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
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            String questionId = ApplicationFormServiceTest.this.applicationFormService.addQuestionToApplicationForm(
                    SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_GENERIC_POST_DTO, new MockHttpSession());

            Mockito.verify(ApplicationFormServiceTest.this.applicationFormRepository).save(argument.capture());
            ApplicationFormQuestionDTO newQuestion = argument.getValue().getDefinition()
                    .getSectionById(SAMPLE_SECTION_ID).getQuestionById(questionId);

            assertThat(newQuestion.getFieldTitle()).isEqualTo(SAMPLE_QUESTION_FIELD_TITLE);

            try {
                UUID.fromString(questionId);
            }
            catch (Exception e) {
                fail("Returned id was was not a UUID");
            }

            this.utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));

        }

        @Test
        void addNewQuestionValuesOptionsHappyPathTest() {
            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            String questionId = ApplicationFormServiceTest.this.applicationFormService.addQuestionToApplicationForm(
                    SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_OPTIONS_POST_DTO, new MockHttpSession());

            Mockito.verify(ApplicationFormServiceTest.this.applicationFormRepository).save(argument.capture());
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

            this.utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));
        }

        @Test
        void addNewQuestionValuesApplicationNotFoundTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .addQuestionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                            SAMPLE_QUESTION_GENERIC_POST_DTO, new MockHttpSession()))
                                    .isInstanceOf(NotFoundException.class)
                                    .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

        @Test
        void addNewQuestionGenericValidationFailedPathTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .addQuestionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                            SAMPLE_QUESTION_GENERIC_INVALID_POST_DTO, new MockHttpSession()))
                                    .isInstanceOf(ConstraintViolationException.class)
                                    .hasMessage("fieldTitle: Question title can not be less than 2 characters");

        }

        @Test
        void addNewQuestionOptionsValidationFailedPathTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .addQuestionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                            SAMPLE_QUESTION_OPTIONS_INVALID_POST_DTO, new MockHttpSession()))
                                    .isInstanceOf(ConstraintViolationException.class)
                                    .hasMessage("options: You must have a minimum of two options");

        }

        @Test
        void addNewQuestionOptionValueValidationFailedPathTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .addQuestionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID,
                            SAMPLE_QUESTION_OPTIONS_CONTENT_INVALID_POST_DTO, new MockHttpSession()))
                                    .isInstanceOf(ConstraintViolationException.class)
                                    .hasMessage("options[2].<list element>: Enter an option");
        }

        @Test
        void addNewQuestionValues_AccessDeniedTest() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationEntity));

            assertThatThrownBy(
                    () -> ApplicationFormServiceTest.this.applicationFormService.addQuestionToApplicationForm(
                            applicationId, SAMPLE_SECTION_ID, SAMPLE_QUESTION_GENERIC_POST_DTO, new MockHttpSession()))
                                    .isInstanceOf(AccessDeniedException.class).hasMessage(
                                            "User 1 is unable to access the application form with id " + applicationId);

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

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            ApplicationFormServiceTest.this.applicationFormService.deleteQuestionFromSection(applicationId, sectionId,
                    questionId);

            Mockito.verify(ApplicationFormServiceTest.this.applicationFormRepository).save(argument.capture());
            List<ApplicationFormQuestionDTO> questions = argument.getValue().getDefinition().getSectionById(sectionId)
                    .getQuestions();

            boolean sectionExists = questions.stream()
                    .anyMatch(question -> Objects.equals(question.getQuestionId(), questionId));

            assertThat(sectionExists).isFalse();

            utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));
            utilMock.close();
        }

        @Test
        void deleteQuestionApplicationNotFoundTest() {

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SAMPLE_QUESTION_ID))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

        @Test
        void deleteQuestionSectionNotFound() {
            String incorrectId = "incorrectId";

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_DELETE_SECTION));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, incorrectId, SAMPLE_QUESTION_ID))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Section with id " + incorrectId + " does not exist");

        }

        @Test
        void deleteQuestionQuestionNotFound() {
            String incorrectId = "incorrectId";

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_DELETE_SECTION));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .deleteQuestionFromSection(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, incorrectId))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Question with id " + incorrectId + " does not exist");

        }

        @Test
        void deleteQuestionQuestion_AccessDenied() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();
            String sectionId = "test-section-id";
            String questionId = "test-question-id";

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationEntity));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .deleteQuestionFromSection(applicationId, sectionId, questionId))
                            .isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);

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

        @Test
        void getQuestion_AccessDeniedTest() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();
            String sectionId = "test-section-id";
            String questionId = "test-question-id";

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationEntity));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .retrieveQuestion(applicationId, sectionId, questionId)).isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);

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

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.save(patchedApplicationFormEntity))
                    .thenReturn(patchedApplicationFormEntity);

            ApplicationFormServiceTest.this.applicationFormService.patchApplicationForm(applicationId,
                    SAMPLE_PATCH_APPLICATION_DTO, false);

            verify(ApplicationFormServiceTest.this.applicationFormRepository).findById(applicationId);
            verify(ApplicationFormServiceTest.this.applicationFormMapper)
                    .updateApplicationEntityFromPatchDto(SAMPLE_PATCH_APPLICATION_DTO, testApplicationFormEntity);
            verify(ApplicationFormServiceTest.this.applicationFormRepository).save(patchedApplicationFormEntity);

            utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));
            utilMock.close();

        }

        @Test
        void attemptingToPatchApplicationFormThatCantBeFound() {
            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .patchApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_PATCH_APPLICATION_DTO, eq(false)))
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
                            .hasMessage("Error occured when patching appliction with id of " + applicationId);
        }

        @Test
        void attemptingToPatchApplicationForm_AccessDenied() {
            ApplicationFormEntity testApplicationEntity = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationEntity.getGrantApplicationId();

            Mockito.when(ApplicationFormServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationEntity));

            assertThatThrownBy(() -> ApplicationFormServiceTest.this.applicationFormService
                    .patchApplicationForm(applicationId, SAMPLE_PATCH_APPLICATION_DTO, false))
                            .isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);
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
            assertThat(response.get()).isEqualTo(applicationFormEntity);
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

        ApplicationFormServiceTest.this.applicationFormService.patchCreatedBy(2, 1);

        assertThat(testApplicationFormEntity.getCreatedBy()).isEqualTo(2);
    }

    @Test
    void patchCreatedByDoesNothingIfAdminIdIsNotFound() {
        ApplicationFormServiceTest.this.applicationFormService.patchCreatedBy(2, 2);

        verify(applicationFormRepository, never()).save(any());
    }

}
