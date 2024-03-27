package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationDefinitionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormQuestionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSchemeGenerator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class OdtServiceTest {
    @InjectMocks
    OdtService odtService;

    @Test
    void generateSingleOdt() throws Exception {
        SchemeEntity mockScheme = RandomSchemeGenerator.randomSchemeEntity().version(2).build();

        List<ApplicationFormSectionDTO> sections = new ArrayList<>(
                List.of(
                        ApplicationFormSectionDTO.builder()
                                .sectionId("ELIGIBILITY")
                                .questions(
                                        new ArrayList<>(List.of(
                                                ApplicationFormQuestionDTO.builder()
                                                        .questionId("ELIGIBILITY")
                                                        .displayText("This is the eligibility statement.")
                                                        .build()
                                        ))
                                )
                                .build(),
                        ApplicationFormSectionDTO.builder()
                                .sectionId("REQUIRED_CHECKS")
                                .build(),
                        ApplicationFormSectionDTO.builder()
                                .sectionId("605d59ab-569a-4db9-bc96-58fb8a75ac94")
                                .sectionTitle("Custom Section 1")
                                .questions(
                                        new ArrayList<>(List.of(
                                                ApplicationFormQuestionDTO.builder()
                                                        .questionId("1ff22c1f-0065-4fe6-bc81-3038edd67ed5")
                                                        .fieldTitle("Custom Question 1")
                                                        .hintText("Question 1 hint text")
                                                        .responseType(ResponseTypeEnum.LongAnswer)
                                                        .validation(Map.ofEntries(
                                                                Map.entry("maxWords", 987),
                                                                Map.entry("mandatory", true)
                                                        ))
                                                        .build(),
                                                ApplicationFormQuestionDTO.builder()
                                                        .questionId("9368923f-2264-4b2a-b84b-752d5cdeb574")
                                                        .fieldTitle("Custom Question 2")
                                                        .hintText("Question 2 hint text")
                                                        .responseType(ResponseTypeEnum.MultipleSelection)
                                                        .options(List.of("Option 1", "Option 2", "Option 3"))
                                                        .validation(Map.ofEntries(
                                                                Map.entry("mandatory", false)
                                                        ))
                                                        .build()
                                        ))
                                )
                                .build(),
                        ApplicationFormSectionDTO.builder()
                                .sectionId("605d59ab-569a-4db9-bc96-58fb8a75ac94")
                                .sectionTitle("Custom Section 2")
                                .questions(List.of())
                                .build()
                )
        );
        ApplicationDefinitionDTO definition = ApplicationDefinitionDTO.builder()
                .sections(sections)
                .build();
        ApplicationFormEntity mockApplicationForm = ApplicationFormEntity.createFromTemplate(
                mockScheme.getId(), "Application Name",
                1, definition, mockScheme.getVersion());

        OdfDocument generatedDoc = odtService.generateSingleOdt(mockScheme, mockApplicationForm);
        final String generatedContent = docToString(generatedDoc.getContentDom());

        assertThat(generatedContent).contains(
                "Scheme details",
                "Sample Scheme",
                "GGIS ID",
                "GGIS12345",
                "Contact email",
                "contact@address.com"
        );
        assertThat(generatedContent).contains(
                "Application details",
                "Application Name",
                "Not published"
        );
        assertThat(generatedContent).contains(
                "Eligibility",
                "This is the eligibility statement."
        );
        assertThat(generatedContent).contains(
                "Due diligence information",
                "Organisation details",
                "Organisation name",
                "Organisation type",
                "Address line 1",
                "Address line 2",
                "Address city",
                "Address county",
                "Address postcode",
                "Charities Commission number (if the organisation has one)",
                "Companies House number (if the organisation has one)",
                "Funding",
                "Amount applied for",
                "Where funding will be spent"
        );
        assertThat(generatedContent).contains(
                "Custom Section 1",
                "Question",
                "Hint text",
                "Question type",
                "Options / Max words",
                "Optional",
                "Custom Question 1",
                "Question 1 hint text",
                "Long answer",
                "987",
                "No",
                "Custom Question 2",
                "Question 2 hint text",
                "Multiple select",
                "Option 1,",
                "Option 2,",
                "Option 3",
                "Yes",
                "Custom Section 2"
        );
    }

    private String docToString(Document document) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.getBuffer().toString();
    }
}
