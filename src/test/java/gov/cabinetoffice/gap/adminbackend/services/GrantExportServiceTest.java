package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.ExportedSubmissionsListDto;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.grantExport.GrantExportListDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.CustomGrantExportMapperImpl;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
public class GrantExportServiceTest {

    @Mock
    private GrantExportRepository exportRepository;

    @Mock
    private CustomGrantExportMapperImpl customGrantExportMapper;

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

    @Nested
    class getGrantExportsByIdAndStatus {

        @Test
        void successfullyGetGrantExports() {
            final Instant date = Instant.now();
            final GrantExportId id = GrantExportId.builder()
                .exportBatchId(UUID.randomUUID())
                .submissionId(UUID.randomUUID())
                .build();
            final List<GrantExportEntity> mockGrantExports = Collections.singletonList(GrantExportEntity.builder()
                .id(id)
                .applicationId(1)
                .status(GrantExportStatus.COMPLETE)
                .created(date)
                .createdBy(1)
                .lastUpdated(date)
                .location("location")
                .emailAddress("test-email@gmail.com")
                .build());
            final GrantExportDTO mockGrantExportDTO = GrantExportDTO.builder()
                .exportBatchId(id.getExportBatchId())
                .submissionId(id.getSubmissionId())
                .applicationId(1)
                .status(GrantExportStatus.COMPLETE)
                .created(date)
                .createdBy(1)
                .lastUpdated(date)
                .location("location")
                .emailAddress("test-email@gmail.com")
                .build();
            final List<GrantExportDTO> mockGrantExportDtoList = Collections.singletonList(mockGrantExportDTO);
            final GrantExportListDTO mockGrantExportList = GrantExportListDTO.builder()
                .exportBatchId(id.getExportBatchId())
                .grantExports(mockGrantExportDtoList)
                .build();

            when(exportRepository.findById_ExportBatchIdAndStatus(id.getExportBatchId(), GrantExportStatus.COMPLETE))
                .thenReturn(mockGrantExports);
            when(customGrantExportMapper.grantExportEntityToGrantExportDTO(any())).thenReturn(mockGrantExportDTO);

            final GrantExportListDTO response = grantExportService.getGrantExportsByIdAndStatus(id.getExportBatchId(),
                    GrantExportStatus.COMPLETE);

            verify(exportRepository).findById_ExportBatchIdAndStatus(id.getExportBatchId(), GrantExportStatus.COMPLETE);
            assertThat(response).isEqualTo(mockGrantExportList);

        }

    }

    @Nested
    class getFailedExportsCount {

        @Test
        void getFailedExportsCount() {
            final UUID mockExportId = UUID.randomUUID();
            final long expectedResponse = 2L;
            when(exportRepository.countByIdExportBatchIdAndStatus(mockExportId, GrantExportStatus.FAILED))
                .thenReturn(expectedResponse);

            final long response = grantExportService.getFailedExportsCount(mockExportId);

            verify(exportRepository).countByIdExportBatchIdAndStatus(mockExportId, GrantExportStatus.FAILED);
            assertThat(response).isEqualTo(expectedResponse);
        }

    }

    @Nested
    class getRemainingExportsCount {

        @Test
        void getRemainingExportsCount() {
            final UUID mockExportId = UUID.randomUUID();
            final long expectedResponse = 2L;
            final List<GrantExportStatus> statusList = List.of(GrantExportStatus.COMPLETE, GrantExportStatus.FAILED);
            when(exportRepository.countByIdExportBatchIdAndStatusIsNotIn(mockExportId, statusList))
                .thenReturn(expectedResponse);

            final long response = grantExportService.getRemainingExportsCount(mockExportId);

            verify(exportRepository).countByIdExportBatchIdAndStatusIsNotIn(mockExportId, statusList);
            assertThat(response).isEqualTo(expectedResponse);

        }

    }

    @Nested
    @WithAdminSession
    class generateExportedSubmissionsListDto {
        public final static Integer SEC_CONTEXT_ADMIN_ID = 1;
        @Test
        void generateExportedSubmissionsListDto() {
            final UUID mockExportId = UUID.fromString("a3e3e3e3-3e3e-3e3e-3e3e-3e3e3e3e3e3e");
            final UUID submissionId = UUID.fromString("f5e3e3e3-3e3e-3e3e-3e3e-3e3e3e3e3e3e");
            final UUID submissionId2 = UUID.fromString("a3e3e3e3-3e3e-3e3e-3e3e-3e3e3e3e3e3e");
            final Pageable pagination = Pageable.unpaged();
            final GrantExportEntity grantExport = GrantExportEntity.builder()
                .id(GrantExportId.builder().exportBatchId(mockExportId).submissionId(submissionId).build())
                .status(GrantExportStatus.COMPLETE)
                .location("location")
                .build();
            final GrantExportEntity grantExport2 = GrantExportEntity.builder()
                .id(GrantExportId.builder().exportBatchId(mockExportId).submissionId(submissionId2).build())
                .status(GrantExportStatus.COMPLETE)
                .location("location2")
                .build();
            final ExportedSubmissionsDto exportedSubmissionsDto = ExportedSubmissionsDto.builder()
                .submissionId(submissionId)
                .zipFileLocation("location")
                .status(GrantExportStatus.COMPLETE)
                .name("name2")
                .build();
            final ExportedSubmissionsDto exportedSubmissionsDto2 = ExportedSubmissionsDto.builder()
                .submissionId(submissionId2)
                .zipFileLocation("location2")
                .status(GrantExportStatus.COMPLETE)
                .name("name1")
                .build();

            when(exportRepository.findByCreatedByAndId_ExportBatchIdAndStatus(SEC_CONTEXT_ADMIN_ID,mockExportId, GrantExportStatus.COMPLETE, pagination))
                .thenReturn(List.of(grantExport, grantExport2));
            when(customGrantExportMapper.grantExportEntityToExportedSubmissions(grantExport))
                .thenReturn(exportedSubmissionsDto);
            when(customGrantExportMapper.grantExportEntityToExportedSubmissions(grantExport2))
                .thenReturn(exportedSubmissionsDto2);

            final ExportedSubmissionsListDto response = grantExportService
                .generateExportedSubmissionsListDto(mockExportId, GrantExportStatus.COMPLETE, pagination);

            verify(exportRepository).findByCreatedByAndId_ExportBatchIdAndStatus(SEC_CONTEXT_ADMIN_ID,mockExportId, GrantExportStatus.COMPLETE, pagination);
            assertThat(response.getGrantExportId()).isEqualTo(mockExportId);
            assertThat(response.getExportedSubmissionDtos().get(0))
                .isEqualTo(exportedSubmissionsDto2);
            assertThat(response.getExportedSubmissionDtos().get(1))
                    .isEqualTo(exportedSubmissionsDto);
        }
    }

    @Nested
    class getExportCountByStatus{
        @Test
        void getExportCountByStatus() {
            final UUID mockExportId = UUID.randomUUID();
            final long expectedResponse = 2L;
            when(exportRepository.countByIdExportBatchIdAndStatus(mockExportId, GrantExportStatus.FAILED)).thenReturn(expectedResponse);

            final long response = grantExportService.getExportCountByStatus(mockExportId, GrantExportStatus.FAILED);

            verify(exportRepository).countByIdExportBatchIdAndStatus(mockExportId, GrantExportStatus.FAILED);
            assertThat(response).isEqualTo(expectedResponse);
        }
    }
}
