package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.constants.SpotlightHeaders;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SpotlightExportException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotlightSubmissionService {

    // make sure to don't use SpotlighBatchService here, otherwise you will get a circular
    // dependency
    private final SpotlightSubmissionRepository spotlightSubmissionRepository;

    private final ZipService zipService;

    public SpotlightSubmission getSpotlightSubmission(UUID spotlightSubmissionId) {
        return spotlightSubmissionRepository.findById(spotlightSubmissionId).orElseThrow(() -> new NotFoundException(
                "A spotlight submission with id " + spotlightSubmissionId + " could not be found"));
    }

    public Optional<SpotlightSubmission> getSpotlightSubmissionById(UUID spotlightSubmissionId) {
        Optional<SpotlightSubmission> spotlightSubmission = Optional.empty();

        try {
            spotlightSubmission = Optional.of(this.getSpotlightSubmission(spotlightSubmissionId));
        }
        catch (NotFoundException e) {
            log.error("No spotlight submission with ID {} found", spotlightSubmissionId);
        }

        return spotlightSubmission;
    }

    public List<SpotlightSubmission> getSubmissionsByBySchemeIdAndStatus(Integer schemeId,
            SpotlightSubmissionStatus status) {
        return spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(schemeId, status.toString());
    }

    public long getCountBySchemeIdAndStatus(Integer schemeId, SpotlightSubmissionStatus status) {
        return spotlightSubmissionRepository.countByGrantSchemeIdAndStatus(schemeId, status.toString());
    }

    public String getLastSubmissionDate(Integer schemeId, SpotlightSubmissionStatus status) {
        final List<SpotlightSubmission> spotlightSubmissions = getSubmissionsByBySchemeIdAndStatus(schemeId, status);
        return spotlightSubmissions.stream().map(SpotlightSubmission::getLastSendAttempt).max(Instant::compareTo)
                .map(date -> date.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .orElse("");
    }

    public SpotlightSubmission getSpotlightSubmissionByMandatoryQuestionGapId(String gapId) {
        return spotlightSubmissionRepository.findByMandatoryQuestions_GapId(gapId)
                .orElseThrow(() -> new NotFoundException(
                        "A spotlight submission with mandatory question gapId " + gapId + " could not be found"));
    }

    public boolean doesSchemeHaveSpotlightSubmission(Integer schemeId) {
        return spotlightSubmissionRepository.existsByGrantScheme_Id(schemeId);
    }

    public ByteArrayOutputStream generateDownloadFile(SchemeDTO scheme, boolean onlyValidationErrors) {
        List<SpotlightSubmission> spotlightSubmissions = spotlightSubmissionRepository
                .findByGrantScheme_Id(scheme.getSchemeId());

        if (onlyValidationErrors) {
            spotlightSubmissions = spotlightSubmissions.stream()
                    .filter(s -> s.getStatus().equals(SpotlightSubmissionStatus.VALIDATION_ERROR.toString())).toList();
        }

        final List<SpotlightSubmission> companiesAndCharitiesSubmissions = spotlightSubmissions.stream()
                .filter(s -> s.getMandatoryQuestions().getOrgType().equals(LIMITED_COMPANY)
                        || s.getMandatoryQuestions().getOrgType().equals(CHARITY)
                        || s.getMandatoryQuestions().getOrgType().equals(REGISTERED_CHARITY)
                        || s.getMandatoryQuestions().getOrgType().equals(UNREGISTERED_CHARITY))
                .toList();

        final List<SpotlightSubmission> nonLimitedCompanySubmissions = spotlightSubmissions.stream()
                .filter(s -> s.getMandatoryQuestions().getOrgType().equals(NON_LIMITED_COMPANY)).toList();

        return generateZipFile(companiesAndCharitiesSubmissions, nonLimitedCompanySubmissions, scheme);

    }

    private ByteArrayOutputStream generateZipFile(List<SpotlightSubmission> companiesAndCharitiesSubmissions,
            List<SpotlightSubmission> nonLimitedSubmissions, SchemeDTO scheme) {

        final List<List<String>> charitiesAndCompanies = exportSpotlightChecks(companiesAndCharitiesSubmissions);
        final String charitiesAndCompaniesFilename = generateExportFileName(scheme, " charities_and_companies");

        final List<List<String>> nonLimitedCompanies = exportSpotlightChecks(nonLimitedSubmissions);
        final String nonLimitedCompaniesFilename = generateExportFileName(scheme, "non_limited_companies");

        final List<List<List<String>>> dataList = List.of(charitiesAndCompanies, nonLimitedCompanies);
        final List<String> filenames = List.of(charitiesAndCompaniesFilename, nonLimitedCompaniesFilename);
        return zipService.createZip(SpotlightHeaders.SPOTLIGHT_HEADERS, dataList, filenames);
    }

    private List<List<String>> exportSpotlightChecks(List<SpotlightSubmission> spotlightSubmissions) {
        return spotlightSubmissions.stream()
                .map(spotlightSubmission -> buildSingleSpotlightRow(spotlightSubmission.getMandatoryQuestions()))
                .toList();
    }

    /**
     * The ordering of the data added here is strongly tied to SPOTLIGHT_HEADERS. If new
     * headers are added or the ordering is changed in SPOTLIGHT_HEADERS, this will need
     * manually reflected here.
     */
    public List<String> buildSingleSpotlightRow(GrantMandatoryQuestions grantMandatoryQuestions) {
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
            row.add(""); // similarities data - should always be blank
            return row;
        }
        catch (NullPointerException e) {
            throw new SpotlightExportException("Unable to find mandatory question data: " + e.getMessage());
        }
    }

    public String generateExportFileName(SchemeDTO scheme, String orgType) {
        final String ggisReference = scheme.getGgisReference();
        final String schemeName = scheme.getName().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");
        final String dateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        return dateString + "_" + ggisReference + "_" + schemeName + (orgType == null ? "" : "_" + orgType) + ".xlsx";
    }

    protected String mandatoryValue(Integer id, String identifier, String value) {
        if (StringUtils.isBlank(value)) {
            throw new SpotlightExportException(
                    "Missing mandatory " + identifier + " value for schemeId " + id.toString());
        }
        return value;
    }

    protected String combineAddressLines(String addressLine1, String addressLine2) {

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

}
