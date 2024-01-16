package gov.cabinetoffice.gap.adminbackend.validation.validators;

import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import gov.cabinetoffice.gap.adminbackend.models.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GrantAdvertPageResponseValidationDto;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertValidationType;
import gov.cabinetoffice.gap.adminbackend.exceptions.ConvertHtmlToMdException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.validation.ValidationResult;
import gov.cabinetoffice.gap.adminbackend.validation.annotations.ValidPageResponse;
import io.github.furstenheim.CopyDown;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class AdvertPageResponseValidator implements ConstraintValidator<ValidPageResponse, Object> {

    public static final String ADVERT_DATES_SECTION_ID = "applicationDates";

    public static final String OPENING_DATE_ID = "grantApplicationOpenDate";

    public static final String CLOSING_DATE_ID = "grantApplicationCloseDate";

    private final AdvertDefinition advertDefinition;

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {

        constraintValidatorContext.disableDefaultConstraintViolation();
        GrantAdvertPageResponseValidationDto submittedPage = (GrantAdvertPageResponseValidationDto) value;

        ValidationResult result;
        // if the page is for opening/closing date, this is a special validation case
        // else, generic validation
        if (Objects.equals(submittedPage.getSectionId(), ADVERT_DATES_SECTION_ID)) {
            result = validateAdvertDates(submittedPage);
        }
        else {
            result = validate(submittedPage);
        }

        if (!result.isValid()) {
            for (Map.Entry<String, String> entry : result.getFieldErrors().entrySet()) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(entry.getValue())
                        .addPropertyNode(entry.getKey()).addConstraintViolation();
            }
        }

        return result.isValid();
    }

    public ValidationResult validate(GrantAdvertPageResponseValidationDto submittedPage) {

        ValidationResult result = ValidationResult.builder().build();

        // validate each question
        for (GrantAdvertQuestionResponse question : submittedPage.getPage().getQuestions()) {

            SimpleEntry<String, String> fieldError = validateQuestion(submittedPage.getSectionId(),
                    submittedPage.getPage(), question);

            if (fieldError != null) {
                result.addError(fieldError.getKey(), fieldError.getValue());
            }
        }

        if (submittedPage.getPage().getStatus() == null) {
            result.addError("completed", "Select 'Yes, I've completed this question', or 'No, I'll come back later'");
        }

        if (result.getFieldErrors().isEmpty())
            result.setValid(Boolean.TRUE);

        return result;
    }

    private SimpleEntry<String, String> validateQuestion(String sectionId, GrantAdvertPageResponse page,
            GrantAdvertQuestionResponse question) {
        // retrieve question and validation rules from static definition
        AdvertDefinitionQuestion questionDefinition = advertDefinition.getSectionById(sectionId)
                .getPageById(page.getId()).getQuestionById(question.getId());
        AdvertDefinitionQuestionValidation validation = questionDefinition.getValidation();
        AdvertDefinitionQuestionValidationMessages validationMessages = questionDefinition.getValidationMessages();

        final boolean isRichText = questionDefinition.getResponseType()
                .equals(AdvertDefinitionQuestionResponseType.RICH_TEXT);

        // mandatory validation
        final boolean singleResponseIsEmpty = StringUtils.isEmpty(question.getResponse());
        final boolean multiResponseIsEmpty = arrayAnswerIsEmpty(question.getMultiResponse(), isRichText);
        if (validation.isMandatory()) {
            // if question is mandatory and responses are empty, fail validation
            if (singleResponseIsEmpty && multiResponseIsEmpty) {
                return getMandatoryFieldViolationMessage(questionDefinition.getResponseType(), question.getId(),
                        validationMessages);
            }
        }
        else {
            // if question is optional and responses are empty, pass validation
            if (singleResponseIsEmpty && multiResponseIsEmpty) {
                return null;
            }
        }

        // will be used to convert Html to Markdown
        CopyDown converter = new CopyDown();
        String convertedHtml = "";
        if (isRichText) {
            try {
                convertedHtml = converter.convert(question.getMultiResponse()[0]);
            }
            catch (Exception e) {
                throw new ConvertHtmlToMdException(
                        String.format("Something went wrong trying to convert %s into markdown format",
                                question.getMultiResponse()[0]));
            }
        }

        // min length validation
        SimpleEntry<String, String> question2 = validateMinLength(question, validation, validationMessages, isRichText,
                convertedHtml);
        if (question2 != null)
            return question2;

        /*
         * URL validation - examples of valid url patterns: https://www.google.com |
         * http://www.google.com | https://google.com
         * https://www.google.com/nested/page.html | https://www.google.co.uk?query=true
         * https://www.google.co.uk/nested?query=true
         */
        if (validation.isUrl()) {
            SimpleEntry<String, String> question1 = validateURL(question, validationMessages);
            if (question1 != null)
                return question1;
        }

        // Integer / currency field validation
        SimpleEntry<String, String> question1 = validateNumericField(question, questionDefinition, validation,
                validationMessages);
        if (question1 != null)
            return question1;

        // comparison validation
        if (validation.getComparedTo() != null) {
            ComparisonValidation comparisonValidation = validation.getComparedTo();

            try {
                GrantAdvertQuestionResponse questionToCompare = page
                        .getQuestionById(comparisonValidation.getQuestionId()).orElseThrow(NotFoundException::new);

                SimpleEntry<String, String> validateQuestion = validateQuestion(sectionId, page, questionToCompare);

                // If the linked question has validation errors
                if (validateQuestion != null) {
                    // don't compare
                    return null;
                }

                return validateComparison(comparisonValidation, questionToCompare, question, questionDefinition,
                        question.getId());

            }
            catch (NotFoundException nfe) {
                return new SimpleEntry<>(question.getId(), "Unable to find linked question to validate against.");
            }

        }

        return null;
    }

    @Nullable
    private SimpleEntry<String, String> validateMinLength(GrantAdvertQuestionResponse question,
            AdvertDefinitionQuestionValidation validation,
            AdvertDefinitionQuestionValidationMessages validationMessages, boolean isRichText, String convertedHtml) {
        if (validation.getMinLength() != null
                && ((!isRichText && question.getResponse().length() < validation.getMinLength())
                        || (isRichText && convertedHtml.length() < validation.getMinLength()))) {
            String customMinLengthErrorMessage = getCustomErrorMessage(GrantAdvertValidationType.MIN_LENGTH,
                    validationMessages);

            return new SimpleEntry<>(question.getId(), customMinLengthErrorMessage != null ? customMinLengthErrorMessage
                    : String.format("Answer must be %s characters or more", validation.getMinLength()));
        }

        // max length validation
        if (validation.getMaxLength() != null
                && ((!isRichText && question.getResponse().length() > validation.getMaxLength())
                        || (isRichText && convertedHtml.length() > validation.getMaxLength()))) {
            String customMaxLengthErrorMessage = getCustomErrorMessage(GrantAdvertValidationType.MAX_LENGTH,
                    validationMessages);

            return new SimpleEntry<>(question.getId(), customMaxLengthErrorMessage != null ? customMaxLengthErrorMessage
                    : String.format("Answer must be %s characters or less", validation.getMaxLength()));
        }
        return null;
    }

    @Nullable
    private SimpleEntry<String, String> validateURL(GrantAdvertQuestionResponse question,
            AdvertDefinitionQuestionValidationMessages validationMessages) {
        // (mandatory) protocol | (optional) www | domain, can't be www | .subdomain
        // (incl. .com) (repeating) |
        // (optional) slash | (optional) additional path | (optional) slash |
        // (optional) query params
        String urlValidPattern = "^(http://|https://)(www.)?((?!www)[a-zA-Z0-9\\-]{2,}+)(\\.[a-zA-Z0-9\\-]{2,})+(/)?(/[a-z0-9\\-._~%!$&'()*+,;=:@]+)*?(/)?(\\?[a-z0-9\\-._~%!$&'()*+,;=:@/]*)?$";
        Pattern pattern = Pattern.compile(urlValidPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(question.getResponse());

        String customUrlErrorMessage = getCustomErrorMessage(GrantAdvertValidationType.URL, validationMessages);

        if (!matcher.find())
            return new SimpleEntry<>(question.getId(),
                    customUrlErrorMessage != null ? customUrlErrorMessage : "You must enter a valid link");

        // check against list of known url shortening sites
        List<String> shortUrlProviders = Arrays.asList("Bit.ly", "TinyURL", "Ow.ly");
        boolean found = shortUrlProviders.stream().anyMatch(
                provider -> question.getResponse().toLowerCase(Locale.UK).contains(provider.toLowerCase(Locale.UK)));
        if (found)
            return new SimpleEntry<>(question.getId(),
                    customUrlErrorMessage != null ? customUrlErrorMessage : "You must enter the full link");
        return null;
    }

    @Nullable
    private SimpleEntry<String, String> validateNumericField(GrantAdvertQuestionResponse question,
            AdvertDefinitionQuestion questionDefinition, AdvertDefinitionQuestionValidation validation,
            AdvertDefinitionQuestionValidationMessages validationMessages) {
        if (questionDefinition.getResponseType().equals(AdvertDefinitionQuestionResponseType.INTEGER)
                || questionDefinition.getResponseType().equals(AdvertDefinitionQuestionResponseType.CURRENCY)) {
            try {
                int response = Integer.parseInt(question.getResponse());

                if (validation.getGreaterThan() != null && response <= validation.getGreaterThan()) {
                    String customGreaterThanErrorMessage = getCustomErrorMessage(GrantAdvertValidationType.GREATER_THAN,
                            validationMessages);

                    return new SimpleEntry<>(question.getId(),
                            customGreaterThanErrorMessage != null ? customGreaterThanErrorMessage
                                    : String.format("Answer must be higher than %s", validation.getGreaterThan()));
                }

                if (validation.getLessThan() != null && response >= validation.getLessThan()) {
                    String customLessThanErrorMessage = getCustomErrorMessage(GrantAdvertValidationType.LESS_THAN,
                            validationMessages);

                    return new SimpleEntry<>(question.getId(),
                            customLessThanErrorMessage != null ? customLessThanErrorMessage
                                    : String.format("Answer must be lower than %s", validation.getLessThan()));
                }
            }
            catch (final NumberFormatException e) {
                return new SimpleEntry<>(question.getId(), "You must only enter numbers");
            }
        }
        return null;
    }

    private SimpleEntry<String, String> validateComparison(ComparisonValidation comparisonRules,
            GrantAdvertQuestionResponse questionToCompare, GrantAdvertQuestionResponse questionToValidate,
            AdvertDefinitionQuestion questionDefinition, String questionId) {

        if (questionDefinition.getResponseType().equals(AdvertDefinitionQuestionResponseType.INTEGER)
                || questionDefinition.getResponseType().equals(AdvertDefinitionQuestionResponseType.CURRENCY)) {

            try {
                if (comparisonRules.isGreaterThan() && Integer.parseInt(questionToValidate.getResponse()) <= Integer
                        .parseInt(questionToCompare.getResponse())) {

                    return new SimpleEntry<>(questionId,
                            !StringUtils.isEmpty(comparisonRules.getErrorMessage()) ? comparisonRules.getErrorMessage()
                                    : String.format("Answer must be higher than %s", questionToCompare.getResponse()));
                }

                if (comparisonRules.isLessThan() && Integer.parseInt(questionToValidate.getResponse()) >= Integer
                        .parseInt(questionToCompare.getResponse())) {

                    return new SimpleEntry<>(questionId,
                            !StringUtils.isEmpty(comparisonRules.getErrorMessage()) ? comparisonRules.getErrorMessage()
                                    : String.format("Answer must be lower than %s", questionToCompare.getResponse()));
                }
            }
            catch (NumberFormatException nfe) {
                // This should only happen if the linked question is not mandatory and is
                // left blank
                // So treat this comparison as optional
                return null;
            }

        }
        return null;
    }

    private ValidationResult validateAdvertDates(GrantAdvertPageResponseValidationDto submittedPage) {
        ValidationResult result = ValidationResult.builder().build();

        GrantAdvertQuestionResponse openingDateQuestion = submittedPage.getPage().getQuestionById(OPENING_DATE_ID)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Failed to find question with id %s", OPENING_DATE_ID)));
        GrantAdvertQuestionResponse closingDateQuestion = submittedPage.getPage().getQuestionById(CLOSING_DATE_ID)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Failed to find question with id %s", CLOSING_DATE_ID)));

        List<GrantAdvertQuestionResponse> grantAdvertQuestionResponses = List.of(openingDateQuestion,
                closingDateQuestion);

        // check both date are valid
        grantAdvertQuestionResponses.forEach(question -> {
            AbstractMap.SimpleEntry<String, String> dateValidation = validateDate(question.getMultiResponse(), true,
                    question.getId(), submittedPage);
            if (dateValidation != null) {
                result.addError(dateValidation.getKey(), dateValidation.getValue());
            }
        });

        if (submittedPage.getPage().getStatus() == null) {
            result.addError("completed", "Select 'Yes, I've completed this question', or 'No, I'll come back later'");
        }

        // if there are any date validation errors (and it's not only the status error)
        // return the errors, don't attempt to validate closing date against opening
        if (!result.getFieldErrors().isEmpty()
                && !(result.getFieldErrors().size() == 1 && result.getFieldErrors().containsKey("completed"))) {
            return result;
        }

        // convert the string[] to int[], to easily build Calendars
        int[] openingResponse = Arrays.stream(openingDateQuestion.getMultiResponse())
                .flatMapToInt(AdvertPageResponseValidator::parseTimeStringToInt).toArray();
        int[] closingResponse = Arrays.stream(closingDateQuestion.getMultiResponse())
                .flatMapToInt(AdvertPageResponseValidator::parseTimeStringToInt).toArray();

        // build Calendar objs to compare
        Calendar openingDate = new Calendar.Builder()
                .setDate(openingResponse[2], openingResponse[1], openingResponse[0])
                .setTimeOfDay(openingResponse[3], openingResponse[4], 0).build();
        Calendar closingDate = new Calendar.Builder()
                .setDate(closingResponse[2], closingResponse[1], closingResponse[0])
                .setTimeOfDay(openingResponse[3], openingResponse[4], 0).build();

        // c o m p a r e
        if (openingDate.compareTo(closingDate) >= 0) {
            result.addError(String.format("%s-%s", CLOSING_DATE_ID, "day"),
                    "The closing date must be later than the opening date");
        }

        if (result.getFieldErrors().isEmpty())
            result.setValid(Boolean.TRUE);
        return result;
    }

    private static IntStream parseTimeStringToInt(String timeString) {
        if (timeString.contains(":")) {
            String[] parts = timeString.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return IntStream.of(hours, minutes);
        }
        return IntStream.of(Integer.parseInt(timeString));
    }

    private SimpleEntry<String, String> getMandatoryFieldViolationMessage(
            AdvertDefinitionQuestionResponseType responseType, String questionId,
            AdvertDefinitionQuestionValidationMessages validationMessages) {

        String mandatoryErrorMessage = getCustomErrorMessage(GrantAdvertValidationType.MANDATORY, validationMessages);

        if (mandatoryErrorMessage != null) {
            return new SimpleEntry<>(questionId, mandatoryErrorMessage);
        }
        else {
            return switch (responseType) {
                case LIST -> new SimpleEntry<>(questionId, "Select an option");
                case DATE -> new SimpleEntry<>(questionId, "Enter a value for day, month, year");
                default -> new SimpleEntry<>(questionId, "You must enter an answer");
            };
        }

    }

    private AbstractMap.SimpleEntry<String, String> validateDate(final String[] dateComponents,
            final boolean isMandatory, String questionId, GrantAdvertPageResponseValidationDto submittedPage) {
        // if it's not mandatory and date part is empty, skip rest of the validation
        if (!isMandatory && (dateComponents.length >= 3
                && arrayAnswerIsEmpty(Arrays.copyOfRange(dateComponents, 0, 2), false))) {
            return null;
        }

        // retrieve question and validation messages from static definition
        AdvertDefinitionQuestion questionDefinition = advertDefinition.getSectionById(submittedPage.getSectionId())
                .getPageById(submittedPage.getPage().getId()).getQuestionById(questionId);
        AdvertDefinitionQuestionValidationMessages validationMessages = questionDefinition.getValidationMessages();

        ArrayList<String> nullList = new ArrayList<>();
        ArrayList<String> invalidList = new ArrayList<>();

        // checking for null values
        if (dateComponents.length < 1 || Strings.isEmpty(dateComponents[0])) {
            nullList.add("day");
        }

        if (dateComponents.length < 2 || Strings.isEmpty(dateComponents[1])) {
            nullList.add("month");
        }

        if (dateComponents.length < 3 || Strings.isEmpty(dateComponents[2])) {
            nullList.add("year");
        }

        if (!nullList.isEmpty()) {
            String errorId = String.format("%s-%s", questionId, nullList.get(0));
            String formattedError;
            if (nullList.size() == 3) {
                formattedError = getCustomErrorMessage(GrantAdvertValidationType.MANDATORY, validationMessages);
            }
            else {
                String errorTemplate = getCustomErrorMessage(GrantAdvertValidationType.MISSING_FIELD,
                        validationMessages);
                formattedError = getDateErrorMessage(nullList, errorTemplate, " and a %s");
            }

            return new AbstractMap.SimpleEntry<>(errorId, formattedError);
        }

        // checking for invalid values
        Integer month = null;
        try {
            month = Integer.parseInt(dateComponents[1]);
            if (month < 1 || month > 12) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e) {
            invalidList.add("month");
        }

        Integer year = null;
        try {
            year = Integer.parseInt(dateComponents[2]);
            if (String.valueOf(year).length() != 4) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e) {
            invalidList.add("year");
        }

        int day;
        try {
            day = Integer.parseInt(dateComponents[0]);
            // throw an exception if day is over 31,
            // but if month or year on invalid, don't throw exception
            // lastly if day is less than 31 and month and year aren't invalid, check if
            // the day is valid for that month (i.e. can June have 31)
            if (day > 31 || day < 1 || ((!invalidList.contains("month") && !invalidList.contains("year"))
                    && !dayIsValidForMonth(day, month, year))) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e) {
            invalidList.add("day");
        }

        if (!invalidList.isEmpty()) {
            String errorId = String.format("%s-%s", questionId, invalidList.get(0));
            String errorTemplate = getCustomErrorMessage(GrantAdvertValidationType.INVALID, validationMessages);
            String formattedError = getDateErrorMessage(invalidList, errorTemplate, null);

            return new AbstractMap.SimpleEntry<>(errorId, formattedError);
        }

        return null;
    }

    /**
     * Given a list of invalid values, will generate a concatenated error message using
     * the provided template. ie. ["day","month","year] & "Enter a value for %s" will
     * return "Enter a value for day, month and year"
     *
     * You can provide an optional string to customise the final concatenation. ie.
     * ["day","month"] & "Date must include a %s" & " and a %s" would return "Date must
     * include a day and a month"
     * @return formatted error message
     */
    private String getDateErrorMessage(ArrayList<String> errorList, String errorTemplate, String terminalConcatString) {
        String terminalConcat = terminalConcatString != null ? terminalConcatString : " and %s";

        String formattedError;
        int last = errorList.size() - 1;
        if (last != 0) {
            String joinedNulls = StringUtils.join(errorList.subList(0, last), ", ");
            formattedError = String.format(String.format("%s%s", errorTemplate, terminalConcat), joinedNulls,
                    errorList.get(last));
        }
        else {
            formattedError = String.format(errorTemplate, errorList.get(0));
        }
        return formattedError;
    }

    /**
     * Checks if day is valid for that particular month and year (takes into account leap
     * years) i.e. does June have 31 days
     * @return boolean
     */
    private boolean dayIsValidForMonth(final int day, final int month, final int year) {
        final Year parsedYear = Year.of(year);
        final Month parsedMonth = Month.of(month);
        final boolean isLeap = parsedYear.isLeap();

        return day <= parsedMonth.length(isLeap);
    }

    private boolean arrayAnswerIsEmpty(final String[] answerParts, boolean isRichText) {
        if (isRichText && (answerParts[0] == null || answerParts[0].isEmpty())) {
            return Boolean.TRUE;
        }
        else if (answerParts == null || answerParts.length == 0) {
            return Boolean.TRUE;
        }
        else {
            return Stream.of(answerParts).allMatch(str -> str == null || str.isBlank());
        }
    }

    private String getCustomErrorMessage(GrantAdvertValidationType validationType,
            AdvertDefinitionQuestionValidationMessages customMessages) {

        return customMessages != null ? switch (validationType) {
            case MANDATORY -> customMessages.getMandatory();
            case MIN_LENGTH -> customMessages.getMinLength();
            case MAX_LENGTH -> customMessages.getMaxLength();
            case URL -> customMessages.getUrl();
            case LESS_THAN -> customMessages.getLessThan();
            case GREATER_THAN -> customMessages.getGreaterThan();
            case MISSING_FIELD -> customMessages.getMissingField();
            case INVALID -> customMessages.getInvalid();
            default -> null;
        } : null;
    }

}
