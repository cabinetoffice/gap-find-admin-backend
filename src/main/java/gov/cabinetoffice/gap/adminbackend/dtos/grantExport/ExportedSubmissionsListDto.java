package gov.cabinetoffice.gap.adminbackend.dtos.grantExport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ExportedSubmissionsListDto {

    private UUID grantExportId;
    @Builder.Default
    private List<ExportedSubmissionsDto> exportedSubmissions = List.of();
    private int failedCount;
    private int successCount;
    private String superZipFileLocation;

}
