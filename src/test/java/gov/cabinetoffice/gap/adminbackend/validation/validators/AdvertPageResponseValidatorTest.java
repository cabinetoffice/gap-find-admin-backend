package gov.cabinetoffice.gap.adminbackend.validation.validators;

import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GrantAdvertPageResponseValidationDto;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.ConvertHtmlToMdException;
import gov.cabinetoffice.gap.adminbackend.models.*;
import io.github.furstenheim.CopyDown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.ADVERT_DATES_SECTION_ID;
import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.CLOSING_DATE_ID;
import static gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator.OPENING_DATE_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertPageResponseValidatorTest {

    @Mock
    private AdvertDefinition advertDefinition;

    @Mock
    private CopyDown converter;

    private ConstraintValidatorContext validatorContext;

    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @InjectMocks
    private AdvertPageResponseValidator validator;

    private final AdvertDefinitionQuestion mandatoryQuestion = AdvertDefinitionQuestion.builder()
            .id("mandatoryQuestion").responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().mandatory(true).build()).build();

    private final AdvertDefinitionQuestion minLengthQuestion = AdvertDefinitionQuestion.builder()
            .id("minLengthQuestion").responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().minLength(2).build()).build();

    private final AdvertDefinitionQuestion maxLengthQuestion = AdvertDefinitionQuestion.builder()
            .id("maxLengthQuestion").responseType(AdvertDefinitionQuestionResponseType.LONG_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().maxLength(256).build()).build();

    private final AdvertDefinitionQuestion urlQuestion = AdvertDefinitionQuestion.builder().id("urlQuestion")
            .responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().url(true).build()).build();

    private final AdvertDefinitionQuestion questionToCompare = AdvertDefinitionQuestion.builder()
            .id("questionToCompare").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder().build()).build();

    private final AdvertDefinitionQuestion greaterThanQuestion = AdvertDefinitionQuestion.builder()
            .id("greaterThanQuestion").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder()
                    .comparedTo(new ComparisonValidation("questionToCompare", null, true, false)).build())
            .build();

    private final AdvertDefinitionQuestion lessThanQuestion = AdvertDefinitionQuestion.builder().id("lessThanQuestion")
            .responseType(AdvertDefinitionQuestionResponseType.INTEGER).validation(AdvertDefinitionQuestionValidation
                    .builder().comparedTo(new ComparisonValidation("questionToCompare", null, false, true)).build())
            .build();

    private final AdvertDefinitionQuestion comparisonWithCustomError = AdvertDefinitionQuestion.builder()
            .id("comparisonWithCustomError").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder()
                    .comparedTo(new ComparisonValidation("questionToCompare", "failed to compare", true, true)).build())
            .build();

    private final AdvertDefinitionQuestion currencyFieldQuestion = AdvertDefinitionQuestion.builder()
            .id("currencyFieldQuestion").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder().build()).build();

    private final AdvertDefinitionQuestion currencyGreaterThanQuestion = AdvertDefinitionQuestion.builder()
            .id("currencyGreaterThanQuestion").responseType(AdvertDefinitionQuestionResponseType.CURRENCY)
            .validation(AdvertDefinitionQuestionValidation.builder().greaterThan(100).build()).build();

    private final AdvertDefinitionQuestion currencyLessThanQuestion = AdvertDefinitionQuestion.builder()
            .id("currencyLessThanQuestion").responseType(AdvertDefinitionQuestionResponseType.CURRENCY)
            .validation(AdvertDefinitionQuestionValidation.builder().lessThan(100).build()).build();

    private final AdvertDefinitionQuestion integerGreaterThanQuestion = AdvertDefinitionQuestion.builder()
            .id("integerGreaterThanQuestion").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder().greaterThan(100).build()).build();

    private final AdvertDefinitionQuestion integerLessThanQuestion = AdvertDefinitionQuestion.builder()
            .id("integerLessThanQuestion").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder().lessThan(100).build()).build();

    private final AdvertDefinitionQuestion mandatoryQuestionToCompare = AdvertDefinitionQuestion.builder()
            .id("mandatoryQuestionToCompare").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder().mandatory(true).build()).build();

    private final AdvertDefinitionQuestion nestedLessThanQuestion = AdvertDefinitionQuestion.builder()
            .id("nestedLessThanQuestion").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder()
                    .comparedTo(new ComparisonValidation("mandatoryLessThanQuestion", null, false, true)).build())
            .build();

    private final AdvertDefinitionQuestion mandatoryLessThanQuestion = AdvertDefinitionQuestion.builder()
            .id("mandatoryLessThanQuestion").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder()
                    .comparedTo(new ComparisonValidation("mandatoryQuestionToCompare", null, false, true)).build())
            .build();

    private final AdvertDefinitionQuestion richTextQuestionMandatory = AdvertDefinitionQuestion.builder()
            .id("richTextQuestionMandatory").responseType(AdvertDefinitionQuestionResponseType.RICH_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().mandatory(true).build()).build();

    private final AdvertDefinitionQuestion richTextQuestionMinLenght = AdvertDefinitionQuestion.builder()
            .id("richTextQuestionMinLenght").responseType(AdvertDefinitionQuestionResponseType.RICH_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().minLength(2).build()).build();

    private final AdvertDefinitionQuestion richTextQuestionMaxLenght = AdvertDefinitionQuestion.builder()
            .id("richTextQuestionMaxLenght").responseType(AdvertDefinitionQuestionResponseType.RICH_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().maxLength(256).build()).build();

    private final AdvertDefinitionQuestion mandatoryQuestionCustomMessage = AdvertDefinitionQuestion.builder()
            .id("mandatoryQuestionCustomMessage").responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().mandatory(true).build())
            .validationMessages(AdvertDefinitionQuestionValidationMessages.builder()
                    .mandatory("This is a required question").build())
            .build();

    private final AdvertDefinitionQuestion minLengthQuestionCustomMessage = AdvertDefinitionQuestion.builder()
            .id("minLengthQuestionCustomMessage").responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().minLength(10).build())
            .validationMessages(AdvertDefinitionQuestionValidationMessages.builder()
                    .minLength("You are required to provide 10 characters").build())
            .build();

    private final AdvertDefinitionQuestion maxLengthQuestionCustomMessage = AdvertDefinitionQuestion.builder()
            .id("maxLengthQuestionCustomMessage").responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().maxLength(10).build())
            .validationMessages(AdvertDefinitionQuestionValidationMessages.builder()
                    .maxLength("You are required to provide less than 10 characters").build())
            .build();

    private final AdvertDefinitionQuestion urlQuestionCustomMessage = AdvertDefinitionQuestion.builder()
            .id("urlQuestionCustomMessage").responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
            .validation(AdvertDefinitionQuestionValidation.builder().url(true).build())
            .validationMessages(AdvertDefinitionQuestionValidationMessages.builder()
                    .url("Please provide url to your personal site").build())
            .build();

    private final AdvertDefinitionQuestion lessThanQuestionCustomMessage = AdvertDefinitionQuestion.builder()
            .id("lessThanQuestionCustomMessage").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder().lessThan(10).build())
            .validationMessages(AdvertDefinitionQuestionValidationMessages.builder()
                    .lessThan("Number provided must be less than 10").build())
            .build();

    private final AdvertDefinitionQuestion greaterThanQuestionCustomMessage = AdvertDefinitionQuestion.builder()
            .id("greaterThanQuestionCustomMessage").responseType(AdvertDefinitionQuestionResponseType.INTEGER)
            .validation(AdvertDefinitionQuestionValidation.builder().greaterThan(100).build())
            .validationMessages(AdvertDefinitionQuestionValidationMessages.builder()
                    .greaterThan("Number provided must be greater than 100").build())
            .build();

    @BeforeEach
    void setup() {
        nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        validatorContext = mock(ConstraintValidatorContext.class);

        validator = new AdvertPageResponseValidator(advertDefinition);

    }

    private static Stream<Arguments> genericResponses_invalid() {

        UUID grantAdvertId = UUID.fromString("d70f787e-993f-41bd-89b7-9da657a6847d");

        return Stream.of(

                // mandatory field missing
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("mandatoryQuestion")
                                        .response(null).build()))
                                .build())
                        .build(), "You must enter an answer"),

                // max length exceeded
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().questions(List.of(GrantAdvertQuestionResponse.builder()
                                .id("maxLengthQuestion")
                                .response("Veryloooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                        + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                        + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                        + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                        + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                        + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                        + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong")
                                .build())).build())
                        .build(), "Answer must be 256 characters or less"),

                // min length not met
                Arguments.of(
                        GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                                .sectionId("grantDetails")
                                .page(GrantAdvertPageResponse.builder()
                                        .questions(List.of(GrantAdvertQuestionResponse.builder().id("minLengthQuestion")
                                                .response("1").build()))
                                        .build())
                                .build(),
                        "Answer must be 2 characters or more"),

                // url not met
                Arguments.of(
                        GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                                .sectionId("grantDetails")
                                .page(GrantAdvertPageResponse.builder()
                                        .questions(List.of(GrantAdvertQuestionResponse.builder().id("urlQuestion")
                                                .response("notAUrl").build()))
                                        .build())
                                .build(),
                        "You must enter a valid link"),

                // greater than validation not met
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().questions(List.of(
                                GrantAdvertQuestionResponse.builder().id("questionToCompare").response("100").build(),
                                GrantAdvertQuestionResponse.builder().id("greaterThanQuestion").response("50").build()))
                                .build())
                        .build(), "Answer must be higher than 100"),

                // less than validation not met
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().questions(List.of(
                                GrantAdvertQuestionResponse.builder().id("questionToCompare").response("100").build(),
                                GrantAdvertQuestionResponse.builder().id("lessThanQuestion").response("150").build()))
                                .build())
                        .build(), "Answer must be lower than 100"),

                // comparison with invalid response
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().questions(List.of(
                                GrantAdvertQuestionResponse.builder().id("questionToCompare")
                                        .response("this-is-not-a-number").build(),
                                GrantAdvertQuestionResponse.builder().id("lessThanQuestion").response("150").build()))
                                .build())
                        .build(), "You must only enter numbers"),

                // comparison with invalid response
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().questions(List.of(
                                GrantAdvertQuestionResponse.builder().id("questionToCompare").response("100.50")
                                        .build(),
                                GrantAdvertQuestionResponse.builder().id("lessThanQuestion").response("150").build()))
                                .build())
                        .build(), "You must only enter numbers"),

                // status missing
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(null)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("mandatoryQuestion")
                                        .response(null).build()))
                                .build())
                        .build(), "Select 'Yes, I've completed this question', or 'No, I'll come back later'"),

                // Integer field String provided
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("currencyFieldQuestion")
                                        .response("notACurrency123").build()))
                                .build())
                        .build(), "You must only enter numbers"),

                // Integer field not whole number provided
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("currencyFieldQuestion")
                                        .response("100.50").build()))
                                .build())
                        .build(), "You must only enter numbers"),

                // Integer field lower than higher than amount
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("integerGreaterThanQuestion").response("50").build()))
                                .build())
                        .build(), "Answer must be higher than 100"),

                // Integer field equal to higher than amount
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("integerGreaterThanQuestion").response("100").build()))
                                .build())
                        .build(), "Answer must be higher than 100"),

                // Integer field higher than less than amount
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("integerLessThanQuestion")
                                        .response("150").build()))
                                .build())
                        .build(), "Answer must be lower than 100"),

                // Integer field equal to less than amount
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("integerLessThanQuestion")
                                        .response("100").build()))
                                .build())
                        .build(), "Answer must be lower than 100"),

                // Currency field lower than greater than amount
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("currencyGreaterThanQuestion").response("50").build()))
                                .build())
                        .build(), "Answer must be higher than 100"),

                // Currency field higher than less than amount
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("currencyLessThanQuestion")
                                        .response("150").build()))
                                .build())
                        .build(), "Answer must be lower than 100"),

                // Currency field equal to lower than amount
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("currencyLessThanQuestion")
                                        .response("100").build()))
                                .build())
                        .build(), "Answer must be lower than 100"),

                // Currency field equal to greater than amount
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("currencyGreaterThanQuestion").response("100").build()))
                                .build())
                        .build(), "Answer must be higher than 100"),

                // Rich Text Mandatory left blank
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("richTextQuestionMandatory")
                                        .multiResponse(new String[] { "", "contentful" }).build()))
                                .build())
                        .build(), "You must enter an answer"),

                // Rich Text max length exceeded
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().questions(List.of(GrantAdvertQuestionResponse.builder()
                                .id("richTextQuestionMaxLenght")
                                .multiResponse(new String[] {
                                        "Veryloooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                                + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                                + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                                + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                                + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                                + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                                                + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong",
                                        "contentful" })
                                .build())).build())
                        .build(), "Answer must be 256 characters or less"),

                // Rich Text min length not met
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("richTextQuestionMinLenght")
                                        .multiResponse(new String[] { "1", "contentful" }).build()))
                                .build())
                        .build(), "Answer must be 2 characters or more"));
    }

    private static Stream<Arguments> customResponses_invalid() {

        UUID grantAdvertId = UUID.fromString("d70f787e-993f-41bd-89b7-9da657a6847d");

        return Stream.of(

                // Comparison with custom error message
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().questions(List.of(
                                GrantAdvertQuestionResponse.builder().id("questionToCompare").response("100").build(),
                                GrantAdvertQuestionResponse.builder().id("comparisonWithCustomError").response("100")
                                        .build()))
                                .build())
                        .build(), "failed to compare"),

                // Comparison question missing
                Arguments.of(
                        GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                                .sectionId("grantDetails")
                                .page(GrantAdvertPageResponse.builder()
                                        .questions(List.of(GrantAdvertQuestionResponse.builder()
                                                .id("greaterThanQuestion").response("50").build()))
                                        .build())
                                .build(),
                        "Unable to find linked question to validate against.")

        );
    }

    @ParameterizedTest
    @MethodSource({ "genericResponses_invalid", "customResponses_invalid" })
    void validateGeneric_returnsFalseWhenInvalidResponseProvided(GrantAdvertPageResponseValidationDto response,
            String message) {
        AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id("grantDetails")
                .pages(Collections.singletonList(AdvertDefinitionPage.builder()
                        .questions(List.of(mandatoryQuestion, minLengthQuestion, maxLengthQuestion, urlQuestion,
                                questionToCompare, greaterThanQuestion, lessThanQuestion, comparisonWithCustomError,
                                currencyFieldQuestion, integerGreaterThanQuestion, integerLessThanQuestion,
                                currencyGreaterThanQuestion, currencyLessThanQuestion, richTextQuestionMandatory,
                                richTextQuestionMinLenght, richTextQuestionMaxLenght))
                        .build()))
                .build();
        when(advertDefinition.getSectionById(anyString())).thenReturn(definitionSection);
        when(validatorContext.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(builder);
        when(builder.addPropertyNode(Mockito.anyString())).thenReturn(nodeBuilder);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext).buildConstraintViolationWithTemplate(message);
        assertThat(isValid).isFalse();

    }

    private static Stream<Arguments> complexComparisons_invalid() {

        UUID grantAdvertId = UUID.fromString("d70f787e-993f-41bd-89b7-9da657a6847d");

        return Stream.of(

                // Comparison to mandatory question which is left blank
                Arguments.of(
                        GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                                .sectionId("grantDetails")
                                .page(GrantAdvertPageResponse.builder()
                                        .questions(List.of(
                                                GrantAdvertQuestionResponse.builder().id("mandatoryQuestionToCompare")
                                                        .response("").build(),
                                                GrantAdvertQuestionResponse.builder().id("mandatoryLessThanQuestion")
                                                        .response("100").build()))
                                        .build())
                                .build(),
                        "You must enter an answer"),

                // Nested Comparisons - Comparison to mandatory question which is left
                // blank
                Arguments.of(
                        GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                                .sectionId("grantDetails")
                                .page(GrantAdvertPageResponse.builder()
                                        .questions(List.of(
                                                GrantAdvertQuestionResponse.builder().id("mandatoryQuestionToCompare")
                                                        .response("").build(),
                                                GrantAdvertQuestionResponse.builder().id("mandatoryLessThanQuestion")
                                                        .response("100").build(),
                                                GrantAdvertQuestionResponse.builder().id("nestedLessThanQuestion")
                                                        .response("50").build()))
                                        .build())
                                .build(),
                        "You must enter an answer"),

                // Nested Comparisons - show lowest level comparison fail
                Arguments.of(
                        GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                                .sectionId("grantDetails").page(GrantAdvertPageResponse.builder()
                                        .questions(List.of(GrantAdvertQuestionResponse.builder()
                                                .id("mandatoryQuestionToCompare").response("75").build(),
                                                GrantAdvertQuestionResponse.builder().id("mandatoryLessThanQuestion")
                                                        .response("100").build(),
                                                GrantAdvertQuestionResponse.builder().id("nestedLessThanQuestion")
                                                        .response("50").build()))
                                        .build())
                                .build(),
                        "Answer must be lower than 75"),

                // Nested Comparisons - show lowest level comparison fail
                Arguments.of(
                        GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                                .sectionId("grantDetails")
                                .page(GrantAdvertPageResponse.builder()
                                        .questions(List.of(
                                                GrantAdvertQuestionResponse.builder().id("mandatoryQuestionToCompare")
                                                        .response("150").build(),
                                                GrantAdvertQuestionResponse.builder().id("mandatoryLessThanQuestion")
                                                        .response("25").build(),
                                                GrantAdvertQuestionResponse.builder().id("nestedLessThanQuestion")
                                                        .response("50").build()))
                                        .build())
                                .build(),
                        "Answer must be lower than 25")

        );
    }

    @ParameterizedTest
    @MethodSource("complexComparisons_invalid")
    void validateComplexComparison_returnsFalseWhenInvalidResponseProvided(
            GrantAdvertPageResponseValidationDto response, String message) {

        AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id("grantDetails")
                .pages(Collections.singletonList(AdvertDefinitionPage.builder()
                        .questions(
                                List.of(mandatoryQuestionToCompare, mandatoryLessThanQuestion, nestedLessThanQuestion))
                        .build()))
                .build();
        when(advertDefinition.getSectionById(anyString())).thenReturn(definitionSection);
        when(validatorContext.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(builder);
        when(builder.addPropertyNode(Mockito.anyString())).thenReturn(nodeBuilder);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext).buildConstraintViolationWithTemplate(message);
        assertThat(isValid).isFalse();

    }

    private static Stream<Arguments> genericResponses_valid() {

        UUID grantAdvertId = UUID.fromString("d70f787e-993f-41bd-89b7-9da657a6847d");

        return Stream.of(

                // mandatory field
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("mandatoryQuestion")
                                        .response("This is an answer").build()))
                                .build())
                        .build()),

                // max length
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("maxLengthQuestion")
                                        .response("Sort of looooooooooooooooooong answer-ish").build()))
                                .build())
                        .build()),

                // min length
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("minLengthQuestion")
                                        .response("22").build()))
                                .build())
                        .build()),

                // url question
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("urlQuestion")
                                        .response("https://www.google.com/").build()))
                                .build())
                        .build()),

                // greater than
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(
                                        GrantAdvertQuestionResponse.builder().id("questionToCompare").response("100")
                                                .build(),
                                        GrantAdvertQuestionResponse.builder().id("greaterThanQuestion").response("150")
                                                .build()))
                                .build())
                        .build()),

                // less than
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(
                                        GrantAdvertQuestionResponse.builder().id("questionToCompare").response("100")
                                                .build(),
                                        GrantAdvertQuestionResponse.builder().id("lessThanQuestion").response("50")
                                                .build()))
                                .build())
                        .build()),

                // Integer field greater than
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("integerGreaterThanQuestion").response("150").build()))
                                .build())
                        .build()),

                // Integer field less than
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("integerLessThanQuestion")
                                        .response("50").build()))
                                .build())
                        .build()),

                // Currency field greater than
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("currencyGreaterThanQuestion").response("150").build()))
                                .build())
                        .build()),

                // Currency field less than
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("currencyLessThanQuestion")
                                        .response("50").build()))
                                .build())
                        .build()),

                // currency field validation
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("currencyFieldQuestion")
                                        .response("123").build()))
                                .build())
                        .build()),

                // Rich text max length
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("richTextQuestionMaxLenght")
                                        .multiResponse(new String[] { "Sort of looooooooooooooooooong answer-ish",
                                                "contentful" })
                                        .build()))
                                .build())
                        .build()),

                // Rich text min length
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("richTextQuestionMinLenght")
                                        .multiResponse(new String[] { "22", "contentful" }).build()))
                                .build())
                        .build())

        );
    }

    @ParameterizedTest
    @MethodSource("genericResponses_valid")
    void validateGeneric_returnsTrue(GrantAdvertPageResponseValidationDto response) {
        AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id("grantDetails")
                .pages(Collections.singletonList(AdvertDefinitionPage.builder()
                        .questions(List.of(mandatoryQuestion, minLengthQuestion, maxLengthQuestion, urlQuestion,
                                questionToCompare, greaterThanQuestion, lessThanQuestion, currencyFieldQuestion,
                                integerGreaterThanQuestion, integerLessThanQuestion, currencyGreaterThanQuestion,
                                currencyLessThanQuestion, richTextQuestionMaxLenght, richTextQuestionMinLenght))
                        .build()))
                .build();
        when(advertDefinition.getSectionById(anyString())).thenReturn(definitionSection);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext, never()).buildConstraintViolationWithTemplate(any());
        assertThat(isValid).isTrue();

    }

    private static Stream<Arguments> complexComparisons_valid() {

        UUID grantAdvertId = UUID.fromString("d70f787e-993f-41bd-89b7-9da657a6847d");

        return Stream.of(

                // Comparison to optional question which is left blank
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.NOT_STARTED)
                                .questions(List.of(
                                        GrantAdvertQuestionResponse.builder().id("questionToCompare").response("")
                                                .build(),
                                        GrantAdvertQuestionResponse.builder().id("lessThanQuestion").response("100")
                                                .build()))
                                .build())
                        .build()

                ),

                // Nested Comparisons
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.NOT_STARTED)
                                .questions(List.of(
                                        GrantAdvertQuestionResponse.builder().id("mandatoryQuestionToCompare")
                                                .response("150").build(),
                                        GrantAdvertQuestionResponse.builder().id("mandatoryLessThanQuestion")
                                                .response("100").build(),
                                        GrantAdvertQuestionResponse.builder().id("nestedLessThanQuestion")
                                                .response("50").build()))
                                .build())
                        .build())

        );
    }

    @ParameterizedTest
    @MethodSource("complexComparisons_valid")
    void complexComparisons_returnsTrue(GrantAdvertPageResponseValidationDto response) {
        AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id("grantDetails")
                .pages(Collections
                        .singletonList(AdvertDefinitionPage
                                .builder().questions(List.of(questionToCompare, lessThanQuestion,
                                        mandatoryQuestionToCompare, mandatoryLessThanQuestion, nestedLessThanQuestion))
                                .build()))
                .build();
        when(advertDefinition.getSectionById(anyString())).thenReturn(definitionSection);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext, never()).buildConstraintViolationWithTemplate(any());
        assertThat(isValid).isTrue();

    }

    private static Stream<Arguments> provideInvalidDatesAndExpectedErrorMessages() {
        return Stream.of(Arguments.of(new String[] { "", "02", "2022" }, "Date must include a day"),
                Arguments.of(new String[] { "", "", "2022" }, "Date must include a day and a month"),
                Arguments.of(new String[] { "", "02", "" }, "Date must include a day and a year"),
                Arguments.of(new String[] { "01", "", "" }, "Date must include a month and a year"),
                Arguments.of(new String[] { "", "", "" }, "You must enter a date"),
                Arguments.of(new String[] { "02", "", "2022" }, "Date must include a month"),
                Arguments.of(new String[] { "02", "02", "" }, "Date must include a year"),
                Arguments.of(new String[] { "INVALID", "02", "2022" }, "Date must include a real day"),
                Arguments.of(new String[] { "02", "INVALID", "2022" }, "Date must include a real month"),
                Arguments.of(new String[] { "02", "02", "INVALID" }, "Date must include a real year"),
                Arguments.of(new String[] { "31", "04", "2022" }, "Date must include a real day"),
                Arguments.of(new String[] { "31", "40", "2022" }, "Date must include a real month"),
                Arguments.of(new String[] { "29", "02", "2022" }, "Date must include a real day"),
                Arguments.of(new String[] { "0", "02", "2022" }, "Date must include a real day"),
                Arguments.of(new String[] { "-1", "02", "2022" }, "Date must include a real day"),
                Arguments.of(new String[] { "28", "13", "2022" }, "Date must include a real month"),
                Arguments.of(new String[] { "28", "0", "2022" }, "Date must include a real month"),
                Arguments.of(new String[] { "28", "-1", "2022" }, "Date must include a real month"),
                Arguments.of(new String[] { "29", "02", "-1" }, "Date must include a real year"),
                Arguments.of(new String[] { "29", "02", "999" }, "Date must include a real year"),
                Arguments.of(new String[] { "29", "02", "10000" }, "Date must include a real year"),
                Arguments.of(new String[] { "290", "02", "10000" }, "Date must include a real year and day"),
                Arguments.of(new String[] { "29", "-2", "10000" }, "Date must include a real month and year"),
                Arguments.of(new String[] { "290", "-2", "2023" }, "Date must include a real month and day"),
                Arguments.of(new String[] { "290", "-2", "10000" }, "Date must include a real month, year and day")

        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDatesAndExpectedErrorMessages")
    void validateDate_returnsFalseWhenInvalidResponseProvided(String[] date, String message) {
        GrantAdvertPageResponseValidationDto response = GrantAdvertPageResponseValidationDto.builder()
                .grantAdvertId(UUID.randomUUID()).sectionId(ADVERT_DATES_SECTION_ID)
                .page(GrantAdvertPageResponse.builder().id("1")
                        .questions(List.of(
                                GrantAdvertQuestionResponse.builder().id(OPENING_DATE_ID).multiResponse(date).build(),
                                GrantAdvertQuestionResponse.builder().id(CLOSING_DATE_ID).multiResponse(date).build()))
                        .build())
                .build();

        when(validatorContext.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(builder);
        when(builder.addPropertyNode(Mockito.anyString())).thenReturn(nodeBuilder);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext, times(2)).buildConstraintViolationWithTemplate(message);
        assertThat(isValid).isFalse();

    }

    private static Stream<Arguments> provideNonMatchingOpeningClosingDates() {
        return Stream.of(Arguments.of(new String[] { "1", "1", "2023" }, new String[] { "31", "12", "2022" }),
                Arguments.of(new String[] { "1", "1", "2022" }, new String[] { "1", "1", "2022" }));
    }

    @ParameterizedTest
    @MethodSource("provideNonMatchingOpeningClosingDates")
    void validateDate_returnsFalseClosingBeforeOpening(String[] openingDate, String[] closingDate) {
        GrantAdvertPageResponseValidationDto response = GrantAdvertPageResponseValidationDto.builder()
                .grantAdvertId(UUID.randomUUID()).sectionId(ADVERT_DATES_SECTION_ID)
                .page(GrantAdvertPageResponse.builder().id(ADVERT_DATES_SECTION_ID)
                        .status(GrantAdvertPageResponseStatus.COMPLETED)
                        .questions(List.of(
                                GrantAdvertQuestionResponse.builder().id(OPENING_DATE_ID).multiResponse(openingDate)
                                        .build(),
                                GrantAdvertQuestionResponse.builder().id(CLOSING_DATE_ID).multiResponse(closingDate)
                                        .build()))
                        .build())
                .build();

        when(validatorContext.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(builder);
        when(builder.addPropertyNode(Mockito.anyString())).thenReturn(nodeBuilder);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext)
                .buildConstraintViolationWithTemplate("The closing date must be later than the opening date");
        assertThat(isValid).isFalse();

    }

    // @formatter:off
    private static Stream<Arguments> validUrls() {
        return Stream.of(Arguments.of("https://www.google.co.uk"),
                Arguments.of("http://www.google.co.uk"),
                Arguments.of("https://www.google.com"),
                Arguments.of("http://www.google.com"),
                Arguments.of("https://google.co.uk"),
                Arguments.of("http://google.co.uk"),
                Arguments.of("https://google.co.uk/"),
                Arguments.of("http://google.co.uk/"),
                Arguments.of("https://www.google.co.uk/long/path"),
                Arguments.of("https://www.google.co.uk/long/path/"),
                Arguments.of("http://www.google.co.uk/long/path"),
                Arguments.of("http://www.google.co.uk/long/path/"),
                Arguments.of("https://google.co.uk/long/path/"),
                Arguments.of("http://google.co.uk/long/path/"),
                Arguments.of("https://www.google.co.uk?query=var&query2=var"),
                Arguments.of("http://www.google.co.uk?query=var&query2=var"),
                Arguments.of("https://google.co.uk?query=var&query2=var"),
                Arguments.of("http://google.co.uk?query=var&query2=var"),
                Arguments.of("https://www.google.co.uk/long/path?query=var&query2=var"),
                Arguments.of("http://www.google.co.uk/long/path?query=var&query2=var"),
                Arguments.of("https://google.co.uk/long/path?query=var&query2=var"),
                Arguments.of("http://google.co.uk/long/path?query=var&query2=var"));
    }
    // @formatter:on

    @ParameterizedTest
    @MethodSource("validUrls")
    void validateUrls_returnTrue(String url) {
        AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id("grantDetails")
                .pages(Collections.singletonList(
                        AdvertDefinitionPage.builder().questions(Collections.singletonList(urlQuestion)).build()))
                .build();

        GrantAdvertPageResponseValidationDto response = GrantAdvertPageResponseValidationDto.builder()
                .grantAdvertId(UUID.randomUUID()).sectionId("grantDetails")
                .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                        .questions(
                                List.of(GrantAdvertQuestionResponse.builder().id("urlQuestion").response(url).build()))
                        .build())
                .build();

        when(advertDefinition.getSectionById(anyString())).thenReturn(definitionSection);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext, never()).buildConstraintViolationWithTemplate(any());
        assertThat(isValid).isTrue();

    }

    // @formatter:off
    private static Stream<Arguments> invalidUrls() {
        String malformedUrlError = "You must enter a valid link";
        String linkShortenError = "You must enter the full link";

        return Stream.of(
                Arguments.of("https://google", malformedUrlError),
                Arguments.of("http://google", malformedUrlError),
                Arguments.of("https://www.google", malformedUrlError),
                Arguments.of("google", malformedUrlError),
                Arguments.of("www.google", malformedUrlError),
                Arguments.of("google.c", malformedUrlError),
                Arguments.of("www.b.c.d", malformedUrlError),
                Arguments.of("google/path", malformedUrlError),
                Arguments.of("ftp://www.google.co.uk/path", malformedUrlError),
                Arguments.of("file://www.google.co.uk/path", malformedUrlError),
                Arguments.of("sftp://www.google.co.uk", malformedUrlError),
                Arguments.of("https://tinyurl.com/abcedf", linkShortenError),
                Arguments.of("https://bit.ly/abcedf", linkShortenError),
                Arguments.of("http://ow.ly/abcedf", linkShortenError)
        );

    }
    // @formatter:on

    @ParameterizedTest
    @MethodSource("invalidUrls")
    void validateUrls_returnFalse(String url, String errorMessage) {
        AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id("grantDetails")
                .pages(Collections.singletonList(
                        AdvertDefinitionPage.builder().questions(Collections.singletonList(urlQuestion)).build()))
                .build();

        GrantAdvertPageResponseValidationDto response = GrantAdvertPageResponseValidationDto.builder()
                .grantAdvertId(UUID.randomUUID()).sectionId("grantDetails")
                .page(GrantAdvertPageResponse.builder().status(GrantAdvertPageResponseStatus.COMPLETED)
                        .questions(
                                List.of(GrantAdvertQuestionResponse.builder().id("urlQuestion").response(url).build()))
                        .build())
                .build();

        when(advertDefinition.getSectionById(anyString())).thenReturn(definitionSection);
        when(validatorContext.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(builder);
        when(builder.addPropertyNode(Mockito.anyString())).thenReturn(nodeBuilder);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext).buildConstraintViolationWithTemplate(errorMessage);
        assertThat(isValid).isFalse();

    }

    @Test
    void validateDate_returnsTrue() {
        String[] openingDate = new String[] { "1", "12", "2022" };
        String[] closingDate = new String[] { "31", "12", "2022" };
        GrantAdvertPageResponseValidationDto response = GrantAdvertPageResponseValidationDto.builder()
                .grantAdvertId(UUID.randomUUID()).sectionId(ADVERT_DATES_SECTION_ID)
                .page(GrantAdvertPageResponse.builder().id(ADVERT_DATES_SECTION_ID)
                        .status(GrantAdvertPageResponseStatus.COMPLETED)
                        .questions(List.of(
                                GrantAdvertQuestionResponse.builder().id(OPENING_DATE_ID).multiResponse(openingDate)
                                        .build(),
                                GrantAdvertQuestionResponse.builder().id(CLOSING_DATE_ID).multiResponse(closingDate)
                                        .build()))
                        .build())
                .build();

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext, never()).buildConstraintViolationWithTemplate(anyString());
        assertThat(isValid).isTrue();

    }

    @Test
    void shouldThrowConvertHtmlToMdException() {

        AdvertDefinitionPage advertDefinitionPage = AdvertDefinitionPage.builder().id("pageId")
                .questions(List.of(richTextQuestionMinLenght)).build();
        AdvertDefinitionSection advertDefinitionSection = AdvertDefinitionSection.builder().id("sectionId")
                .pages(List.of(advertDefinitionPage)).build();

        GrantAdvertQuestionResponse grantAdvertQuestionResponse = GrantAdvertQuestionResponse.builder()
                .id("richTextQuestionMinLenght").multiResponse(new String[] { "firstString", "ewq" }).build();
        GrantAdvertPageResponse grantAdvertPageResponse = GrantAdvertPageResponse.builder().id("pageId")
                .questions(List.of(grantAdvertQuestionResponse)).build();
        GrantAdvertPageResponseValidationDto grantAdvertPageResponseValidationDto = GrantAdvertPageResponseValidationDto
                .builder().sectionId("sectionId").page(grantAdvertPageResponse).build();
        when(advertDefinition.getSectionById("sectionId")).thenReturn(advertDefinitionSection);
        try (MockedConstruction<CopyDown> mocked = mockConstruction(CopyDown.class, ((copyDown, context) -> {
            when(copyDown.convert("firstString")).thenThrow(ConvertHtmlToMdException.class);
        }))) {

            assertThrows(ConvertHtmlToMdException.class,
                    () -> validator.validate(grantAdvertPageResponseValidationDto));
        }

    }

    private static Stream<Arguments> customErrorMessages_invalid() {

        UUID grantAdvertId = UUID.fromString("d70f787e-993f-41bd-89b7-9da657a6847d");

        return Stream.of(

                // Mandatory
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("mandatoryQuestionCustomMessage").response("").build()))
                                .build())
                        .build(), "This is a required question"),

                // Min Length
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("minLengthQuestionCustomMessage").response("Test").build()))
                                .build())
                        .build(), "You are required to provide 10 characters"),

                // Max Length
                Arguments.of(
                        GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                                .sectionId("grantDetails")
                                .page(GrantAdvertPageResponse.builder()
                                        .questions(List.of(GrantAdvertQuestionResponse.builder()
                                                .id("maxLengthQuestionCustomMessage").response("Too many characters")
                                                .build()))
                                        .build())
                                .build(),
                        "You are required to provide less than 10 characters"),

                // Max Length
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder().id("urlQuestionCustomMessage")
                                        .response("not-a-url").build()))
                                .build())
                        .build(), "Please provide url to your personal site"),

                // Less Than
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("lessThanQuestionCustomMessage").response("100").build()))
                                .build())
                        .build(), "Number provided must be less than 10"),

                // Greater Than
                Arguments.of(GrantAdvertPageResponseValidationDto.builder().grantAdvertId(grantAdvertId)
                        .sectionId("grantDetails")
                        .page(GrantAdvertPageResponse.builder()
                                .questions(List.of(GrantAdvertQuestionResponse.builder()
                                        .id("greaterThanQuestionCustomMessage").response("10").build()))
                                .build())
                        .build(), "Number provided must be greater than 100")

        );
    }

    @ParameterizedTest
    @MethodSource("customErrorMessages_invalid")
    void validate_returnsCustomValidationMessages(GrantAdvertPageResponseValidationDto response, String message) {
        AdvertDefinitionSection definitionSection = AdvertDefinitionSection.builder().id("grantDetails")
                .pages(Collections.singletonList(AdvertDefinitionPage.builder()
                        .questions(List.of(mandatoryQuestionCustomMessage, minLengthQuestionCustomMessage,
                                maxLengthQuestionCustomMessage, urlQuestionCustomMessage, lessThanQuestionCustomMessage,
                                greaterThanQuestionCustomMessage))
                        .build()))
                .build();
        when(advertDefinition.getSectionById(anyString())).thenReturn(definitionSection);
        when(validatorContext.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(builder);
        when(builder.addPropertyNode(Mockito.anyString())).thenReturn(nodeBuilder);

        boolean isValid = validator.isValid(response, validatorContext);

        verify(validatorContext).buildConstraintViolationWithTemplate(message);
        assertThat(isValid).isFalse();

    }

}