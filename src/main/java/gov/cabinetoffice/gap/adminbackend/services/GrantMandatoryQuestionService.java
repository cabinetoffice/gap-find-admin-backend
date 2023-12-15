package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.constants.DueDiligenceHeaders;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
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
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class GrantMandatoryQuestionService {

    private final GrantMandatoryQuestionRepository grantMandatoryQuestionRepository;

    private final SchemeService schemeService;

    public List<GrantMandatoryQuestions> getGrantMandatoryQuestionBySchemeAndCompletedStatus(Integer schemeId) {
        return grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(schemeId);
    }

    public ByteArrayOutputStream getDueDiligenceData(Integer schemeId, boolean isInternal) {
        List<GrantMandatoryQuestions> mandatoryQuestions = getGrantMandatoryQuestionBySchemeAndCompletedStatus(
                schemeId);
        if (isInternal) {
            mandatoryQuestions = mandatoryQuestions.stream().filter(mq -> mq.getSubmission() != null
                    && mq.getSubmission().getStatus().equals(SubmissionStatus.SUBMITTED)).toList();
        }
        final List<List<String>> exportData = exportSpotlightChecks(schemeId, mandatoryQuestions);
        return XlsxGenerator.createResource(DueDiligenceHeaders.DUE_DILIGENCE_HEADERS, exportData);
    }

    private List<List<String>> exportSpotlightChecks(Integer schemeId,
            List<GrantMandatoryQuestions> grantMandatoryQuestions) {
        final AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();

        try {
            schemeService.getSchemeBySchemeId(schemeId);
        }
        catch (EntityNotFoundException | AccessDeniedException ex) {
            throw new AccessDeniedException("Admin " + adminSession.getGrantAdminId()
                    + " is unable to access mandatory questions with scheme id " + schemeId);
        }

        log.info("Found {} mandatory questions in COMPLETED state for scheme ID {}", grantMandatoryQuestions.size(),
                schemeId);

        return grantMandatoryQuestions.stream().map(this::buildSingleSpotlightRow).toList();
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
            final List<String> row = new ArrayList<>(List.of(
                    mandatoryValue(schemeId, "gap id", grantMandatoryQuestions.getGapId()),
                    mandatoryValue(schemeId, "organisation name", grantMandatoryQuestions.getName()),
                    combineAddressLines(grantMandatoryQuestions.getAddressLine1(),
                            grantMandatoryQuestions.getAddressLine2()),
                    grantMandatoryQuestions.getCity(), grantMandatoryQuestions.getCounty(),
                    mandatoryValue(schemeId, "postcode", grantMandatoryQuestions.getPostcode()),
                    mandatoryValue(schemeId, "application amount",
                            grantMandatoryQuestions.getFundingAmount().toString()),
                    Objects.requireNonNullElse(grantMandatoryQuestions.getCharityCommissionNumber(), ""),
                    Objects.requireNonNullElse(grantMandatoryQuestions.getCompaniesHouseNumber(), ""),
                    mandatoryValue(schemeId, "organisation type", grantMandatoryQuestions.getOrgType().toString())));

            row.add(""); // similarities data - should always be blank
            return row;
        }
        catch (NullPointerException e) {
            throw new SpotlightExportException("Unable to find mandatory question data: " + e.getMessage());
        }
    }

    public String generateExportFileName(Integer schemeId, String orgType) {
        final SchemeDTO schemeDTO = schemeService.getSchemeBySchemeId(schemeId);
        final String ggisReference = schemeDTO.getGgisReference();
        final String schemeName = schemeDTO.getName().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");
        final String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(System.currentTimeMillis());

        return dateString + "_" + ggisReference + "_" + schemeName + (orgType == null ? "" : "_" + orgType) + ".xlsx";
    }

    public boolean hasCompletedMandatoryQuestions(Integer schemeId, boolean isInternal) {
        if (isInternal) {
            return grantMandatoryQuestionRepository.existBySchemeIdAndCompletedStatusAndSubmittedSubmission(schemeId);
        }
        return grantMandatoryQuestionRepository.existsBySchemeEntity_IdAndCompletedStatus(schemeId);
    }

}
