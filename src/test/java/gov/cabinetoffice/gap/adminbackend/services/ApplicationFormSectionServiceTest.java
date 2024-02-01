package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.PostSectionDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ApplicationFormMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.ApplicationFormMapperImpl;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.TemplateApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.utils.ApplicationFormUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.validation.Validation;
import javax.validation.Validator;

import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.testdata.ApplicationFormTestData.*;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.EmptySectionApplicationFormGenerators.emptySectionApplicationFormGenerator;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators.randomApplicationFormEntity;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@SpringJUnitConfig
@WithAdminSession
class ApplicationFormSectionServiceTest {

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
    private ApplicationFormSectionService applicationFormSectionService;

    @Nested
    class getSectionById {

        @Test
        void successfullyGettingSectionIdFromApplication() {
            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            ApplicationFormSectionDTO section = ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .getSectionById(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, true);

            assertThat(section).isEqualTo(SAMPLE_SECTION);
        }

        @Test
        void successfullyGettingSectionIdNoQuestionsFromApplication() {
            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_NO_QUESTIONS));

            ApplicationFormSectionDTO section = ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .getSectionById(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, false);

            assertThat(section.getQuestions()).isNull();
        }

        @Test
        void noApplicationFormFoundWithIDProvided() {
            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .getSectionById(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, true))
                            .isInstanceOf(NotFoundException.class);
        }

        @Test
        void noSectionFoundWithIDProvided() {
            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .getSectionById(SAMPLE_APPLICATION_ID, "WRONG_ID", true)).isInstanceOf(NotFoundException.class)
                            .hasMessage("Section with id WRONG_ID does not exist");
        }

        @Test
        void multipleSectionsFoundWithSameID() {
            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_WITH_DUPLICATE_SECTIONS));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .getSectionById(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, true))
                            .isInstanceOf(ApplicationFormException.class)
                            .hasMessage("Ambiguous reference - more than one section with id " + SAMPLE_SECTION_ID);
        }

        @Test
        void insufficientPermissionsToAccessThisSection() {
            ApplicationFormEntity testApplicationForm = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationForm.getGrantApplicationId();
            String sectionId = "test-section-id";

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationForm));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .getSectionById(applicationId, sectionId, true)).isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);
        }

    }

    @Nested
    class addNewSection {

        @Test
        void addNewSectionHappyPathTest() {
            MockedStatic<ApplicationFormUtils> utilMock = mockStatic(ApplicationFormUtils.class);

            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            String sectionId = ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .addSectionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_POST_SECTION);

            Mockito.verify(ApplicationFormSectionServiceTest.this.applicationFormRepository).save(argument.capture());
            ApplicationFormSectionDTO newSection = argument.getValue().getDefinition().getSectionById(sectionId);

            assertThat(newSection.getSectionTitle()).isEqualTo(SAMPLE_NEW_SECTION_TITLE);
            assertThat(newSection.getQuestions()).asList().isEmpty();

            try {
                UUID.fromString(sectionId);
            }
            catch (Exception e) {
                fail("Returned id was was not a UUID");
            }

            utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));
            utilMock.close();
        }

        @Test
        void addNewSectionApplicationNotFoundTest() {

            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .addSectionToApplicationForm(SAMPLE_APPLICATION_ID, SAMPLE_POST_SECTION))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

        @Test
        void addNewSectionDuplicateNamePathTest() {

            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .addSectionToApplicationForm(SAMPLE_APPLICATION_ID, new PostSectionDTO(SAMPLE_SECTION_TITLE)))
                            .isInstanceOf(FieldViolationException.class).hasMessage("Section name has to be unique");

        }

        @Test
        void addNewSection_insufficientPermissionsToAddThisSection() {
            ApplicationFormEntity testApplicationForm = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationForm.getGrantApplicationId();
            String sectionId = "test-section-id";

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationForm));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .addSectionToApplicationForm(applicationId, SAMPLE_POST_SECTION))
                            .isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);

        }

    }

    @Nested
    class deleteSection {

        @Test
        void deleteSectionHappyPathTest() {
            MockedStatic<ApplicationFormUtils> utilMock = mockStatic(ApplicationFormUtils.class);

            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_DELETE_QUESTION));

            ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .deleteSectionFromApplication(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID);

            Mockito.verify(ApplicationFormSectionServiceTest.this.applicationFormRepository).save(argument.capture());

            List<ApplicationFormSectionDTO> sections = argument.getValue().getDefinition().getSections();

            boolean sectionExists = sections.stream()
                    .anyMatch(section -> Objects.equals(section.getSectionId(), SAMPLE_SECTION_ID));

            assertThat(sectionExists).isFalse();

            utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));
            utilMock.close();
        }

        @Test
        void deleteSectionApplicationNotFoundTest() {

            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .deleteSectionFromApplication(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");

        }

        @Test
        void deleteSectionSectionNotFound() {
            String incorrectId = "incorrectId";

            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.of(SAMPLE_APPLICATION_FORM_ENTITY_DELETE_SECTION));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .deleteSectionFromApplication(SAMPLE_APPLICATION_ID, incorrectId))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Section with id " + incorrectId + " does not exist");

        }

        @Test
        void deleteSection_insufficientPermissionsToDeleteThisSection() {
            ApplicationFormEntity testApplicationForm = randomApplicationFormEntity().createdBy(2).build();
            Integer applicationId = testApplicationForm.getGrantApplicationId();
            String sectionId = "test-section-id";

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationForm));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .deleteSectionFromApplication(applicationId, sectionId)).isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);

        }

    }

    @Nested
    class updateSectionStatus {

        @Test
        void updateSectionStatusHappyPath() {
            MockedStatic<ApplicationFormUtils> utilMock = mockStatic(ApplicationFormUtils.class);

            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            final ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = testApplicationFormEntity.getDefinition().getSections().get(0).getSectionId();

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            ApplicationFormSectionServiceTest.this.applicationFormSectionService.updateSectionStatus(applicationId,
                    sectionId, SectionStatusEnum.COMPLETE);

            Mockito.verify(ApplicationFormSectionServiceTest.this.applicationFormRepository).save(argument.capture());

            SectionStatusEnum newSectionStatus = argument.getValue().getDefinition().getSections().get(0)
                    .getSectionStatus();

            assertThat(newSectionStatus).isEqualTo(SectionStatusEnum.COMPLETE);
            utilMock.verify(() -> ApplicationFormUtils.updateAuditDetailsAfterFormChange(any(), any(), eq(false)));
            utilMock.close();
        }

        @Test
        void updateSectionStatusApplicationNotFound() {
            Mockito.when(
                    ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(SAMPLE_APPLICATION_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .updateSectionStatus(SAMPLE_APPLICATION_ID, SAMPLE_SECTION_ID, SectionStatusEnum.COMPLETE))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Application with id " + SAMPLE_APPLICATION_ID + " does not exist");
        }

        @Test
        void updateSectionStatusSectionNotFound() {
            final ApplicationFormEntity testApplicationFormEntity = emptySectionApplicationFormGenerator().build();
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String nonExistentSectionId = "12345678";

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .updateSectionStatus(applicationId, nonExistentSectionId, SectionStatusEnum.COMPLETE))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Section with id " + nonExistentSectionId + " does not exist");
        }

        @Test
        void updateSectionStatus_AccessDenied() {
            final ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().createdBy(2).build();
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = "test-section-id";

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .updateSectionStatus(applicationId, sectionId, SectionStatusEnum.COMPLETE))
                            .isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);
        }

    }

    @Nested
    class updateSectionOrder {

        @Test
        void updateSectionOrderSuccessful() {
            MockedStatic<ApplicationFormUtils> utilMock = mockStatic(ApplicationFormUtils.class);

            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();
            List<ApplicationFormSectionDTO> sections = new ArrayList<>(
                    List.of(ApplicationFormSectionDTO.builder().sectionId("Section1").build(),
                            ApplicationFormSectionDTO.builder().sectionId("Section2").build(),
                            ApplicationFormSectionDTO.builder().sectionId("Section3").build(),
                            ApplicationFormSectionDTO.builder().sectionId("Section4").build()));
            testApplicationFormEntity.getDefinition().setSections(sections);
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = testApplicationFormEntity.getDefinition().getSections().get(2).getSectionId();
            final Integer increment = 1;

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            ApplicationFormSectionServiceTest.this.applicationFormSectionService.updateSectionOrder(applicationId,
                    sectionId, increment);

            Mockito.verify(ApplicationFormSectionServiceTest.this.applicationFormRepository).save(argument.capture());
            utilMock.close();
        }

        @Test
        void updateSectionOrderOutsideOfRange() {
            MockedStatic<ApplicationFormUtils> utilMock = mockStatic(ApplicationFormUtils.class);

            ArgumentCaptor<ApplicationFormEntity> argument = ArgumentCaptor.forClass(ApplicationFormEntity.class);

            ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().build();
            List<ApplicationFormSectionDTO> sections = new ArrayList<>(
                    List.of(ApplicationFormSectionDTO.builder().sectionId("Section1").build(),
                            ApplicationFormSectionDTO.builder().sectionId("Section2").build(),
                            ApplicationFormSectionDTO.builder().sectionId("Section3").build(),
                            ApplicationFormSectionDTO.builder().sectionId("Section4").build()));
            testApplicationFormEntity.getDefinition().setSections(sections);
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = testApplicationFormEntity.getDefinition().getSections().get(3).getSectionId();
            final Integer increment = 1;

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .updateSectionOrder(applicationId, sectionId, increment))
                            .isInstanceOf(FieldViolationException.class).hasMessage("Section is already at the bottom");

            utilMock.close();

        }

        @Test
        void updateSectionOrderUnauthorised() {
            final ApplicationFormEntity testApplicationFormEntity = randomApplicationFormEntity().createdBy(2).build();
            final Integer applicationId = testApplicationFormEntity.getGrantApplicationId();
            final String sectionId = "test-section-id";
            final Integer increment = 1;

            Mockito.when(ApplicationFormSectionServiceTest.this.applicationFormRepository.findById(applicationId))
                    .thenReturn(Optional.of(testApplicationFormEntity));

            assertThatThrownBy(() -> ApplicationFormSectionServiceTest.this.applicationFormSectionService
                    .updateSectionOrder(applicationId, sectionId, increment)).isInstanceOf(AccessDeniedException.class)
                            .hasMessage("User 1 is unable to access the application form with id " + applicationId);

        }

    }

}
