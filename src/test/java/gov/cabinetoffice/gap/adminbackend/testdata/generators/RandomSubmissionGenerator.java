package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionExportsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionQuestion;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionSection;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;

import java.util.List;
import java.util.UUID;

public class RandomSubmissionGenerator {

    public static Submission.SubmissionBuilder randomSubmission() {

        return Submission.builder().id(UUID.randomUUID());

    }

    public static SubmissionDefinition.SubmissionDefinitionBuilder randomSubmissionDefinition() {

        return SubmissionDefinition.builder();

    }

    // behold, the most elaborate object deep cloning
    public static SubmissionDefinition.SubmissionDefinitionBuilder randomSubmissionDefinition(
            SubmissionDefinition submissionDefinition) {

        List<SubmissionSection> submissionSectionList = submissionDefinition.getSections().stream()
                .map(section -> randomSubmissionSection(section).build()).toList();

        return SubmissionDefinition.builder().sections(submissionSectionList);

    }

    public static SubmissionSection.SubmissionSectionBuilder randomSubmissionSection() {

        return SubmissionSection.builder();

    }

    public static SubmissionSection.SubmissionSectionBuilder randomSubmissionSection(
            SubmissionSection submissionSection) {

        List<SubmissionQuestion> submissionQuestionList = submissionSection.getQuestions().stream()
                .map(question -> randomSubmissionQuestion(question).build()).toList();

        return SubmissionSection.builder().sectionId(submissionSection.getSectionId())
                .sectionStatus(submissionSection.getSectionStatus()).sectionTitle(submissionSection.getSectionTitle())
                .questions(submissionQuestionList);

    }

    public static SubmissionQuestion.SubmissionQuestionBuilder randomSubmissionQuestion() {

        return SubmissionQuestion.builder();

    }

    public static SubmissionQuestion.SubmissionQuestionBuilder randomSubmissionQuestion(SubmissionQuestion question) {

        return SubmissionQuestion.builder().questionId(question.getQuestionId())
                .profileField(question.getProfileField()).fieldTitle(question.getFieldTitle())
                .hintText(question.getHintText()).questionSuffix(question.getQuestionSuffix())
                .adminSummary(question.getAdminSummary()).validation(question.getValidation())
                .displayText(question.getDisplayText()).fieldPrefix(question.getFieldPrefix())
                .responseType(question.getResponseType()).options(question.getOptions())
                .response(question.getResponse()).multiResponse(question.getMultiResponse());
    }

    public static SubmissionExportsDTO.SubmissionExportsDTOBuilder randomSubmissionDTOBuilder() {
        return SubmissionExportsDTO.builder().label("test_file.zip").s3key("/test/path_to/s3_location/");
    }

}
