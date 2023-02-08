package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;

import java.time.Instant;
import java.util.UUID;

public class RandomGrantExportEntityGenerator {

    public static GrantExportEntity.GrantExportEntityBuilder randomGrantExportEntityBuilder() {
        return GrantExportEntity.builder().id(randomGrantExportEntityIdBuilder().build()).applicationId(1)
                .status(GrantExportStatus.REQUESTED).emailAddress("test@and.digital").created(Instant.now())
                .createdBy(1).lastUpdated(Instant.now()).location("/test_location");
    }

    public static GrantExportId.GrantExportIdBuilder randomGrantExportEntityIdBuilder() {
        return GrantExportId.builder().exportBatchId(UUID.randomUUID()).submissionId(UUID.randomUUID());
    }

}
