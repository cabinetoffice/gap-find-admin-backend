package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SpotlightExportException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantMandatoryQuestionRepository;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import gov.cabinetoffice.gap.adminbackend.utils.XlsxGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
@Slf4j
public class GrantMandatoryQuestionService {

    static final List<String> SPOTLIGHT_HEADERS = Arrays.asList("Application number (required)",
            "Organisation name (required)", "Address street (optional)", "Address town (optional)",
            "Address county (optional)", "Address postcode (required)", "Application amount (required)",
            "Charity Commission registration number (required - if applicable)",
            "Companies House registration number (required - if applicable)",
            "Similarities to other applications (optional)");

    private final GrantMandatoryQuestionRepository grantMandatoryQuestionRepository;

    private final SchemeService schemeService;

    public List<GrantMandatoryQuestions> getGrantMandatoryQuestionBySchemeAndCompletedStatus(Integer schemeId) {
        return ofNullable(grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(schemeId))
                .orElseThrow(() -> new NotFoundException(
                        String.format("No completed mandatory questions with ID %s was found", schemeId)));
    }

    public ByteArrayOutputStream exportSpotlightChecks(Integer schemeId) {
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();

        try {
            schemeService.getSchemeBySchemeId(schemeId);
        }
        catch (EntityNotFoundException | AccessDeniedException ex) {
            throw new AccessDeniedException("Admin " + adminSession.getGrantAdminId()
                    + " is unable to access mandatory questions with scheme id " + schemeId);
        }

        final List<GrantMandatoryQuestions> grantMandatoryQuestions = getGrantMandatoryQuestionBySchemeAndCompletedStatus(
                schemeId);
        log.info("Found {} mandatory questions in COMPLETED state for scheme ID {}", grantMandatoryQuestions.size(),
                schemeId);

        List<List<String>> exportData = new ArrayList<>();
        grantMandatoryQuestions.forEach(grantMandatoryQuestion -> {
            try {
                exportData.add(buildSingleSpotlightRow(grantMandatoryQuestion));
            }
            catch (SpotlightExportException e) {
                log.error("Problem extracting data: " + e.getMessage());
            }
        });

        return XlsxGenerator.createResource(SPOTLIGHT_HEADERS, exportData);
    }

    static String mandatoryValue(Integer id, String identifier, String value) {
        if (StringUtils.isBlank(value)) {
            throw new SpotlightExportException(
                    "Missing mandatory " + identifier + " value for schemeId " + id.toString());
        }
        return value;
    }

    static String combineAddressLines(String addressLine1, String addressLine2) {

        if (StringUtils.isEmpty(addressLine1) && StringUtils.isEmpty(addressLine2)) {
            return "";
        }

        if (StringUtils.isEmpty(addressLine2)) {
            return StringUtils.defaultString(addressLine1);
        }

        if (StringUtils.isEmpty(addressLine1)) {
            return StringUtils.defaultString(addressLine2);
        }

        return String.join(", ", StringUtils.defaultString(addressLine1), StringUtils.defaultString(addressLine2));
    }

    /**
     * The ordering of the data added here is strongly tied to SPOTLIGHT_HEADERS. If new
     * headers are added or the ordering is changed in SPOTLIGHT_HEADERS, this will need
     * manually reflected here.
     */
    public List<String> buildSingleSpotlightRow(GrantMandatoryQuestions grantMandatoryQuestions) {
        try {

            final Integer schemeId = grantMandatoryQuestions.getSchemeEntity().getId();

            final String gapId = grantMandatoryQuestions.getGapId();
            final String organisationName = grantMandatoryQuestions.getName();
            final String addressStreet = combineAddressLines(grantMandatoryQuestions.getAddressLine1(),
                    grantMandatoryQuestions.getAddressLine2());
            final String addressTown = grantMandatoryQuestions.getCity();
            final String addressCounty = grantMandatoryQuestions.getCounty();
            final String postcode = grantMandatoryQuestions.getPostcode();
            final String charityNumber = grantMandatoryQuestions.getCharityCommissionNumber();
            final String companyNumber = grantMandatoryQuestions.getCompaniesHouseNumber();
            final String applicationAmount = grantMandatoryQuestions.getFundingAmount() == null ? null
                    : grantMandatoryQuestions.getFundingAmount().toString();

            List<String> row = new ArrayList<>();
            row.add(mandatoryValue(schemeId, "gap id", gapId));
            row.add(mandatoryValue(schemeId, "organisation name", organisationName));
            row.add(addressStreet);
            row.add(addressTown);
            row.add(addressCounty);
            row.add(mandatoryValue(schemeId, "postcode", postcode));
            row.add(mandatoryValue(schemeId, "application amount", applicationAmount));
            row.add(charityNumber);
            row.add(companyNumber);
            row.add(""); // similarities data - should always be blank

            return row;
        }
        catch (NullPointerException e) {
            throw new SpotlightExportException("Unable to find mandatory question data: " + e.getMessage());
        }
    }

    public String generateExportFileName(Integer schemeId) {
        SchemeDTO schemeDTO = schemeService.getSchemeBySchemeId(schemeId);
        String ggisReference = schemeDTO.getGgisReference();
        String schemeName = schemeDTO.getName().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        return dateString + "_" + ggisReference + "_" + schemeName + ".xlsx";
    }

    public boolean doesSchemeHaveCompletedMandatoryQuestions(Integer schemeId) {
        try {
            return !getGrantMandatoryQuestionBySchemeAndCompletedStatus(schemeId).isEmpty();
        }
        catch (NotFoundException ex) {
            return false;
        }

    }

}
