package gov.cabinetoffice.gap.adminbackend.testdata;

import gov.cabinetoffice.gap.adminbackend.dtos.GenericPostResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.*;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.ClassErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.entities.TemplateApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;
import gov.cabinetoffice.gap.adminbackend.models.ClassError;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;

import java.time.Instant;
import java.util.*;

public class ApplicationFormTestData {

    public final static Integer SAMPLE_APPLICATION_ID = 111;

    public final static UUID SAMPLE_ADVERT_ID = UUID.fromString("0-0-0-0-0");

    public final static Integer SAMPLE_SECOND_APPLICATION_ID = 222;

    public final static Integer SAMPLE_SCHEME_ID = 333;

    public final static Integer SAMPLE_VERSION = 1;

    public final static Instant SAMPLE_CREATED = Instant.parse("2022-01-01T00:00:00.00Z");

    public final static Integer SAMPLE_CREATED_BY = 1;

    public final static Instant SAMPLE_LAST_UPDATED = Instant.parse("2022-01-02T00:00:00.00Z");

    public final static Instant SAMPLE_LAST_PUBLISHED = Instant.parse("2022-01-03T00:00:00.00Z");

    public final static Integer SAMPLE_LAST_UPDATE_BY = 999;

    public final static String SAMPLE_APPLICATION_NAME = "Test Application Form Name";

    public final static String SAMPLE_SECTION_ID = "ef0907af-c24b-49ff-a535-436ac62b3a2c";

    public final static List<String> SAMPLE_QUESTION_OPTIONS = new LinkedList<>(
            Arrays.asList("Option 1", "Option 2", "Option 3")); // If we perform any
                                                                // modifications to the
                                                                // size during test, we
                                                                // must use LinkedList

    public final static List<String> SAMPLE_QUESTION_INVALID_OPTIONS = new LinkedList<>(
            Arrays.asList("Option 1", "Option 2", ""));

    public final static String SAMPLE_QUESTION_ID = "d169e143-9b64-4c09-b5b2-fecd02afced8";

    public final static String SAMPLE_QUESTION_FIELD_TITLE = "What is the question?";

    public final static Map<String, Object> SAMPLE_VALIDATION = Collections.singletonMap("mandatory", true);

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION = new ApplicationFormQuestionDTO(SAMPLE_QUESTION_ID,
            "ORG_TYPE", null, SAMPLE_QUESTION_FIELD_TITLE, "Answer the question", null, null, null,
            ResponseTypeEnum.YesNo, Collections.singletonMap("mandatory", true), null);

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION_WITH_OPTIONS = new ApplicationFormQuestionDTO(
            SAMPLE_QUESTION_ID, "ORG_TYPE", null, "Select one of the following", "Answer the question", null, null,
            null, ResponseTypeEnum.Dropdown, Collections.singletonMap("mandatory", true), SAMPLE_QUESTION_OPTIONS);

    public final static List<ApplicationFormQuestionDTO> SAMPLE_QUESTION_LIST = new LinkedList<>(
            Collections.singletonList(SAMPLE_QUESTION));

    public final static List<ApplicationFormQuestionDTO> SAMPLE_QUESTION_LIST_DELETE_QUESTION = new LinkedList<>(
            Collections.singletonList(SAMPLE_QUESTION));

    public final static List<ApplicationFormQuestionDTO> SAMPLE_QUESTION_WITH_OPTIONS_LIST = new LinkedList<>(
            Collections.singletonList(SAMPLE_QUESTION_WITH_OPTIONS));

    public final static String SAMPLE_SECTION_TITLE = "Section Title";

    public final static ApplicationFormSectionDTO SAMPLE_SECTION = new ApplicationFormSectionDTO(SAMPLE_SECTION_ID,
            SAMPLE_SECTION_TITLE, SectionStatusEnum.INCOMPLETE, SAMPLE_QUESTION_LIST);

    public final static ApplicationFormSectionDTO SAMPLE_SECTION_NO_QUESTIONS = new ApplicationFormSectionDTO(
            SAMPLE_SECTION_ID, SAMPLE_SECTION_TITLE, SectionStatusEnum.INCOMPLETE, SAMPLE_QUESTION_LIST);

    public final static ApplicationFormSectionDTO SAMPLE_SECTION_SECTIONS_NO_QUESTIONS = new ApplicationFormSectionDTO(
            SAMPLE_SECTION_ID, SAMPLE_SECTION_TITLE, SectionStatusEnum.INCOMPLETE, SAMPLE_QUESTION_LIST);

    public final static ApplicationFormSectionDTO SAMPLE_SECTION_DELETE_QUESTION = new ApplicationFormSectionDTO(
            SAMPLE_SECTION_ID, SAMPLE_SECTION_TITLE, SectionStatusEnum.INCOMPLETE,
            SAMPLE_QUESTION_LIST_DELETE_QUESTION);

    public final static ApplicationFormSectionDTO SAMPLE_SECTION_WITH_OPTIONS = new ApplicationFormSectionDTO(
            SAMPLE_SECTION_ID, "Section Title", SectionStatusEnum.INCOMPLETE, SAMPLE_QUESTION_WITH_OPTIONS_LIST);

    public final static List<ApplicationFormSectionDTO> SAMPLE_SECTIONS_LIST = new LinkedList<>(
            Collections.singletonList(SAMPLE_SECTION));

    public final static List<ApplicationFormSectionDTO> SAMPLE_SECTIONS_LIST_DELETE_SECTION = new LinkedList<>(
            Collections.singletonList(SAMPLE_SECTION));

    public final static List<ApplicationFormSectionDTO> SAMPLE_SECTIONS_LIST_DELETE_QUESTION = new LinkedList<>(
            Collections.singletonList(SAMPLE_SECTION_DELETE_QUESTION));

    public final static List<ApplicationFormSectionDTO> SAMPLE_SECTIONS_LIST_NO_QUESTIONS = new LinkedList<>(
            Collections.singletonList(SAMPLE_SECTION_NO_QUESTIONS));

    public final static List<ApplicationFormSectionDTO> SAMPLE_SECTIONS_LIST_SECTIONS_NO_QUESTIONS = new LinkedList<>(
            Collections.singletonList(SAMPLE_SECTION_SECTIONS_NO_QUESTIONS));

    public final static List<ApplicationFormSectionDTO> SAMPLE_SECTIONS_LIST_WITH_OPTIONS = Collections
            .singletonList(SAMPLE_SECTION_WITH_OPTIONS);

    public final static ApplicationDefinitionDTO SAMPLE_APPLICATION_DEFINITION = new ApplicationDefinitionDTO(
            SAMPLE_SECTIONS_LIST);

    public final static ApplicationDefinitionDTO SAMPLE_APPLICATION_DEFINITION_DELETE_SECTION = new ApplicationDefinitionDTO(
            SAMPLE_SECTIONS_LIST_DELETE_SECTION);

    public final static ApplicationDefinitionDTO SAMPLE_APPLICATION_DEFINITION_DELETE_QUESTION = new ApplicationDefinitionDTO(
            SAMPLE_SECTIONS_LIST_DELETE_QUESTION);

    public final static ApplicationDefinitionDTO SAMPLE_APPLICATION_DEFINITION_NO_QUESTIONS = new ApplicationDefinitionDTO(
            SAMPLE_SECTIONS_LIST_NO_QUESTIONS);

    public final static ApplicationDefinitionDTO SAMPLE_APPLICATION_DEFINITION_SECTIONS_NO_QUESTIONS = new ApplicationDefinitionDTO(
            SAMPLE_SECTIONS_LIST_SECTIONS_NO_QUESTIONS);

    public final static ApplicationDefinitionDTO SAMPLE_APPLICATION_DEFINITION_WITH_OPTIONS_QUESTION = new ApplicationDefinitionDTO(
            SAMPLE_SECTIONS_LIST_WITH_OPTIONS);

    public final static ApplicationDefinitionDTO SAMPLE_APPLICATION_DEFINITION_WITH_DUPLICATE_SECTIONS = new ApplicationDefinitionDTO(
            Arrays.asList(SAMPLE_SECTION, SAMPLE_SECTION));

    public final static ApplicationFormDTO SAMPLE_APPLICATION_FORM_DTO = new ApplicationFormDTO(SAMPLE_APPLICATION_ID,
            SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME, null, ApplicationStatusEnum.DRAFT, SAMPLE_SECTIONS_LIST);

    public final static ApplicationFormDTO SAMPLE_SECOND_APPLICATION_FORM_DTO = new ApplicationFormDTO(
            SAMPLE_SECOND_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME, null, ApplicationStatusEnum.DRAFT,
            SAMPLE_SECTIONS_LIST);

    public final static ApplicationFormDTO SAMPLE_TEMPLATE_FORM_DTO = new ApplicationFormDTO(SAMPLE_APPLICATION_ID,
            null, null, null, ApplicationStatusEnum.DRAFT, SAMPLE_SECTIONS_LIST);

    public final static GenericPostResponseDTO SAMPLE_APPLICATION_RESPONSE_SUCCESS = new GenericPostResponseDTO(
            SAMPLE_APPLICATION_ID);

    public final static ApplicationFormPostDTO SAMPLE_APPLICATION_POST_FORM_DTO = new ApplicationFormPostDTO(
            SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME);

    public final static TemplateApplicationFormEntity SAMPLE_TEMPLATE_APPLICATION_FORM_ENTITY = new TemplateApplicationFormEntity(
            SAMPLE_APPLICATION_ID, SAMPLE_APPLICATION_DEFINITION);

    public final static ApplicationFormEntity SAMPLE_APPLICATION_FORM_ENTITY = new ApplicationFormEntity(
            SAMPLE_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_VERSION, SAMPLE_CREATED, SAMPLE_CREATED_BY,
            SAMPLE_LAST_UPDATED, SAMPLE_LAST_UPDATE_BY, SAMPLE_LAST_PUBLISHED, SAMPLE_APPLICATION_NAME,
            ApplicationStatusEnum.DRAFT, SAMPLE_APPLICATION_DEFINITION);

    public final static ApplicationFormEntity SAMPLE_APPLICATION_FORM_ENTITY_DELETE_SECTION = new ApplicationFormEntity(
            SAMPLE_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_VERSION, SAMPLE_CREATED, SAMPLE_CREATED_BY,
            SAMPLE_LAST_UPDATED, SAMPLE_LAST_UPDATE_BY, SAMPLE_LAST_PUBLISHED, SAMPLE_APPLICATION_NAME,
            ApplicationStatusEnum.DRAFT, SAMPLE_APPLICATION_DEFINITION_DELETE_SECTION);

    public final static ApplicationFormEntity SAMPLE_APPLICATION_FORM_ENTITY_DELETE_QUESTION = new ApplicationFormEntity(
            SAMPLE_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_VERSION, SAMPLE_CREATED, SAMPLE_CREATED_BY,
            SAMPLE_LAST_UPDATED, SAMPLE_LAST_UPDATE_BY, SAMPLE_LAST_PUBLISHED, SAMPLE_APPLICATION_NAME,
            ApplicationStatusEnum.DRAFT, SAMPLE_APPLICATION_DEFINITION_DELETE_QUESTION);

    public final static ApplicationFormEntity SAMPLE_APPLICATION_FORM_ENTITY_NO_QUESTIONS = new ApplicationFormEntity(
            SAMPLE_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_VERSION, SAMPLE_CREATED, SAMPLE_CREATED_BY,
            SAMPLE_LAST_UPDATED, SAMPLE_LAST_UPDATE_BY, SAMPLE_LAST_PUBLISHED, SAMPLE_APPLICATION_NAME,
            ApplicationStatusEnum.DRAFT, SAMPLE_APPLICATION_DEFINITION_NO_QUESTIONS);

    public final static ApplicationFormEntity SAMPLE_APPLICATION_FORM_ENTITY_SECTIONS_NO_QUESTIONS = new ApplicationFormEntity(
            SAMPLE_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_VERSION, SAMPLE_CREATED, SAMPLE_CREATED_BY,
            SAMPLE_LAST_UPDATED, SAMPLE_LAST_UPDATE_BY, SAMPLE_LAST_PUBLISHED, SAMPLE_APPLICATION_NAME,
            ApplicationStatusEnum.DRAFT, SAMPLE_APPLICATION_DEFINITION_SECTIONS_NO_QUESTIONS);

    public final static ApplicationFormEntity SAMPLE_SECOND_APPLICATION_FORM_ENTITY = new ApplicationFormEntity(
            SAMPLE_SECOND_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_VERSION, SAMPLE_CREATED, SAMPLE_CREATED_BY,
            SAMPLE_LAST_UPDATED, SAMPLE_LAST_UPDATE_BY, SAMPLE_LAST_PUBLISHED, SAMPLE_APPLICATION_NAME,
            ApplicationStatusEnum.DRAFT, SAMPLE_APPLICATION_DEFINITION_WITH_OPTIONS_QUESTION);

    public final static ApplicationFormEntity SAMPLE_APPLICATION_FORM_ENTITY_WITH_DUPLICATE_SECTIONS = new ApplicationFormEntity(
            SAMPLE_SECOND_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_VERSION, SAMPLE_CREATED, SAMPLE_CREATED_BY,
            SAMPLE_LAST_UPDATED, SAMPLE_LAST_UPDATE_BY, SAMPLE_LAST_PUBLISHED, SAMPLE_APPLICATION_NAME,
            ApplicationStatusEnum.DRAFT, SAMPLE_APPLICATION_DEFINITION_WITH_DUPLICATE_SECTIONS);

    public final static ApplicationFormExistsDTO SAMPLE_APPLICATION_FORM_EXISTS_DTO_SINGLE_PROP = new ApplicationFormExistsDTO(
            null, SAMPLE_SCHEME_ID, null);

    public final static ApplicationFormExistsDTO SAMPLE_APPLICATION_FORM_EXISTS_DTO_MULTIPLE_PROPS = new ApplicationFormExistsDTO(
            SAMPLE_APPLICATION_ID, SAMPLE_SCHEME_ID, SAMPLE_APPLICATION_NAME);

    public final static List<ClassError> SAMPLE_CLASS_ERROR_LIST_NO_PROPS_PROVIDED = Collections.singletonList(
            new ClassError("applicationFormExistsDTO", "Request body did not provide any valid searchable fields."));

    public final static ClassErrorsDTO SAMPLE_CLASS_ERROR_NO_PROPS_PROVIDED = new ClassErrorsDTO(
            SAMPLE_CLASS_ERROR_LIST_NO_PROPS_PROVIDED);

    public final static ApplicationFormEntity SAMPLE_ENTITY_EXAMPLE_SINGLE_PROP = new ApplicationFormEntity(null,
            SAMPLE_SCHEME_ID, null, null, null, null, null, null, null, null, null);

    public final static String SAMPLE_CLASS_ERROR_NO_PROPS_JSON_STRING = HelperUtils
            .asJsonString(SAMPLE_CLASS_ERROR_NO_PROPS_PROVIDED);

    public final static List<ApplicationFormEntity> SAMPLE_APPLICATION_FORM_ENTITY_LIST = Collections
            .singletonList(SAMPLE_APPLICATION_FORM_ENTITY);

    public final static List<ApplicationFormEntity> SAMPLE_EMPTY_APPLICATION_ENBTITY_LIST = Collections.emptyList();

    public final static List<ApplicationFormEntity> SAMPLE_LIST_WITH_MULTIPLE_APPLICATION_FORMS = Arrays
            .asList(SAMPLE_APPLICATION_FORM_ENTITY, SAMPLE_SECOND_APPLICATION_FORM_ENTITY);

    public final static List<Integer> SAMPLE_RETURN_SINGLE_APPLICATION_FORM_ID = Collections
            .singletonList(SAMPLE_APPLICATION_ID);

    public final static List<Integer> SAMPLE_RETURN_MULTIPLE_APPLICATION_FORM_IDS = Arrays.asList(SAMPLE_APPLICATION_ID,
            SAMPLE_SECOND_APPLICATION_ID);

    public final static String SAMPLE_UPDATED_FIELD_TITLE = "Updated field title";

    public final static List<String> SAMPLE_UPDATED_OPTIONS = Arrays.asList("New 1", "New 2", "New 3");

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION_GENERIC_PATCH_DTO = ApplicationFormQuestionDTO
            .builder().fieldTitle(SAMPLE_UPDATED_FIELD_TITLE).build();

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION_OPTIONS_PATCH_DTO = ApplicationFormQuestionDTO
            .builder().options(SAMPLE_UPDATED_OPTIONS).build();

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION_GENERIC_POST_DTO = ApplicationFormQuestionDTO
            .builder().fieldTitle(SAMPLE_QUESTION_FIELD_TITLE).responseType(ResponseTypeEnum.YesNo)
            .validation(SAMPLE_VALIDATION).build();

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION_OPTIONS_POST_DTO = ApplicationFormQuestionDTO
            .builder().fieldTitle(SAMPLE_QUESTION_FIELD_TITLE).responseType(ResponseTypeEnum.Dropdown)
            .validation(SAMPLE_VALIDATION).options(SAMPLE_QUESTION_OPTIONS).build();

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION_GENERIC_INVALID_POST_DTO = ApplicationFormQuestionDTO
            .builder().responseType(ResponseTypeEnum.YesNo).validation(SAMPLE_VALIDATION).build();

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION_OPTIONS_INVALID_POST_DTO = ApplicationFormQuestionDTO
            .builder().fieldTitle(SAMPLE_QUESTION_FIELD_TITLE).responseType(ResponseTypeEnum.Dropdown)
            .validation(SAMPLE_VALIDATION).build();

    public final static ApplicationFormQuestionDTO SAMPLE_QUESTION_OPTIONS_CONTENT_INVALID_POST_DTO = ApplicationFormQuestionDTO
            .builder().fieldTitle(SAMPLE_QUESTION_FIELD_TITLE).responseType(ResponseTypeEnum.Dropdown)
            .options(SAMPLE_QUESTION_INVALID_OPTIONS).validation(SAMPLE_VALIDATION).build();

    public final static String SAMPLE_NEW_SECTION_TITLE = "New section name";

    public final static PostSectionDTO SAMPLE_POST_SECTION = new PostSectionDTO(SAMPLE_NEW_SECTION_TITLE);

    public final static ApplicationFormPatchDTO SAMPLE_PATCH_APPLICATION_DTO = new ApplicationFormPatchDTO(
            ApplicationStatusEnum.PUBLISHED);

}
