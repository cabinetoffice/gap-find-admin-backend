package gov.cabinetoffice.gap.adminbackend.dtos.grantExport;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GrantExportListDTO {
    private UUID exportBatchId;
    private List<GrantExportDTO> grantExports;
}
