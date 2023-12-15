package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
public class GrantExportServiceTest {

    @Mock
    private GrantExportRepository exportRepository;

    @InjectMocks
    @Spy
    private GrantExportService grantExportService;

    @Nested
    class getOutstandingExportCount {

        @Test
        void successfullyGetOutstandingExportCount() {
            Long mockCount = 10L;
            UUID mockUUID = UUID.randomUUID();

            when(exportRepository.countByIdExportBatchIdAndStatusNot(any(), any())).thenReturn(mockCount);

            Long response = grantExportService.getOutstandingExportCount(mockUUID);

            assertEquals(mockCount, response);
            verify(exportRepository).countByIdExportBatchIdAndStatusNot(mockUUID, GrantExportStatus.COMPLETE);
        }

        @Test
        void unnexpectedErrorThrownFromRepository() {
            UUID mockUUID = UUID.randomUUID();
            when(exportRepository.countByIdExportBatchIdAndStatusNot(any(), any())).thenThrow(RuntimeException.class);

            assertThrows(RuntimeException.class, () -> grantExportService.getOutstandingExportCount(mockUUID));
        }

    }

}
