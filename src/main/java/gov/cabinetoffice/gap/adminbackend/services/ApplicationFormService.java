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
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.TemplateApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.utils.ApplicationFormUtils;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static java.lang.Integer.parseInt;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationFormService {

    private final ApplicationFormRepository applicationFormRepository;

    private final TemplateApplicationFormRepository templateApplicationFormRepository;

    private final SchemeRepository schemeRepository;

    private final ApplicationFormMapper applicationFormMapper;

    private final SessionsService sessionsService;

    private final Validator validator;

    private final Clock clock;

    public ApplicationFormEntity save(ApplicationFormEntity applicationForm) {
        final ApplicationFormEntity savedApplicationForm = applicationFormRepository.save(applicationForm);

        Optional.ofNullable(HelperUtils.getAdminSessionForAuthenticatedUser())
                .ifPresentOrElse(adminSession -> {
                    final SchemeEntity grantScheme = schemeRepository.findById(applicationForm.getGrantSchemeId())
                            .orElseThrow(() -> new EntityNotFoundException("Could not find grant scheme with ID" + applicationForm.getGrantSchemeId()));

                    grantScheme.setLastUpdated(Instant.now(clock));
                    grantScheme.setLastUpdatedBy(adminSession.getGrantAdminId());

                    this.schemeRepository.save(grantScheme);
                }, () -> log.warn("Admin session was null. Update must have been performed by a lambda."));

        return savedApplicationForm;
    }

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
            final TemplateApplicationFormEntity formTemplate = this.templateApplicationFormRepository.findById(1)
                    .orElseThrow(() -> new ApplicationFormException("Could not retrieve template application form"));

            final ApplicationFormEntity newFormEntity = ApplicationFormEntity.createFromTemplate(
                    applicationFormDTO.getGrantSchemeId(), applicationFormDTO.getApplicationName(),
                    session.getGrantAdminId(), formTemplate.getDefinition(), parseInt(scheme.getVersion()));

            newFormEntity.setCreatedBy(session.getGrantAdminId());

            final ApplicationFormEntity save = save(newFormEntity);
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
        List<ApplicationFormsFoundView> applicationFormsFoundView = this.applicationFormRepository
                .findMatchingApplicationForm(applicationFormExistsDTO.getGrantApplicationId(), applicationFormExistsDTO.getApplicationName(),
                        applicationFormExistsDTO.getGrantSchemeId());

        return this.applicationFormMapper.applicationFormFoundViewToDTO(applicationFormsFoundView);
    }

    public ApplicationFormDTO retrieveApplicationFormSummary(Integer applicationId, boolean withSections,
            boolean withQuestions) {
        if (withSections) {
            ApplicationFormEntity applicationFormEntity = this.applicationFormRepository.findById(applicationId)
                    .orElseThrow(() -> new ApplicationFormException("No application found with id " + applicationId));
            if (!withQuestions) {
                applicationFormEntity.getDefinition().getSections().forEach(section -> section.setQuestions(null));
            }
            return this.applicationFormMapper.applicationEntityToDto(applicationFormEntity);
        }
        else {
            ApplicationFormNoSections applicationFormNoSections = this.applicationFormRepository
                    .findByGrantApplicationId(applicationId)
                    .orElseThrow(() -> new ApplicationFormException("No application found with id " + applicationId));
            return this.applicationFormMapper.applicationEntityNoSectionsToDto(applicationFormNoSections);
        }
    }

    public void deleteApplicationForm(Integer applicationId) {
        ApplicationFormEntity applicationFormEntity = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("No application found with id " + applicationId));

        this.applicationFormRepository.delete(applicationFormEntity);
    }

    public Optional<ApplicationFormEntity> getOptionalApplicationFromSchemeId(Integer schemeId) {
        return applicationFormRepository.findByGrantSchemeId(schemeId);
    }

    public void patchQuestionValues(Integer applicationId, String sectionId, String questionId,
            ApplicationFormQuestionDTO questionDto, HttpSession session) {
        this.applicationFormRepository.findById(applicationId).ifPresentOrElse(applicationForm -> {

            ApplicationFormQuestionDTO questionById = applicationForm.getDefinition().getSectionById(sectionId)
                    .getQuestionById(questionId);

            ResponseTypeEnum responseType = Optional
                    .ofNullable(questionDto.getResponseType())
                    .orElse(questionById.getResponseType());

            QuestionAbstractPatchDTO questionPatchDTO = validatePatchQuestion(questionDto, responseType);

            if (questionPatchDTO.getClass() == QuestionGenericPatchDTO.class) {
                this.applicationFormMapper.updateGenericQuestionPatchToQuestionDto(
                        (QuestionGenericPatchDTO) questionPatchDTO, questionById);
            }
            else {
                this.applicationFormMapper.updateOptionsQuestionPatchToQuestionDto(
                        (QuestionOptionsPatchDTO) questionPatchDTO, questionById);
            }

            ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, false);

            save(applicationForm);
            this.sessionsService.deleteObjectFromSession(SessionObjectEnum.updatedQuestion, session);
        }, () -> {
            throw new NotFoundException("Application with id " + applicationId + " does not exist");
        });
    }

    private QuestionAbstractPatchDTO validatePatchQuestion(ApplicationFormQuestionDTO questionPatchDto,
            ResponseTypeEnum responseType) {
        Set<ConstraintViolation<QuestionAbstractPatchDTO>> violationsSet;
        QuestionAbstractPatchDTO mappedQuestion;

        // check response type, map to the correct model, and validate
        if (Objects.requireNonNull(responseType) == ResponseTypeEnum.MultipleSelection
                || responseType == ResponseTypeEnum.Dropdown || responseType == ResponseTypeEnum.SingleSelection) {
            mappedQuestion = this.applicationFormMapper.questionDtoToQuestionOptionsPatch(questionPatchDto);
        }
        else {
            mappedQuestion = this.applicationFormMapper.questionDtoToQuestionGenericPatch(questionPatchDto);
        }
        violationsSet = this.validator.validate(mappedQuestion);

        validateMaxWordsValidationField(questionPatchDto, responseType);

        if (!violationsSet.isEmpty()) {
            throw new ConstraintViolationException(violationsSet);
        }
        else {
            return mappedQuestion;
        }
    }

    // TODO GAP-2429: Refactor validation of validation Map to use a DTO with proper validation annotations
    private void validateMaxWordsValidationField(final ApplicationFormQuestionDTO questionPatchDto, final ResponseTypeEnum responseType) {
        if (responseType == ResponseTypeEnum.LongAnswer) {
            final String MAX_WORDS_FIELD = "maxWords";
            if (!questionPatchDto.getValidation().containsKey(MAX_WORDS_FIELD)) {
                throw new FieldViolationException(MAX_WORDS_FIELD, "Please enter the max words an applicant could enter");
            }
            final String maxWordsString = questionPatchDto.getValidation().get("maxWords").toString();
            if (maxWordsString.isBlank()) {
                throw new FieldViolationException(MAX_WORDS_FIELD, "Please enter the max words an applicant could enter");
            }
            if (!NumberUtils.isCreatable(maxWordsString)) {
                throw new FieldViolationException(MAX_WORDS_FIELD, "Max words must be a number");
            }
            final long maxWords = Long.parseLong(maxWordsString);
            if (maxWords < 1) {
                throw new FieldViolationException(MAX_WORDS_FIELD, "Max words must be greater than 0");
            }
            if (maxWords > 5000) {
                throw new FieldViolationException(MAX_WORDS_FIELD, "Max words must be less than 5000");
            }
        }
    }

    public String addQuestionToApplicationForm(Integer applicationId, String sectionId,
            ApplicationFormQuestionDTO question, HttpSession session) {

        String questionId = UUID.randomUUID().toString();

        this.applicationFormRepository.findById(applicationId).ifPresentOrElse(applicationForm -> {
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

            ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, false);

            save(applicationForm);
            this.sessionsService.deleteObjectFromSession(SessionObjectEnum.newQuestion, session);
        }, () -> {
            throw new NotFoundException("Application with id " + applicationId + " does not exist");
        });

        return questionId;
    }

    private QuestionAbstractPostDTO validatePostQuestion(ApplicationFormQuestionDTO questionPostDto) {
        if (questionPostDto.getResponseType() == null) {
            throw new FieldViolationException("responseType", "Select a question type");
        }

        // check response type, map to the correct model, and validate
        // left as switch statement in the event of new question types
        Set<ConstraintViolation<QuestionAbstractPostDTO>> violationsSet;
        QuestionAbstractPostDTO mappedQuestion;

        switch (questionPostDto.getResponseType()) {
            case MultipleSelection, Dropdown, SingleSelection -> {
                mappedQuestion = this.applicationFormMapper.questionDtoToQuestionOptionsPost(questionPostDto);
            }
            default -> {
                mappedQuestion = this.applicationFormMapper.questionDtoToQuestionGenericPost(questionPostDto);
            }
        }
        violationsSet = this.validator.validate(mappedQuestion);

        validateMaxWordsValidationField(questionPostDto, questionPostDto.getResponseType());

        if (!violationsSet.isEmpty()) {
            throw new ConstraintViolationException(violationsSet);
        }
        else {
            return mappedQuestion;
        }

    }

    public void deleteQuestionFromSection(Integer applicationId, String sectionId, String questionId) {

        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application with id " + applicationId + " does not exist"));

        boolean questionDeleted = applicationForm.getDefinition().getSectionById(sectionId).getQuestions()
                .removeIf(question -> Objects.equals(question.getQuestionId(), questionId));

        if (!questionDeleted) {
            throw new NotFoundException("Question with id " + questionId + " does not exist");
        }

        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, false);

        save(applicationForm);
    }

    public ApplicationFormQuestionDTO retrieveQuestion(Integer applicationId, String sectionId, String questionId) {
        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application with id " + applicationId + " does not exist"));

        return applicationForm.getDefinition().getSectionById(sectionId).getQuestionById(questionId);

    }

    public void patchApplicationForm(Integer applicationId, ApplicationFormPatchDTO patchDTO, boolean isLambdaCall) {
        ApplicationFormEntity application = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application with id " + applicationId + " does not exist."));

        if (patchDTO.getApplicationStatus() == ApplicationStatusEnum.PUBLISHED && application.getDefinition()
                .getSections().stream().anyMatch(section -> section.getQuestions().isEmpty())) {
            throw new FieldViolationException("sections", "Cannot publish a form with a section that has no questions");
        }

        try {
            this.applicationFormMapper.updateApplicationEntityFromPatchDto(patchDTO, application);
            if (patchDTO.getApplicationStatus().equals(ApplicationStatusEnum.PUBLISHED)) {
                application.setLastPublished(Instant.now());
            }

            ApplicationFormUtils.updateAuditDetailsAfterFormChange(application, isLambdaCall);

            save(application);
        }
        catch (Exception e) {
            throw new ApplicationFormException("Error occurred when patching application with id of " + applicationId,
                    e);
        }

    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void updateApplicationOwner(Integer adminId, Integer schemeId) {
        Optional<ApplicationFormEntity> applicationOptional = this.applicationFormRepository
                .findByGrantSchemeId(schemeId);
        if (applicationOptional.isPresent()) {
            final ApplicationFormEntity application = applicationOptional.get();
            application.setCreatedBy(adminId);
            applicationFormRepository.save(application);
        }
    }

    public void updateQuestionOrder(final Integer applicationId, final String sectionId, final String questionId, final Integer increment) {
        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(
                        "Application with id " + applicationId + " does not exist or insufficient permissions"));

        final List<ApplicationFormSectionDTO> sections = applicationForm.getDefinition().getSections();
        final ApplicationFormSectionDTO section = applicationForm.getDefinition().getSectionById(sectionId);
        final List<ApplicationFormQuestionDTO> questions = section.getQuestions();
        final ApplicationFormQuestionDTO question = section.getQuestionById(questionId);

        final int index = questions.indexOf(question);
        final int questionListSize = questions.size() - 1;
        final int newSectionIndex = index + increment;

        if (newSectionIndex < 0)
            throw new FieldViolationException("questionId", "Question is already at the top");

        if (newSectionIndex > questionListSize)
            throw new FieldViolationException("questionId", "Question is already at the bottom");

        questions.remove(index);
        questions.add(newSectionIndex, question);

        applicationForm.getDefinition().setSections(sections);
        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, false);
        save(applicationForm);
    }

    public Integer getLastUpdatedBy(Integer applicationId) {
        return this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application with id " + applicationId + " does not exist"))
                .getLastUpdateBy();
    }

    public ApplicationStatusEnum getApplicationStatus(Integer applicationId) {
        return this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application with id " + applicationId + " does not exist"))
                .getApplicationStatus();
    }
}
