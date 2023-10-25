package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.*;
import gov.cabinetoffice.gap.adminbackend.dtos.application.questions.*;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.TemplateApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.mappers.ApplicationFormMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.TemplateApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.utils.ApplicationFormUtils;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.lang.Integer.parseInt;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationFormService {

    private final ApplicationFormRepository applicationFormRepository;

    private final TemplateApplicationFormRepository templateApplicationFormRepository;

    private final ApplicationFormMapper applicationFormMapper;

    private final SessionsService sessionsService;

    private final Validator validator;

    /**
     * This method is responsible for saving basic details about an application form to
     * PostgreSQL (copied from a template stored in the db)
     * @param applicationFormDTO
     * @return
     */
    public GenericPostResponseDTO saveApplicationForm(ApplicationFormPostDTO applicationFormDTO, SchemeDTO scheme) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        try {
            // TODO move template id to external config?
            final TemplateApplicationFormEntity formTemplate = this.templateApplicationFormRepository
                    .findById(1)
                    .orElseThrow(() -> new ApplicationFormException("Could not retrieve template application form"));

            final ApplicationFormEntity newFormEntity = ApplicationFormEntity.createFromTemplate(
                    applicationFormDTO.getGrantSchemeId(), applicationFormDTO.getApplicationName(),
                    session.getGrantAdminId(), formTemplate.getDefinition(), parseInt(scheme.getVersion()));

            newFormEntity.setCreatedBy(session.getGrantAdminId());

            final ApplicationFormEntity save = this.applicationFormRepository.save(newFormEntity);
            return new GenericPostResponseDTO(save.getGrantApplicationId());
        }
        catch (ApplicationFormException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ApplicationFormException(
                    "Could save application form with name " + applicationFormDTO.getApplicationName(), e);
        }

    }

    public List<ApplicationFormsFoundDTO> getMatchingApplicationFormsIds(
            ApplicationFormExistsDTO applicationFormExistsDTO) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        List<ApplicationFormsFoundView> applicationFormsFoundView = this.applicationFormRepository
                .findMatchingApplicationForm(session.getGrantAdminId(),
                        applicationFormExistsDTO.getGrantApplicationId(), applicationFormExistsDTO.getApplicationName(),
                        applicationFormExistsDTO.getGrantSchemeId());

        return this.applicationFormMapper.applicationFormFoundViewToDTO(applicationFormsFoundView);
    }

    public ApplicationFormDTO retrieveApplicationFormSummary(Integer applicationId, Boolean withSections,
            Boolean withQuestions) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        if (withSections) {
            ApplicationFormEntity applicationFormEntity = this.applicationFormRepository.findById(applicationId)
                    .orElseThrow(() -> new ApplicationFormException("No application found with id " + applicationId));
            if (!withQuestions) {
                applicationFormEntity.getDefinition().getSections().forEach(section -> section.setQuestions(null));
            }
            if (!session.getGrantAdminId().equals(applicationFormEntity.getCreatedBy())) {
                throw new AccessDeniedException("User " + session.getGrantAdminId()
                        + " is unable to access the application form with id " + applicationId);
            }
            return this.applicationFormMapper.applicationEntityToDto(applicationFormEntity);
        }
        else {
            ApplicationFormNoSections applicationFormNoSections = this.applicationFormRepository
                    .findByGrantApplicationId(applicationId)
                    .orElseThrow(() -> new ApplicationFormException("No application found with id " + applicationId));
            if (!session.getGrantAdminId().equals(applicationFormNoSections.getCreatedBy())) {
                throw new AccessDeniedException("User " + session.getGrantAdminId()
                        + " is unable to access the application form with id " + applicationId);
            }
            return this.applicationFormMapper.applicationEntityNoSectionsToDto(applicationFormNoSections);
        }
    }

    public void deleteApplicationForm(Integer applicationId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        ApplicationFormEntity applicationFormEntity = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("No application found with id " + applicationId));

        if (!session.getGrantAdminId().equals(applicationFormEntity.getCreatedBy())) {
            throw new AccessDeniedException("User " + session.getGrantAdminId()
                    + " is unable to access the application form with id " + applicationId);
        }

        this.applicationFormRepository.delete(applicationFormEntity);
    }

    public ApplicationFormEntity getApplicationFromSchemeId(Integer schemeId) {
        return applicationFormRepository.findByGrantSchemeId(schemeId).orElseThrow();
    }

    public void patchQuestionValues(Integer applicationId, String sectionId, String questionId,
            ApplicationFormQuestionDTO questionDto) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        this.applicationFormRepository.findById(applicationId).ifPresentOrElse(applicationForm -> {

            if (!session.getGrantAdminId().equals(applicationForm.getCreatedBy())) {
                throw new AccessDeniedException("User " + session.getGrantAdminId()
                        + " is unable to access the application form with id " + applicationId);
            }

            ApplicationFormQuestionDTO questionById = applicationForm.getDefinition().getSectionById(sectionId)
                    .getQuestionById(questionId);

            QuestionAbstractPatchDTO questionPatchDTO = validatePatchQuestion(questionDto,
                    questionById.getResponseType());

            if (questionPatchDTO.getClass() == QuestionGenericPatchDTO.class) {
                this.applicationFormMapper.updateGenericQuestionPatchToQuestionDto(
                        (QuestionGenericPatchDTO) questionPatchDTO, questionById);
            }
            else {
                this.applicationFormMapper.updateOptionsQuestionPatchToQuestionDto(
                        (QuestionOptionsPatchDTO) questionPatchDTO, questionById);
            }

            ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, session, false);

            this.applicationFormRepository.save(applicationForm);
        }, () -> {
            throw new NotFoundException("Application with id " + applicationId + " does not exist");
        });
    }

    private QuestionAbstractPatchDTO validatePatchQuestion(ApplicationFormQuestionDTO questionPatchDto,
            ResponseTypeEnum responseType) {
        Set violationsSet;
        QuestionAbstractPatchDTO mappedQuestion;

        // check response type, map to the correct model, and validate
        // left as switch statement in the event of new question types
        switch (responseType) {
            case MultipleSelection, Dropdown, SingleSelection -> {
                mappedQuestion = this.applicationFormMapper.questionDtoToQuestionOptionsPatch(questionPatchDto);
                violationsSet = this.validator.validate(mappedQuestion);
            }
            default -> {
                mappedQuestion = this.applicationFormMapper.questionDtoToQuestionGenericPatch(questionPatchDto);
                violationsSet = this.validator.validate(mappedQuestion);
            }
        }

        if (!violationsSet.isEmpty()) {
            throw new ConstraintViolationException(violationsSet);
        }
        else {
            return mappedQuestion;
        }

    }

    public String addQuestionToApplicationForm(Integer applicationId, String sectionId,
            ApplicationFormQuestionDTO question, HttpSession session) {
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();

        String questionId = UUID.randomUUID().toString();

        this.applicationFormRepository.findById(applicationId).ifPresentOrElse(applicationForm -> {

            if (!adminSession.getGrantAdminId().equals(applicationForm.getCreatedBy())) {
                throw new AccessDeniedException("User " + adminSession.getGrantAdminId()
                        + " is unable to access the application form with id " + applicationId);
            }

            ApplicationFormQuestionDTO applicationFormQuestionDTO;
            QuestionAbstractPostDTO questionAbstractPostDTO = validatePostQuestion(question);
            if (questionAbstractPostDTO.getClass() == QuestionOptionsPostDTO.class) {
                applicationFormQuestionDTO = this.applicationFormMapper
                        .optionsQuestionPostToQuestionDto((QuestionOptionsPostDTO) questionAbstractPostDTO);
            }
            else {
                applicationFormQuestionDTO = this.applicationFormMapper
                        .genericQuestionPostToQuestionDto((QuestionGenericPostDTO) questionAbstractPostDTO);
            }

            applicationFormQuestionDTO.setQuestionId(questionId);
            applicationFormQuestionDTO.getValidation()
                    .putAll(applicationFormQuestionDTO.getResponseType().getValidation());

            applicationForm.getDefinition().getSectionById(sectionId).getQuestions().add(applicationFormQuestionDTO);

            ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, adminSession, false);

            this.applicationFormRepository.save(applicationForm);
            this.sessionsService.deleteObjectFromSession(SessionObjectEnum.newQuestion, session);
        }, () -> {
            throw new NotFoundException("Application with id " + applicationId + " does not exist");
        });

        return questionId;
    }

    private QuestionAbstractPostDTO validatePostQuestion(ApplicationFormQuestionDTO questionPostDto) {
        Set violationsSet;
        QuestionAbstractPostDTO mappedQuestion;

        if (questionPostDto.getResponseType() == null) {
            throw new FieldViolationException("responseType", "Select a question type");
        }

        // check response type, map to the correct model, and validate
        // left as switch statement in the event of new question types
        switch (questionPostDto.getResponseType()) {
            case MultipleSelection, Dropdown, SingleSelection -> {
                mappedQuestion = this.applicationFormMapper.questionDtoToQuestionOptionsPost(questionPostDto);
                violationsSet = this.validator.validate(mappedQuestion);
            }
            default -> {
                mappedQuestion = this.applicationFormMapper.questionDtoToQuestionGenericPost(questionPostDto);
                violationsSet = this.validator.validate(mappedQuestion);
            }
        }

        if (!violationsSet.isEmpty()) {
            throw new ConstraintViolationException(violationsSet);
        }
        else {
            return mappedQuestion;
        }

    }

    public void deleteQuestionFromSection(Integer applicationId, String sectionId, String questionId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application with id " + applicationId + " does not exist"));

        if (!session.getGrantAdminId().equals(applicationForm.getCreatedBy())) {
            throw new AccessDeniedException("User " + session.getGrantAdminId()
                    + " is unable to access the application form with id " + applicationId);
        }

        boolean questionDeleted = applicationForm.getDefinition().getSectionById(sectionId).getQuestions()
                .removeIf(question -> Objects.equals(question.getQuestionId(), questionId));

        if (!questionDeleted) {
            throw new NotFoundException("Question with id " + questionId + " does not exist");
        }

        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, session, false);

        this.applicationFormRepository.save(applicationForm);

    }

    public ApplicationFormQuestionDTO retrieveQuestion(Integer applicationId, String sectionId, String questionId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application with id " + applicationId + " does not exist"));

        if (!session.getGrantAdminId().equals(applicationForm.getCreatedBy())) {
            throw new AccessDeniedException("User " + session.getGrantAdminId()
                    + " is unable to access the application form with id " + applicationId);
        }

        return applicationForm.getDefinition().getSectionById(sectionId).getQuestionById(questionId);

    }

    public void patchApplicationForm(Integer applicationId, ApplicationFormPatchDTO patchDTO, boolean isLambdaCall) {
        AdminSession session = null;
        ApplicationFormEntity application = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application with id " + applicationId + " does not exist."));

        if (!isLambdaCall) {
            session = HelperUtils.getAdminSessionForAuthenticatedUser();

            if (!application.getCreatedBy().equals(session.getGrantAdminId())) {
                throw new AccessDeniedException("User " + session.getGrantAdminId()
                        + " is unable to access the application form with id " + applicationId);
            }
        }

        if (patchDTO.getApplicationStatus() == ApplicationStatusEnum.PUBLISHED && application.getDefinition()
                .getSections().stream().anyMatch(section -> section.getQuestions().isEmpty())) {
            throw new FieldViolationException("sections", "Cannot publish a form with a section that has no questions");
        }

        try {
            this.applicationFormMapper.updateApplicationEntityFromPatchDto(patchDTO, application);
            if (patchDTO.getApplicationStatus().equals(ApplicationStatusEnum.PUBLISHED)) {
                application.setLastPublished(Instant.now());
            }

            ApplicationFormUtils.updateAuditDetailsAfterFormChange(application, session, isLambdaCall);

            this.applicationFormRepository.save(application);
        }
        catch (Exception e) {
            throw new ApplicationFormException("Error occured when patching appliction with id of " + applicationId, e);
        }

    }

}
