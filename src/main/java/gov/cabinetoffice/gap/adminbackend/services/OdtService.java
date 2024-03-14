package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormQuestionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import gov.cabinetoffice.gap.adminbackend.enums.ResponseTypeEnum;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.odfdom.dom.element.style.*;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.OdfStylePropertySet;
import org.odftoolkit.odfdom.dom.style.props.OdfPageLayoutProperties;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStylePageLayout;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextHeading;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OdtService {

    private static final Logger logger = LoggerFactory.getLogger(OdtService.class);
    private static final String ELIGIBILITY_SECTION_ID = "ELIGIBILITY";
    private static final String Heading_20_1 = "Heading_20_1";
    private static final String Heading_20_2 = "Heading_20_2";
    private static final String Heading_20_3 = "Heading_20_3";
    private static final String Text_20_1 = "Text_20_1";
    private static final String Text_20_2 = "Text_20_2";
    private static final String Text_20_3 = "Text_20_3";
    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm").withZone(ZoneOffset.UTC);

    public OdfTextDocument generateSingleOdt(final SchemeEntity grantScheme, final ApplicationFormEntity applicationForm) {
        try {
            OdfStyleProcessor styleProcessor = new OdfStyleProcessor();
            OdfTextDocument odt = OdfTextDocument.newTextDocument();
            OdfOfficeStyles stylesOfficeStyles = odt.getOrCreateDocumentStyles();
            OdfContentDom contentDom = odt.getContentDom();
            OfficeTextElement documentText = odt.getContentRoot();

            setOfficeStyles(odt, styleProcessor, stylesOfficeStyles);
            OdfTextParagraph sectionBreak = new OdfTextParagraph(contentDom);

            populateSchemeHeadingSection(grantScheme, documentText, contentDom, odt);

            documentText.appendChild(sectionBreak);

            populateApplicationHeadingSection(applicationForm, documentText, contentDom);

            odt.getContentRoot().setTextUseSoftPageBreaksAttribute(true);

            populateEligibilitySection(applicationForm, documentText, contentDom);

            populateRequiredChecksSection(documentText, contentDom, odt);

            AtomicInteger count = new AtomicInteger(3); //2 sections already added

            if (applicationForm.getDefinition().getSections().stream().anyMatch(section -> section.getSectionId().matches(UUID_REGEX))) {
                addPageBreak(contentDom, odt);
                documentText.appendChild(new OdfTextParagraph(contentDom)
                        .addStyledContentWhitespace(Heading_20_2, "Custom sections"));
            }
            applicationForm.getDefinition().getSections().forEach(section -> {
                if (section.getSectionId().matches(UUID_REGEX)) {
                    populateQuestionResponseTable(count, section, documentText, contentDom, odt);
                }
            });
            logger.info("ODT file generated successfully");
            return odt;
        } catch (Exception e) {
            logger.error("Could not generate ODT for given application", e);
            throw new RuntimeException(e);
        }
    }

    private void addPageBreak(OdfContentDom contentDocument, OdfTextDocument doc) throws Exception {
        final OdfOfficeAutomaticStyles styles = contentDocument.getAutomaticStyles();
        final OdfStyle style = styles.newStyle(OdfStyleFamily.Paragraph);
        style.newStyleParagraphPropertiesElement().setFoBreakBeforeAttribute("page");
        final TextPElement page = doc.getContentRoot().newTextPElement();
        page.setStyleName(style.getStyleNameAttribute());
    }

    private static void populateSchemeHeadingSection(final SchemeEntity grantScheme,
                                                     final OfficeTextElement documentText,
                                                     final OdfContentDom contentDom,
                                                     final OdfTextDocument odt) {

        OdfTextHeading h1 = new OdfTextHeading(contentDom);
        OdfTextHeading h2 = new OdfTextHeading(contentDom);

        h1.addStyledContentWhitespace(Heading_20_1, "Scheme details");
        h2.addStyledContentWhitespace(Heading_20_2, grantScheme.getName());

        OdfTable table;
        table = OdfTable.newTable(odt, 2, 1);
        table.getRowByIndex(0).getCellByIndex(0).setStringValue("GGIS ID");
        table.getRowByIndex(0).getCellByIndex(1).setStringValue(grantScheme.getGgisIdentifier());
        table.getRowByIndex(1).getCellByIndex(0).setStringValue("Contact email");
        table.getRowByIndex(1).getCellByIndex(1).setStringValue(grantScheme.getEmail());

        documentText.appendChild(h1);
        documentText.appendChild(h2);
        documentText.appendChild(table.getOdfElement());
    }

    private static void populateApplicationHeadingSection(final ApplicationFormEntity applicationForm,
                                                          final OfficeTextElement documentText,
                                                          final OdfContentDom contentDom) {

        OdfTextHeading h1 = new OdfTextHeading(contentDom);
        OdfTextHeading h2 = new OdfTextHeading(contentDom);
        OdfTextParagraph p = new OdfTextParagraph(contentDom);

        h1.addStyledContentWhitespace(Heading_20_1, "\nApplication details");
        h2.addStyledContentWhitespace(Heading_20_2, applicationForm.getApplicationName());
        p.addStyledContentWhitespace(Text_20_3, (
                Objects.equals(ApplicationStatusEnum.PUBLISHED, applicationForm.getApplicationStatus())
                        ? ("Published on " + formatter.format(applicationForm.getLastPublished()))
                        : "Not published"
        ) + "\n\n");
        documentText.appendChild(h1);
        documentText.appendChild(h2);
        documentText.appendChild(p);
    }

    private static void populateRequiredChecksSection(final OfficeTextElement documentText,
                                                      final OdfContentDom contentDom,
                                                      final OdfTextDocument odt) {
        OdfTextHeading requiredCheckHeading = new OdfTextHeading(contentDom);
        OdfTextHeading requiredCheckSubHeading = new OdfTextHeading(contentDom);
        OdfTextParagraph locationQuestion = new OdfTextParagraph(contentDom);
        final String orgNameHeading = "Organisation details";

        requiredCheckHeading.addStyledContentWhitespace(Heading_20_2, "Due diligence information");
        requiredCheckSubHeading.addStyledContentWhitespace(Heading_20_3, orgNameHeading);

        documentText.appendChild(requiredCheckHeading);
        documentText.appendChild(requiredCheckSubHeading);
        documentText.appendChild(generateEssentialTable(odt));
        documentText.appendChild(new OdfTextParagraph(contentDom).addContentWhitespace(""));
        locationQuestion.addStyledContent(Heading_20_3, "Funding");
        OdfTable table = OdfTable.newTable(odt, 2, 1);

        table.getRowByIndex(0).getCellByIndex(0).setStringValue("Amount applied for");
        table.getRowByIndex(1).getCellByIndex(0).setStringValue("Where funding will be spent");

        documentText.appendChild(locationQuestion);
        documentText.appendChild(table.getOdfElement());
    }

    private static void populateEligibilitySection(final ApplicationFormEntity applicationForm,
                                                   final OfficeTextElement documentText,
                                                   final OdfContentDom contentDom) {
        final ApplicationFormSectionDTO eligibilitySection = applicationForm.getDefinition().getSectionById(ELIGIBILITY_SECTION_ID);
        OdfTextHeading eligibilityHeading = new OdfTextHeading(contentDom);
        OdfTextParagraph eligibilityStatement = new OdfTextParagraph(contentDom);
        OdfTextParagraph eligibilityResponse = new OdfTextParagraph(contentDom);

        eligibilityHeading.addStyledContent(Heading_20_2, "Eligibility");
        documentText.appendChild(eligibilityHeading);

        OdfTextHeading eligibilitySubHeading = new OdfTextHeading(contentDom);
        eligibilitySubHeading.addStyledContent(Heading_20_3, "Eligibility Statement");
        documentText.appendChild(eligibilitySubHeading);

        eligibilityResponse.addStyledContentWhitespace(Text_20_3, eligibilitySection
                .getQuestionById(ELIGIBILITY_SECTION_ID).getDisplayText() + "\n");

        documentText.appendChild(eligibilityResponse);

        documentText.appendChild(new OdfTextHeading(contentDom).addContentWhitespace(""));
        documentText.appendChild(eligibilityStatement);
    }

    private static void populateQuestionResponseTable(AtomicInteger count,
                                                      ApplicationFormSectionDTO section,
                                                      OfficeTextElement documentText,
                                                      OdfContentDom contentDom, OdfTextDocument odt) {
        OdfTextHeading sectionHeading = new OdfTextHeading(contentDom);
        sectionHeading.addStyledContentWhitespace(Heading_20_3, section.getSectionTitle());
        documentText.appendChild(sectionHeading);

        if (section.getQuestions().size() > 0) {
            AtomicInteger questionIndex = new AtomicInteger(1);
            OdfTable table = OdfTable.newTable(odt, section.getQuestions().size(), 3);
            long columnWidth = table.getWidth() / 5;
            table.getColumnByIndex(0).setWidth(columnWidth);
            table.getColumnByIndex(1).setWidth(columnWidth);
            table.getColumnByIndex(2).setWidth(columnWidth);
            table.getColumnByIndex(3).setWidth(columnWidth);
            table.getColumnByIndex(4).setWidth(columnWidth);
            table.getRowByIndex(0).getCellByIndex(0).setStringValue("Question");
            table.getRowByIndex(0).getCellByIndex(1).setStringValue("Hint text");
            table.getRowByIndex(0).getCellByIndex(2).setStringValue("Question type");
            table.getRowByIndex(0).getCellByIndex(3).setStringValue("Options / Max words");
            table.getRowByIndex(0).getCellByIndex(4).setStringValue("Optional");
            section.getQuestions().forEach(question -> {
                populateDocumentFromQuestionResponse(question, documentText, questionIndex,
                        table);
                questionIndex.incrementAndGet();
            });
        } else {
            OdfTable table = OdfTable.newTable(odt, 1, 3);
            long columnWidth = table.getWidth() / 5;
            table.getColumnByIndex(0).setWidth(columnWidth);
            table.getColumnByIndex(1).setWidth(columnWidth);
            table.getColumnByIndex(2).setWidth(columnWidth);
            table.getColumnByIndex(3).setWidth(columnWidth);
            table.getColumnByIndex(4).setWidth(columnWidth);
            table.getRowByIndex(0).getCellByIndex(0).setStringValue("Question");
            table.getRowByIndex(0).getCellByIndex(1).setStringValue("Hint text");
            table.getRowByIndex(0).getCellByIndex(2).setStringValue("Question type");
            table.getRowByIndex(0).getCellByIndex(3).setStringValue("Options / Max words");
            table.getRowByIndex(0).getCellByIndex(4).setStringValue("Optional");
            documentText.appendChild(table.getOdfElement());
        }
        documentText.appendChild(new OdfTextParagraph(contentDom).addContentWhitespace(""));
        count.getAndIncrement();
    }

    private static void populateDocumentFromQuestionResponse(ApplicationFormQuestionDTO question,
                                                             OfficeTextElement documentText,
                                                             AtomicInteger questionIndex,
                                                             OdfTable table) {
        EnumMap<ResponseTypeEnum, String> responseTypeMap = new EnumMap<>(ResponseTypeEnum.class);
        responseTypeMap.put(ResponseTypeEnum.YesNo, "Yes/No");
        responseTypeMap.put(ResponseTypeEnum.SingleSelection, "Single selection");
        responseTypeMap.put(ResponseTypeEnum.Dropdown, "Multiple choice");
        responseTypeMap.put(ResponseTypeEnum.MultipleSelection, "Multiple select");
        responseTypeMap.put(ResponseTypeEnum.ShortAnswer, "Short answer");
        responseTypeMap.put(ResponseTypeEnum.LongAnswer, "Long answer");
        responseTypeMap.put(ResponseTypeEnum.AddressInput, "Address input");
        responseTypeMap.put(ResponseTypeEnum.Numeric, "Numeric");
        responseTypeMap.put(ResponseTypeEnum.Date, "Date");
        responseTypeMap.put(ResponseTypeEnum.SingleFileUpload, "Document upload");

        table.getRowByIndex(questionIndex.get()).getCellByIndex(0).setStringValue(question.getFieldTitle());
        table.getRowByIndex(questionIndex.get()).getCellByIndex(1).setStringValue(question.getHintText());
        table.getRowByIndex(questionIndex.get()).getCellByIndex(2).setStringValue(responseTypeMap.get(question.getResponseType()));
        // Options / Max Words
        boolean shouldDisplayOptions = question.getResponseType().equals(ResponseTypeEnum.MultipleSelection)
                || question.getResponseType().equals(ResponseTypeEnum.SingleSelection)
                || question.getResponseType().equals(ResponseTypeEnum.Dropdown);
        boolean shouldDisplayMaxWords = question.getResponseType().equals(ResponseTypeEnum.LongAnswer)
                || question.getResponseType().equals(ResponseTypeEnum.ShortAnswer);
        if (shouldDisplayOptions) {
            table.getRowByIndex(questionIndex.get()).getCellByIndex(3).setStringValue(String.join(",\n", question.getOptions()));
        } else if (shouldDisplayMaxWords) {
            table.getRowByIndex(questionIndex.get()).getCellByIndex(3).setStringValue(question.getValidation().get("maxWords").toString());
        } else {
            table.getRowByIndex(questionIndex.get()).getCellByIndex(3).setStringValue("");
        }
        //Optional
        table.getRowByIndex(questionIndex.get()).getCellByIndex(4).setStringValue(
                Objects.equals(question.getValidation().get("mandatory"), true)
                        ? "No"
                        : "Yes"
        );
        documentText.appendChild(table.getOdfElement());
    }

    private static TableTableElement generateEssentialTable(OdfTextDocument doc) {
        OdfTable odfTable = OdfTable.newTable(doc, 9, 1);

        odfTable.getRowByIndex(0).getCellByIndex(0).setStringValue("Organisation name");
        odfTable.getRowByIndex(1).getCellByIndex(0).setStringValue("Organisation type");

        odfTable.getRowByIndex(2).getCellByIndex(0).setStringValue("Address line 1");
        odfTable.getRowByIndex(3).getCellByIndex(0).setStringValue("Address line 2");
        odfTable.getRowByIndex(4).getCellByIndex(0).setStringValue("Address city");
        odfTable.getRowByIndex(5).getCellByIndex(0).setStringValue("Address county");
        odfTable.getRowByIndex(6).getCellByIndex(0).setStringValue("Address postcode");

        odfTable.getRowByIndex(7).getCellByIndex(0).setStringValue("Charities Commission number (if the organisation has one)");
        odfTable.getRowByIndex(8).getCellByIndex(0).setStringValue("Companies House number (if the organisation has one)");

        return odfTable.getOdfElement();
    }

    public ByteArrayResource odtToResource(OdfTextDocument odtDoc) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        odtDoc.save(outputStream);
        byte[] zipBytes = outputStream.toByteArray();
        return new ByteArrayResource(zipBytes);
    }

    private static void setOfficeStyles(OdfTextDocument outputDocument, OdfStyleProcessor styleProcessor, OdfOfficeStyles stylesOfficeStyles) {
        // Set landscape layout
        StyleMasterPageElement defaultPage = outputDocument.getOfficeMasterStyles().getMasterPage("Standard");
        String pageLayoutName = defaultPage.getStylePageLayoutNameAttribute();
        OdfStylePageLayout pageLayoutStyle = defaultPage.getAutomaticStyles().getPageLayout(pageLayoutName);
        pageLayoutStyle.setProperty(OdfPageLayoutProperties.PrintOrientation, "Portrait");

        styleProcessor.setStyle(stylesOfficeStyles.getDefaultStyle(OdfStyleFamily.Paragraph))
                .margins("0cm", "0cm", "0cm", "0cm")
                .fontFamily("Arial")
                .fontSize("11pt")
                .textAlign("normal");


        // Title 1
        styleProcessor.setStyle(stylesOfficeStyles.getStyle(Heading_20_1, OdfStyleFamily.Paragraph))
                .margins("0cm", "0cm", "0cm", "0cm").
                color("#000000")
                .fontWeight("normal")
                .fontSize("26pt");

        // Title 2
        styleProcessor.setStyle(stylesOfficeStyles.getStyle(Heading_20_2, OdfStyleFamily.Paragraph))
                .fontStyle("normal")
                .margins("0cm", "0cm", "0.2cm", "0cm")
                .fontWeight("normal")
                .fontSize("20pt")
                .color("#000000");

        // Title 3
        styleProcessor.setStyle(stylesOfficeStyles.getStyle(Heading_20_3, OdfStyleFamily.Paragraph))
                .margins("0cm", "0cm", "0.2cm", "0cm")
                .fontWeight("normal")
                .fontSize("16pt");

        //test
        styleProcessor.setStyle(stylesOfficeStyles.newStyle(Text_20_1, OdfStyleFamily.Text))
                .margins("0cm", "0cm", "1cm", "0cm")
                .fontFamily("Arial")
                .fontSize("15pt")
                .color("#000000");

        styleProcessor.setStyle(stylesOfficeStyles.newStyle(Text_20_2, OdfStyleFamily.Text))
                .margins("0cm", "0cm", "0cm", "0cm")
                .fontFamily("Arial")
                .fontSize("11pt")
                .color("#000000");

        styleProcessor.setStyle(stylesOfficeStyles.newStyle(Text_20_3, OdfStyleFamily.Text))
                .margins("0cm", "0cm", "0cm", "0cm")
                .fontFamily("Arial")
                .fontSize("11pt")
                .color("#000000")
                .fontStyle("italic");
    }

    private static class OdfStyleProcessor {

        private OdfStylePropertySet style;

        public OdfStyleProcessor() {

        }

        public OdfStyleProcessor setStyle(OdfStylePropertySet style) {
            this.style = style;
            return this;
        }

        public OdfStyleProcessor fontFamily(String value) {
            this.style.setProperty(StyleTextPropertiesElement.FontFamily, value);
            this.style.setProperty(StyleTextPropertiesElement.FontName, value);
            return this;
        }

        public OdfStyleProcessor fontWeight(String value) {
            this.style.setProperty(StyleTextPropertiesElement.FontWeight, value);
            this.style.setProperty(StyleTextPropertiesElement.FontWeightAsian, value);
            this.style.setProperty(StyleTextPropertiesElement.FontWeightComplex, value);
            return this;
        }

        public OdfStyleProcessor fontStyle(String value) {
            this.style.setProperty(StyleTextPropertiesElement.FontStyle, value);
            this.style.setProperty(StyleTextPropertiesElement.FontStyleAsian, value);
            this.style.setProperty(StyleTextPropertiesElement.FontStyleComplex, value);
            return this;
        }

        public OdfStyleProcessor fontSize(String value) {
            this.style.setProperty(StyleTextPropertiesElement.FontSize, value);
            this.style.setProperty(StyleTextPropertiesElement.FontSizeAsian, value);
            this.style.setProperty(StyleTextPropertiesElement.FontSizeComplex, value);
            return this;
        }

        public OdfStyleProcessor margins(String top, String right, String bottom, String left) {
            this.style.setProperty(StyleParagraphPropertiesElement.MarginTop, top);
            this.style.setProperty(StyleParagraphPropertiesElement.MarginRight, right);
            this.style.setProperty(StyleParagraphPropertiesElement.MarginBottom, bottom);
            this.style.setProperty(StyleParagraphPropertiesElement.MarginLeft, left);
            return this;
        }

        public OdfStyleProcessor color(String value) {
            this.style.setProperty(StyleTextPropertiesElement.Color, value);
            return this;
        }

        public void textAlign(String value) {
            this.style.setProperty(StyleParagraphPropertiesElement.TextAlign, value);
        }

    }
}
