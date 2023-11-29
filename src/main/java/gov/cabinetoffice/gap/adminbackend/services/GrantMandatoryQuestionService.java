package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.constants.DueDiligenceHeaders;
import gov.cabinetoffice.gap.adminbackend.constants.SpotlightHeaders;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType;
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
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class GrantMandatoryQuestionService {

    private final GrantMandatoryQuestionRepository grantMandatoryQuestionRepository;

    private final SchemeService schemeService;

    private final ZipService zipService;

    public List<GrantMandatoryQuestions> getGrantMandatoryQuestionBySchemeAndCompletedStatus(Integer schemeId) {
        return grantMandatoryQuestionRepository.findBySchemeEntity_IdAndCompletedStatus(schemeId);
    }

    public List<GrantMandatoryQuestions> getCharitiesAndCompaniesMandatoryQuestionsBySchemeAndCompletedStatus(
            Integer schemeId) {
        return grantMandatoryQuestionRepository.findCharitiesAndCompaniesBySchemeEntityIdAndCompletedStatus(schemeId);
    }

    public List<GrantMandatoryQuestions> getNonLimitedCompaniesMandatoryQuestionsBySchemeAndCompletedStatus(
            Integer schemeId) {
        return grantMandatoryQuestionRepository.findNonLimitedCompaniesBySchemeEntityIdAndCompletedStatus(schemeId);
    }

    public boolean hasCompletedDataForSpotlight(Integer schemeId) {
        return grantMandatoryQuestionRepository.existsBySchemeEntityIdAndCompleteStatusAndOrgType(schemeId);

    }

    public ByteArrayOutputStream getValidationErrorChecks(List<GrantMandatoryQuestions> mandatoryQuestions, Integer schemeId) {
        final List<GrantMandatoryQuestions> companiesAndCharitiesQuestions = mandatoryQuestions.stream()
                .filter(s -> s.getOrgType().equals(GrantMandatoryQuestionOrgType.CHARITY)
                        || s.getOrgType().equals(GrantMandatoryQuestionOrgType.UNREGISTERED_CHARITY)
                        || s.getOrgType().equals(GrantMandatoryQuestionOrgType.REGISTERED_CHARITY)
                        || s.getOrgType().equals(GrantMandatoryQuestionOrgType.LIMITED_COMPANY))
                .toList();

        final List<GrantMandatoryQuestions> nonLimitedCompanyQuestions = mandatoryQuestions.stream()
                .filter(s -> s.getOrgType().equals(GrantMandatoryQuestionOrgType.NON_LIMITED_COMPANY)).toList();

        return generateZipFile(companiesAndCharitiesQuestions, nonLimitedCompanyQuestions, schemeId);
    }

    public ByteArrayOutputStream getSpotlightChecks(Integer schemeId) {
        final List<GrantMandatoryQuestions> companiesAndCharitiesQuestions = getCharitiesAndCompaniesMandatoryQuestionsBySchemeAndCompletedStatus(
                schemeId);
        final List<GrantMandatoryQuestions> nonLimitedCompanyQuestions = getNonLimitedCompaniesMandatoryQuestionsBySchemeAndCompletedStatus(
                schemeId);
        return generateZipFile(companiesAndCharitiesQuestions, nonLimitedCompanyQuestions, schemeId);

    }

    private ByteArrayOutputStream generateZipFile(List<GrantMandatoryQuestions> companiesAndCharitiesQuestions, List<GrantMandatoryQuestions> nonLimitedCompanyQuestions, Integer schemeId) {
        final List<List<String>> charitiesAndCompanies = exportSpotlightChecks(schemeId, companiesAndCharitiesQuestions,
                false);
        final String charitiesAndCompaniesFilename = generateExportFileName(schemeId, " charities_and_companies");


        final List<List<String>> nonLimitedCompanies = exportSpotlightChecks(schemeId, nonLimitedCompanyQuestions,
                false);
        final String nonLimitedCompaniesFilename = generateExportFileName(schemeId, "non_limited_companies");

        final List<List<List<String>>> dataList = List.of(charitiesAndCompanies, nonLimitedCompanies);
        final List<String> filenames = List.of(charitiesAndCompaniesFilename, nonLimitedCompaniesFilename);
        return zipService.createZip(SpotlightHeaders.SPOTLIGHT_HEADERS, dataList, filenames);

    }

    public ByteArrayOutputStream getDueDiligenceData(Integer schemeId) {
        final List<GrantMandatoryQuestions> mandatoryQuestions = getGrantMandatoryQuestionBySchemeAndCompletedStatus(
                schemeId);
        final List<List<String>> exportData = exportSpotlightChecks(schemeId, mandatoryQuestions, true);
        return XlsxGenerator.createResource(DueDiligenceHeaders.DUE_DILIGENCE_HEADERS, exportData);
    }

    private List<List<String>> exportSpotlightChecks(Integer schemeId,
            List<GrantMandatoryQuestions> grantMandatoryQuestions, boolean addOrgType) {
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

        return grantMandatoryQuestions.stream()
                .map(grantMandatoryQuestion -> buildSingleSpotlightRow(grantMandatoryQuestion, addOrgType)).toList();
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
    public List<String> buildSingleSpotlightRow(GrantMandatoryQuestions grantMandatoryQuestions, boolean addOrgType) {
        try {
            final Integer schemeId = grantMandatoryQuestions.getSchemeEntity().getId();
            final List<String> row = new ArrayList<>(
                    List.of(mandatoryValue(schemeId, "gap id", grantMandatoryQuestions.getGapId()),
                            mandatoryValue(schemeId, "organisation name", grantMandatoryQuestions.getName()),
                            combineAddressLines(grantMandatoryQuestions.getAddressLine1(),
                                    grantMandatoryQuestions.getAddressLine2()),
                            grantMandatoryQuestions.getCity(), grantMandatoryQuestions.getCounty(),
                            mandatoryValue(schemeId, "postcode", grantMandatoryQuestions.getPostcode()),
                            mandatoryValue(schemeId, "application amount",
                                    grantMandatoryQuestions.getFundingAmount().toString()),
                            Objects.requireNonNullElse(grantMandatoryQuestions.getCharityCommissionNumber(), ""),
                            Objects.requireNonNullElse(grantMandatoryQuestions.getCompaniesHouseNumber(), "")));
            if (addOrgType) {
                row.add(mandatoryValue(schemeId, "organisation type", grantMandatoryQuestions.getOrgType().toString()));
            }
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
        final String dateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        return dateString + "_" + ggisReference + "_" + schemeName + (orgType == null ? "" : "_" + orgType) + ".xlsx";
    }

    public boolean hasCompletedMandatoryQuestions(Integer schemeId) {
        return grantMandatoryQuestionRepository.existsBySchemeEntity_IdAndCompletedStatus(schemeId);
    }

}
