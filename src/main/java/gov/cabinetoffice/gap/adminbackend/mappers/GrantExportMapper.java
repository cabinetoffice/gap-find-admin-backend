package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.ZonedDateTime;


@Mapper(componentModel = "spring", uses = {CustomGrantExportMapperImpl.class})
public interface GrantExportMapper {

    @Mapping(target = "exportBatchId", source = "id.exportBatchId")
    @Mapping(target = "submissionId", source = "id.submissionId")
    GrantExportDTO grantExportEntityToGrantExportDTO(GrantExportEntity grantExportEntity);

    @Mapping(target = "submissionId", source = "id.submissionId")
    @Mapping(target = "zipFileLocation", source = "location")
    @Mapping(target = "name", expression = "java(mapExportedSubmissionName(grantExportEntity))")
    @Mapping(target ="status", source = "status")
    @Mapping(target = "submittedDate", expression = "java(mapExportedSubmissionSubmittedDate(grantExportEntity))")
    @Mapping(target = "submissionName", expression = "java(mapExportedSubmissionSubmissionName(grantExportEntity))")
    ExportedSubmissionsDto grantExportEntityToExportedSubmissions(GrantExportEntity grantExportEntity);

    default String mapExportedSubmissionName(GrantExportEntity grantExportEntity) {
        return "";
    }

    default ZonedDateTime mapExportedSubmissionSubmittedDate(GrantExportEntity grantExportEntity) {
        return null;
    }

    default String mapExportedSubmissionSubmissionName(GrantExportEntity grantExportEntity) {
        return "";
    }

}
