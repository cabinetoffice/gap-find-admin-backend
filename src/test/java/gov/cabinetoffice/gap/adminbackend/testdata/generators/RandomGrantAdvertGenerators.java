package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPageResponseDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.enums.AdvertDefinitionQuestionResponseType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertPageResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSectionResponseStatus;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import gov.cabinetoffice.gap.adminbackend.models.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class RandomGrantAdvertGenerators {

    // Grant advert generators
    public static GrantAdvert.GrantAdvertBuilder randomGrantAdvertEntity() {
        return GrantAdvert.builder().id(UUID.randomUUID()).scheme(RandomSchemeGenerator.randomSchemeEntity().build())
                .revision(1).created(Instant.now()).createdBy(new GrantAdmin(1, null, null, new ArrayList<>())).lastUpdated(Instant.now())
                .lastUpdatedBy(new GrantAdmin(1, null, GapUser.builder().userSub("sub").build(), new ArrayList<>())).status(GrantAdvertStatus.DRAFT)
                .contentfulEntryId("entry-id").contentfulSlug("contentful-slug").grantAdvertName("Grant Advert Name")
                .response(randomAdvertResponse().build());
    }

    public static GrantAdvertResponse.GrantAdvertResponseBuilder randomAdvertResponse() {
        return GrantAdvertResponse.builder().sections(Collections.singletonList(randomAdvertSectionResponse().build()));
    }

    public static GrantAdvertSectionResponse.GrantAdvertSectionResponseBuilder randomAdvertSectionResponse() {
        return GrantAdvertSectionResponse.builder().id("section-id")
                .status(GrantAdvertSectionResponseStatus.IN_PROGRESS)
                .pages(Collections.singletonList(randomAdvertPageResponse().build()));
    }

    public static GrantAdvertPageResponse.GrantAdvertPageResponseBuilder randomAdvertPageResponse() {
        return GrantAdvertPageResponse.builder().id("page-id").status(GrantAdvertPageResponseStatus.NOT_STARTED)
                .questions(Collections.singletonList(randomAdvertQuestionResponse().build()));
    }

    public static GrantAdvertQuestionResponse.GrantAdvertQuestionResponseBuilder randomAdvertQuestionResponse() {
        return GrantAdvertQuestionResponse.builder().id("question-id").seen(true).response("This is a response")
                .multiResponse(null);
    }

    public static AdvertBuilderQuestionView.AdvertBuilderQuestionViewBuilder randomAdvertBuilderQuestionView() {
        return AdvertBuilderQuestionView.builder().responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT)
                .questionId("question-id").questionTitle("Question Title").hintText("this is a hint")
                .options(Collections.emptyList()).response(randomAdvertQuestionResponse().build());
    }

    public static AdvertDefinitionQuestionValidation.AdvertDefinitionQuestionValidationBuilder randomQuestionValidationGenerator() {
        return AdvertDefinitionQuestionValidation.builder().mandatory(true);
    }

    public static GetGrantAdvertPageResponseDTO.GetGrantAdvertPageResponseDTOBuilder randomAdvertBuilderPageResponse() {
        return GetGrantAdvertPageResponseDTO.builder().sectionName("Mock section name").pageTitle("Mock Page Title")
                .questions(Collections.singletonList(randomAdvertBuilderQuestionView().build()))
                .previousPageId("previous-page-id").nextPageId("next-page-id");
    }

    // Advert Definition Generators
    public static AdvertDefinition.AdvertDefinitionBuilder randomAdvertDefinition() {
        return AdvertDefinition.builder().sections(Collections.singletonList(randomAdvertDefinitionSection().build()));
    }

    public static AdvertDefinitionSection.AdvertDefinitionSectionBuilder randomAdvertDefinitionSection() {
        return AdvertDefinitionSection.builder().id("section-id").title("Question Title")
                .pages(Collections.singletonList(randomAdvertDefinitionPage().build()));
    }

    public static AdvertDefinitionPage.AdvertDefinitionPageBuilder randomAdvertDefinitionPage() {
        return AdvertDefinitionPage.builder().id("page-id").title("Page Title")
                .questions(Collections.singletonList(randomAdvertDefinitionQuestion().build()));
    }

    public static AdvertDefinitionQuestion.AdvertDefinitionQuestionBuilder randomAdvertDefinitionQuestion() {
        return AdvertDefinitionQuestion.builder().id("question-id").title("Question Title")
                .displayText("Text to display").hintText("This is some hint text")
                .validation(new AdvertDefinitionQuestionValidation(false, 0, 255, false, null, null, null))
                .responseType(AdvertDefinitionQuestionResponseType.SHORT_TEXT);
    }

}
