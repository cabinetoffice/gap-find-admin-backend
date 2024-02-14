package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportBatchRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
public class GrantExportBatchServiceTest {

    @Mock
    private GrantExportBatchRepository grantExportBatchRepository;

    @InjectMocks
    @Spy
    private GrantExportBatchService grantExportBatchService;

    final UUID mockExportId = UUID.randomUUID();

    @Nested
    class updateExportBatchStatusById {

        @Test
        void successfullyUpdateExportBatchStatusById() {
            when(grantExportBatchRepository.updateStatusById(mockExportId,  GrantExportStatus.COMPLETE.toString())).thenReturn(1);
            grantExportBatchService.updateExportBatchStatusById(mockExportId, GrantExportStatus.COMPLETE);
            verify(grantExportBatchRepository).updateStatusById(mockExportId, GrantExportStatus.COMPLETE.toString());
        }

        @Test
        void errorThrownFromRepository() {
            when(grantExportBatchRepository.updateStatusById(mockExportId,GrantExportStatus.COMPLETE.toString())).thenReturn(0);
            assertThrows(RuntimeException.class, () ->
                    grantExportBatchService.updateExportBatchStatusById(mockExportId, GrantExportStatus.COMPLETE),
                    "Could not update entry in grant_export_batch table to " + GrantExportStatus.COMPLETE);
        }
    }

    @Test
    void successfullyUpdatesLocation() {
        final String s3ObjectKey = "s3ObjectKey";

        when(grantExportBatchRepository.updateLocationById(mockExportId, s3ObjectKey)).thenReturn(1);
        grantExportBatchService.addS3ObjectKeyToGrantExportBatch(mockExportId, s3ObjectKey);
        verify(grantExportBatchRepository).updateLocationById(mockExportId, s3ObjectKey);
    }

}
