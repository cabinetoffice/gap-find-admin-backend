package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportBatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportBatchEntity;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface GrantExportMapper {

    @Mapping(target = "exportBatchId", source = "id.exportBatchId")
    @Mapping(target = "submissionId", source = "id.submissionId")
    GrantExportDTO grantExportEntityToGrantExportDTO(GrantExportEntity grantExportEntity);

    @Mapping(target = "exportBatchId", source = "id")
    GrantExportBatchDTO grantExportBatchEntityToGrantExportBatchDTO(GrantExportBatchEntity grantExportBatchEntity);

}
