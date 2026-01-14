package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.repositories.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Primary
@Slf4j
public class CustomGrantExportMapperImpl implements GrantExportMapper {

    private static final String ESSENTIAL_SECTION_ID = "ESSENTIAL";
    private static final String APPLICANT_ORG_NAME = "APPLICANT_ORG_NAME";
    private static final String ORGANISATION_DETAILS_SECTION_ID = "ORGANISATION_DETAILS";
    private final SubmissionRepository submissionRepository;

    @Override
    public GrantExportDTO grantExportEntityToGrantExportDTO(GrantExportEntity grantExportEntity) {
        if ( grantExportEntity == null ) {
            return null;
        }

        GrantExportDTO.GrantExportDTOBuilder grantExportDTO = GrantExportDTO.builder();

        grantExportDTO.exportBatchId( grantExportEntityIdExportBatchId( grantExportEntity ) );
        grantExportDTO.submissionId( grantExportEntityIdSubmissionId( grantExportEntity ) );
        grantExportDTO.applicationId( grantExportEntity.getApplicationId() );
        grantExportDTO.status( grantExportEntity.getStatus() );
        grantExportDTO.emailAddress( grantExportEntity.getEmailAddress() );
        grantExportDTO.created( grantExportEntity.getCreated() );
        grantExportDTO.createdBy( grantExportEntity.getCreatedBy() );
        grantExportDTO.lastUpdated( grantExportEntity.getLastUpdated() );
        grantExportDTO.location( grantExportEntity.getLocation() );

        return grantExportDTO.build();
    }

    @Override
    public ExportedSubmissionsDto grantExportEntityToExportedSubmissions(GrantExportEntity grantExportEntity) {
        if (grantExportEntity == null) {
            return null;
        }

        ExportedSubmissionsDto.ExportedSubmissionsDtoBuilder exportedSubmissionsDto = ExportedSubmissionsDto.builder();

        exportedSubmissionsDto.submissionId(grantExportEntityIdSubmissionId(grantExportEntity));
        exportedSubmissionsDto.zipFileLocation(grantExportEntity.getLocation());
        exportedSubmissionsDto.status(grantExportEntity.getStatus());
        exportedSubmissionsDto.name(mapExportedSubmissionName(grantExportEntity));
        exportedSubmissionsDto.submittedDate(mapExportedSubmissionSubmittedDate(grantExportEntity));
        exportedSubmissionsDto.submissionName(mapExportedSubmissionSubmissionName(grantExportEntity));

        return exportedSubmissionsDto.build();
    }

    @Override
    public String mapExportedSubmissionName(GrantExportEntity grantExportEntity) {
        log.info("Getting legal name from grant export {} and submission {}", grantExportEntity.getId(),
                grantExportEntity.getId().getSubmissionId());
        final UUID submissionId = grantExportEntity.getId().getSubmissionId();
        final Optional<Submission> submission = submissionRepository.findById(submissionId);
        if (submission.isEmpty()) {
            log.error("Submission not found for id: {}", submissionId);
            return submissionId.toString();
        }
        final int schemeVersion = submission.get().getScheme().getVersion();
        return submission.get()
                .getDefinition()
                .getSectionById( schemeVersion > 1 ? ORGANISATION_DETAILS_SECTION_ID : ESSENTIAL_SECTION_ID )
                .getQuestionById(APPLICANT_ORG_NAME)
                .getResponse();
    }

    @Override
    public ZonedDateTime mapExportedSubmissionSubmittedDate(GrantExportEntity grantExportEntity) {
        log.info("Getting submitted date from grant export {} and submission {}", grantExportEntity.getId(),
                grantExportEntity.getId().getSubmissionId());

        final UUID submissionId = grantExportEntity.getId().getSubmissionId();
        final Optional<Submission> submission = submissionRepository.findById(submissionId);
        if (submission.isEmpty()) {
            log.error("Submission not found for id: {}", submissionId);
            return null;
        }

        return submission.get().getSubmittedDate();
    }

    @Override
    public String mapExportedSubmissionSubmissionName(GrantExportEntity grantExportEntity) {
        log.info("Getting submission name from grant export {} and submission {}", grantExportEntity.getId(),
                grantExportEntity.getId().getSubmissionId());
        final UUID submissionId = grantExportEntity.getId().getSubmissionId();
        final Optional<Submission> submission = submissionRepository.findById(submissionId);
        if (submission.isEmpty()) {
            log.error("Submission not found for id: {}", submissionId);
            return null;
        }
        return submission.get().getSubmissionName();
    }

    private UUID grantExportEntityIdSubmissionId(GrantExportEntity grantExportEntity) {
        if (grantExportEntity == null) {
            return null;
        }
        GrantExportId id = grantExportEntity.getId();
        if (id == null) {
            return null;
        }
        return id.getSubmissionId();

    }
    private UUID grantExportEntityIdExportBatchId(GrantExportEntity grantExportEntity) {
        if ( grantExportEntity == null ) {
            return null;
        }
        GrantExportId id = grantExportEntity.getId();
        if ( id == null ) {
            return null;
        }
        UUID exportBatchId = id.getExportBatchId();
        if ( exportBatchId == null ) {
            return null;
        }
        return exportBatchId;
    }

}
