package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class GrantExportMapperTest {

    final Instant date = Instant.now();
    final GrantExportId id = GrantExportId.builder()
            .exportBatchId(UUID.randomUUID())
            .submissionId(UUID.randomUUID())
            .build();
    final GrantExportEntity grantExportEntity = GrantExportEntity.builder()
            .id(id)
            .applicationId(1)
            .status(GrantExportStatus.COMPLETE)
            .created(date)
            .createdBy(1)
            .lastUpdated(date)
            .location("location")
            .emailAddress("test-email@gmail.com")
            .build();

    @InjectMocks
    private GrantExportMapper grantExportMapper = Mappers.getMapper(GrantExportMapper.class);

    @Test
    public void testGrantExportEntityToGrantExportDTO() {
        final GrantExportDTO grantExportDTO = grantExportMapper.grantExportEntityToGrantExportDTO(grantExportEntity);

        assertThat(grantExportDTO).isNotNull();
        assertThat(grantExportDTO.getExportBatchId()).isEqualTo(grantExportEntity.getId().getExportBatchId());
        assertThat(grantExportDTO.getSubmissionId()).isEqualTo(grantExportEntity.getId().getSubmissionId());
        assertThat(grantExportDTO.getApplicationId()).isEqualTo(grantExportEntity.getApplicationId());
        assertThat(grantExportDTO.getStatus()).isEqualTo(grantExportEntity.getStatus());
        assertThat(grantExportDTO.getEmailAddress()).isEqualTo(grantExportEntity.getEmailAddress());
        assertThat(grantExportDTO.getCreated()).isEqualTo(grantExportEntity.getCreated());
        assertThat(grantExportDTO.getCreatedBy()).isEqualTo(grantExportEntity.getCreatedBy());
        assertThat(grantExportDTO.getLastUpdated()).isEqualTo(grantExportEntity.getLastUpdated());
        assertThat(grantExportDTO.getLocation()).isEqualTo(grantExportEntity.getLocation());
    }

}