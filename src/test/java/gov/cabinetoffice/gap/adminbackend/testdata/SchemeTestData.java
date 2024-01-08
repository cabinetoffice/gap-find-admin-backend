package gov.cabinetoffice.gap.adminbackend.testdata;

import gov.cabinetoffice.gap.adminbackend.dtos.errors.FieldErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.models.ClassError;
import gov.cabinetoffice.gap.adminbackend.models.ValidationError;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class SchemeTestData {

    public final static Integer SAMPLE_SCHEME_ID = 1;

    public final static Integer SAMPLE_ORGANISATION_ID = 1;

    public final static Integer SAMPLE_USER_ID = 1;

    public final static String SAMPLE_SCHEME_NAME = "Sample Scheme";

    public final static String SAMPLE_GGIS_REFERENCE = "GGIS12345";

    public final static String SAMPLE_SCHEME_CONTACT = "contact@address.com";

    public final static String SAMPLE_DESCRIPTION = "Sample scheme description";

    public final static String SAMPLE_INVALID_NAME = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas et ultrices tortor, in cursus est. Pellentesque id libero rutrum, mattis dui nec, pulvinar ligula. Pellentesque vel dictum ex, vel posuere enim. Suspendisse vulputate diam ipsum, sit amet mollis quam gravida in. Nulla pretium aliquam.";

    public final static SchemeDTO SCHEME_DTO_EXAMPLE = SchemeDTO.builder().schemeId(SAMPLE_SCHEME_ID)
            .funderId(SAMPLE_ORGANISATION_ID).name(SAMPLE_SCHEME_NAME).ggisReference(SAMPLE_GGIS_REFERENCE)
            .contactEmail(SAMPLE_SCHEME_CONTACT).build();

    public final static List<SchemeDTO> SCHEME_DTOS_EXAMPLE = Collections.singletonList(SCHEME_DTO_EXAMPLE);

    public final static SchemePostDTO SCHEME_POST_DTO_EXAMPLE = new SchemePostDTO(SAMPLE_SCHEME_NAME,
            SAMPLE_GGIS_REFERENCE, SAMPLE_SCHEME_CONTACT);

    public final static SchemePatchDTO SCHEME_PATCH_DTO_EXAMPLE = new SchemePatchDTO(SAMPLE_SCHEME_NAME,
            SAMPLE_GGIS_REFERENCE, null);

    public final static ClassError SCHEME_PATCH_CLASS_ERROR_ALL_NULL = new ClassError("SchemePatchDTO",
            "All checked fields were null");

    public final static List<ClassError> SCHEME_PATCH_DTO_CLASS_ERROR_LIST = Collections
            .singletonList(SCHEME_PATCH_CLASS_ERROR_ALL_NULL);

    public final static ValidationError SCHEME_VALIDATION_NAME_NULL = new ValidationError("name",
            "Enter the name of your grant");

    public final static ValidationError SCHEME_VALIDATION_NAME_MAX_LENGTH = new ValidationError("name",
            "Name should not be greater than 255 characters");

    public final static ValidationError SCHEME_VALIDATION_GGIS_NULL = new ValidationError("ggisReference",
            "Enter your GGIS Scheme Reference Number");

    public final static ValidationError SCHEME_VALIDATION_FUNDER_NULL = new ValidationError("funderId",
            "Funder ID cannot be blank");

    public final static List<ValidationError> SCHEME_VALIDATION_ALL_NULL_LIST = List.of(SCHEME_VALIDATION_NAME_NULL,
            SCHEME_VALIDATION_GGIS_NULL, SCHEME_VALIDATION_FUNDER_NULL);

    public final static List<ValidationError> SCHEME_VALIDATION_NAME_NULL_LIST = List.of(SCHEME_VALIDATION_NAME_NULL);

    public final static List<ValidationError> SCHEME_VALIDATION_NAME_MAX_LENGTH_LIST = List
            .of(SCHEME_VALIDATION_NAME_MAX_LENGTH);

    public final static SchemeEntity SCHEME_ENTITY_EXAMPLE = new SchemeEntity(SAMPLE_SCHEME_ID, SAMPLE_ORGANISATION_ID,
            1, Instant.parse("2022-07-02T15:00:00.00Z"), SAMPLE_USER_ID, Instant.parse("2022-07-02T16:00:00.00Z"),
            SAMPLE_USER_ID, SAMPLE_GGIS_REFERENCE, SAMPLE_SCHEME_NAME, SAMPLE_SCHEME_CONTACT);

    public final static List<SchemeEntity> SCHEME_ENTITY_LIST_EXAMPLE = Collections
            .singletonList(SCHEME_ENTITY_EXAMPLE);

    public final static Pageable EXAMPLE_PAGINATION_PROPS = PageRequest.of(0, 20);

    public final static String SCHEME_POST_ALL_NULL_DTO_JSON = "{" + "\"fieldErrors\":[" + "{"
            + "	\"fieldName\":\"name\"," + "	\"errorMessage\":\"Enter the name of your grant\"" + "}," + "{"
            + "	\"fieldName\":\"ggisReference\"," + "	\"errorMessage\":\"Enter your GGIS Scheme Reference Number\""
            + "} " + "]" + "}";

    public final static String SCHEME_PATCH_DTO_JSON = "    {\n" + "        \"name\": \"" + SAMPLE_SCHEME_NAME + "\",\n"
            + "        \"ggisReference\": \"" + SAMPLE_GGIS_REFERENCE + "\"\n" + "    }\n";

    public final static String SCHEME_PATCH_DTO_NULL_JSON = "    {\n" + "        \"grantName\": \"" + SAMPLE_SCHEME_NAME
            + "\",\n" + "        \"ggisReferenceNumber\": \"" + SAMPLE_GGIS_REFERENCE + "\"\n" + "    }\n";

    public final static String SCHEME_PATCH_DTO_CLASS_ERRORS_ALL_NULL = "{\"classErrors\": " + "	[" + "		{"
            + "			\"className\":\"schemePatchDTO\","
            + "			\"errorMessage\":\"All checked fields were null\"" + "		}" + "	]" + "}";

    public final static String SCHEME_PATCH_DTO_EMPTY_PROPERTIES = "    {\n" + "        \"name\": \"\"" + "    }\n";

    public final static String SCHEME_PATCH_DTO_INVALID_PROPERTIES_MAX_LENGTH = "    {\n" + "        \"name\": \""
            + SAMPLE_INVALID_NAME + "\"\n" + "    }\n";

    public final static FieldErrorsDTO SCHEME_PATCH_BLANK_VALIDATION_ERROR_JSON = new FieldErrorsDTO(
            Collections.singletonList(new ValidationError("name", "Enter the name of your grant")));

    public final static FieldErrorsDTO SCHEME_PATCH_MAX_LENGTH_VALIDATION_ERROR_JSON = new FieldErrorsDTO(
            Collections.singletonList(new ValidationError("name", "Name should not be greater than 255 characters")));

    public final static String SCHEME_PATCH_DTO_INVALID_JSON = "    {\n" + "        \"grantName\": \""
            + SAMPLE_SCHEME_NAME + "\" \n" + "        \"ggisReferenceNumber\": \"" + SAMPLE_GGIS_REFERENCE + "\" \n"
            + "    }\n";

    public final static String EXPECTED_SINGLE_SCHEME_JSON_RESPONSE = "    {\n" + "        \"schemeId\": "
            + SAMPLE_SCHEME_ID + ",\n" + "        \"funderId\": " + SAMPLE_ORGANISATION_ID + ",\n"
            + "        \"name\": \"" + SAMPLE_SCHEME_NAME + "\",\n" + "        \"ggisReference\": \""
            + SAMPLE_GGIS_REFERENCE + "\"\n" + "    }\n";

}
