package gov.cabinetoffice.gap.adminbackend.services;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.google.common.collect.Lists;
import gov.cabinetoffice.gap.adminbackend.constants.AWSConstants;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.LambdaSubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionExportsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionSection;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SpotlightExportException;
import gov.cabinetoffice.gap.adminbackend.mappers.SubmissionMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SubmissionRepository;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import gov.cabinetoffice.gap.adminbackend.utils.XlsxGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionsService {

    static final List<String> SPOTLIGHT_HEADERS = Arrays.asList("Application number (required)",
            "Organisation name (required)", "Address street (optional)", "Address town (optional)",
            "Address county (optional)", "Address postcode (required)", "Application amount (required)",
            "Charity Commission registration number (required - if applicable)",
            "Companies House registration number (required - if applicable)",
            "Similarities to other applications (optional)");

    private final SubmissionRepository submissionRepository;

    private final GrantExportRepository grantExportRepository;

    private final ApplicationFormService applicationFormService;

    private final SchemeService schemeService;

    private final AmazonSQS amazonSQS;

    private final SubmissionMapper submissionMapper;

    @Value("${cloud.aws.sqs.submissions-export-queue}")
    private String submissionsExportQueue;

    public ByteArrayOutputStream exportSpotlightChecks(Integer applicationId) {
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();

        Integer createdBy = applicationFormService.retrieveApplicationFormSummary(applicationId, false, false)
                .getAudit().getCreatedBy();
        // the submissions being exported must be to an application the admin has created
        if (!Objects.equals(createdBy, adminSession.getGrantAdminId())) {
            throw new AccessDeniedException("Admin " + adminSession.getGrantAdminId()
                    + " is unable to access application with id " + applicationId);
        }

        // TODO GAP-1377 we need to limit the number of submissions we export
        // to 1000 rows in a file due to a restriction the Spotlight input
        // processing. So we will need to "page" the returned data and create
        // multiple files if there are more than 999 submissions.

        final List<Submission> submissionsByAppId = submissionRepository
                .findByApplicationGrantApplicationIdAndStatus(applicationId, SubmissionStatus.SUBMITTED);
        log.info("Found {} submissions in SUBMITTED state for application ID {}", submissionsByAppId.size(),
                applicationId);

        List<List<String>> spotlightExportData = new ArrayList<>();
        submissionsByAppId.forEach(submission -> {
            try {
                spotlightExportData.add(buildSingleSpotlightRow(submission));
            }
            catch (SpotlightExportException e) {
                log.error("Problem extracting data: " + e.getMessage());
            }
        });

        return XlsxGenerator.createResource(SPOTLIGHT_HEADERS, spotlightExportData);
    }

    public void updateSubmissionLastRequiredChecksExport(Integer applicationId) {
        submissionRepository.updateSubmissionLastRequiredChecksExportByGrantApplicationId(applicationId, Instant.now());
    }

    static String mandatoryValue(UUID id, String identifier, String value) {
        if (StringUtils.isBlank(value)) {
            throw new SpotlightExportException(
                    "Missing mandatory " + identifier + " value for submission_id " + id.toString());
        }
        return value;
    }

    static String combineAddressLines(String[] addressLines) {
        if (addressLines == null || addressLines.length < 2) {
            return "";
        }

        if (StringUtils.isEmpty(addressLines[0]) && StringUtils.isEmpty(addressLines[1])) {
            return "";
        }

        if (StringUtils.isEmpty(addressLines[1])) {
            return StringUtils.defaultString(addressLines[0]);
        }

        if (StringUtils.isEmpty(addressLines[0])) {
            return StringUtils.defaultString(addressLines[1]);
        }

        return String.join(", ", StringUtils.defaultString(addressLines[0]),
                StringUtils.defaultString(addressLines[1]));
    }

    /**
     * The ordering of the data added here is strongly tied to SPOTLIGHT_HEADERS. If new
     * headers are added or the ordering is changed in SPOTLIGHT_HEADERS, this will need
     * manually reflected here.
     */
    public List<String> buildSingleSpotlightRow(Submission submission) {
        try {
            SubmissionSection section = submission.getDefinition().getSectionById("ESSENTIAL");

            final UUID subId = submission.getId();
            final String gapId = submission.getGapId();
            final String organisationName = section.getQuestionById("APPLICANT_ORG_NAME").getResponse();
            final String[] applicantAddress = section.getQuestionById("APPLICANT_ORG_ADDRESS").getMultiResponse();
            final String addressStreet = combineAddressLines(applicantAddress);
            final String addressTown = applicantAddress[2];
            final String addressCounty = applicantAddress[3];
            final String postcode = applicantAddress[4];
            final String applicationAmount = section.getQuestionById("APPLICANT_AMOUNT").getResponse();
            final String charityNumber = section.getQuestionById("APPLICANT_ORG_CHARITY_NUMBER").getResponse();
            final String companyNumber = section.getQuestionById("APPLICANT_ORG_COMPANIES_HOUSE").getResponse();

            List<String> row = new ArrayList<>();
            row.add(mandatoryValue(subId, "GAP_ID", gapId));
            row.add(mandatoryValue(subId, "APPLICANT_ORG_NAME", organisationName));
            row.add(addressStreet);
            row.add(addressTown);
            row.add(addressCounty);
            row.add(mandatoryValue(subId, "POSTCODE", postcode));
            row.add(mandatoryValue(subId, "APPLICANT_AMOUNT", applicationAmount));
            row.add(charityNumber);
            row.add(companyNumber);
            row.add(""); // similarities data - should always be blank

            return row;
        }
        catch (NotFoundException e) {
            throw new SpotlightExportException("Unable to find submission section or question: " + e.getMessage());
        }
    }

    public String generateExportFileName(Integer applicationId) {
        ApplicationFormDTO applicationFormDTO = applicationFormService.retrieveApplicationFormSummary(applicationId,
                false, false);
        String ggisReference = schemeService.getSchemeBySchemeId(applicationFormDTO.getGrantSchemeId())
                .getGgisReference();
        String applicationName = applicationFormDTO.getApplicationName().replace(" ", "_").replaceAll("[^A-Za-z0-9_]",
                "");
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        return dateString + "_" + ggisReference + "_" + applicationName + ".xlsx";
    }

    @Transactional
    public void triggerSubmissionsExport(Integer applicationId) {
        UUID exportBatchId = UUID.randomUUID();
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();
        List<Submission> submissions = submissionRepository.findByApplicationGrantApplicationIdAndStatus(applicationId,
                SubmissionStatus.SUBMITTED);

        if (submissions.isEmpty()) {
            throw new NotFoundException("No submissions in SUBMITTED state for application " + applicationId);
        }

        // split the submissions into groups of 10, process in batches
        List<List<Submission>> partitionedSubmissions = Lists.partition(submissions,
                AWSConstants.MAX_ALLOWED_SQS_FIFO_BATCH_SIZE);

        partitionedSubmissions.stream().map(submissionsBatch -> mapExportRecordListToBatchMessageRequest(applicationId,
                exportBatchId, adminSession, submissionsBatch)).forEach(exportRecordsBatch -> {
                    grantExportRepository.saveAll(exportRecordsBatch);
                    amazonSQS.sendMessageBatch(mapExportRecordListToBatchMessageRequest(exportRecordsBatch));
                });
    }

    private List<GrantExportEntity> mapExportRecordListToBatchMessageRequest(Integer applicationId, UUID exportId,
            AdminSession adminSession, List<Submission> list) {
        return list.stream()
                .map(submission -> GrantExportEntity.builder().id(new GrantExportId(exportId, submission.getId()))
                        .status(GrantExportStatus.REQUESTED).applicationId(applicationId)
                        .emailAddress(adminSession.getEmailAddress()).createdBy(adminSession.getGrantAdminId()).build())
                .toList();
    }

    private SendMessageBatchRequest mapExportRecordListToBatchMessageRequest(
            List<GrantExportEntity> grantExportEntityList) {
        // maps each export record into the MessageAttributes
        // DedupId is set as a random uuid per batch, MessageGroupId is set as
        // exportBatchId
        List<SendMessageBatchRequestEntry> sendMessageBatchRequestEntries = grantExportEntityList.stream()
                .map(exportRecord -> {
                    String randomUuid = UUID.randomUUID().toString();
                    return new SendMessageBatchRequestEntry().withId(randomUuid).withMessageBody(randomUuid)
                            .withMessageDeduplicationId(randomUuid)
                            .withMessageGroupId(exportRecord.getId().getExportBatchId().toString())
                            .addMessageAttributesEntry("submissionId",
                                    new MessageAttributeValue().withDataType("String")
                                            .withStringValue(exportRecord.getId().getSubmissionId().toString()))
                            .addMessageAttributesEntry("exportBatchId",
                                    new MessageAttributeValue().withDataType("String")
                                            .withStringValue(exportRecord.getId().getExportBatchId().toString()))
                            .addMessageAttributesEntry("applicationId",
                                    new MessageAttributeValue().withDataType("Number")
                                            .withStringValue(exportRecord.getApplicationId().toString()))
                            .addMessageAttributesEntry("emailAddress",
                                    new MessageAttributeValue().withDataType("String")
                                            .withStringValue(exportRecord.getEmailAddress()))
                            .addMessageAttributesEntry("created",
                                    new MessageAttributeValue().withDataType("String")
                                            .withStringValue(exportRecord.getCreated().toString()))
                            .addMessageAttributesEntry("createdBy", new MessageAttributeValue().withDataType("String")
                                    .withStringValue(exportRecord.getCreatedBy().toString()));
                }).toList();

        return new SendMessageBatchRequest(submissionsExportQueue).withEntries(sendMessageBatchRequestEntries);

    }

    // TODO handle new EXPIRED and ERROR statuses
    public GrantExportStatus getExportStatus(Integer applicationId) {
        if (!grantExportRepository.existsByApplicationId(applicationId)) {
            return GrantExportStatus.NOT_STARTED;
        }
        if (grantExportRepository.existsByApplicationIdAndStatus(applicationId, GrantExportStatus.PROCESSING)) {
            return GrantExportStatus.PROCESSING;
        }
        if (grantExportRepository.existsByApplicationIdAndStatus(applicationId, GrantExportStatus.REQUESTED)) {
            return GrantExportStatus.REQUESTED;
        }
        return GrantExportStatus.COMPLETE;
    }

    public List<SubmissionExportsDTO> getCompletedSubmissionExportsForBatch(UUID exportBatchId) {

        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();

        List<GrantExportEntity> exports = grantExportRepository.findAllByIdExportBatchIdAndStatusAndCreatedBy(
                exportBatchId, GrantExportStatus.COMPLETE, adminSession.getGrantAdminId());

        return exports.stream().map(entity -> SubmissionExportsDTO.builder().url(entity.getLocation())
                .label(getFilenameFromExportsSignedUrl(entity)).build()).toList();
    }

    /**
     * <p>
     * Extract filename from AWS SignedURL. Expected path format: <strong>
     * "/{directory}/{filename}" </strong>
     * </p>
     *
     * <p>
     * Returns submission id as a fallback in case of errors
     * </p>
     * @param exportEntity
     * @return filename
     */
    private String getFilenameFromExportsSignedUrl(GrantExportEntity exportEntity) {

        try {
            URL url = new URL(exportEntity.getLocation());
            return URLDecoder.decode(url.getPath().split("/", 3)[2], StandardCharsets.UTF_8.name());
        }
        catch (Exception e) {
            return exportEntity.getId().getSubmissionId().toString();
        }

    }

    public LambdaSubmissionDefinition getSubmissionInfo(final UUID submissionId, final UUID exportBatchId) {

        if (!grantExportRepository
                .existsById(GrantExportId.builder().exportBatchId(exportBatchId).submissionId(submissionId).build())) {
            throw new NotFoundException();
        }

        final Submission submission = submissionRepository.findById(submissionId).orElseThrow(NotFoundException::new);

        return submissionMapper.submissionToLambdaSubmissionDefinition(submission);
    }

    public void updateExportStatus(String submissionId, String batchExportId, GrantExportStatus status) {
        final Integer result = grantExportRepository.updateExportRecordStatus(submissionId, batchExportId,
                status.toString());

        if (result == 1) {
            log.info(String.format("Updated entry in export records table to %s\nexportBatchId: %s\nsubmissionId: %s",
                    status, batchExportId, submissionId));
        }
        else {
            log.error(String.format(
                    "Could not update entry in export records table to %s\nexportBatchId: %s\nsubmissionId: %s", status,
                    batchExportId, submissionId));
            throw new RuntimeException("Could not update entry in export records table to " + status);
        }
    }

    public void addSignedUrlToSubmissionExport(UUID submissionId, UUID exportId, String signedUrl) {
        grantExportRepository.updateExportRecordLocation(submissionId, exportId, signedUrl);
    }

}
