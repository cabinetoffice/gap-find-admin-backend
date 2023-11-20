package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class SpotlightSubmissionServiceTest {

    private static final UUID spotlightSubmissionId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final UUID spotlightBatchId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private SpotlightSubmissionRepository spotlightSubmissionRepository;

    @InjectMocks
    private SpotlightSubmissionService spotlightSubmissionService;

    private final Integer SCHEME_ID = 1;

    private final LocalDate date = LocalDate.of(2023, 9, 25);

    private final LocalTime time = LocalTime.of(0, 0, 0);

    private final LocalDateTime dateTime = LocalDateTime.of(date, time);

    private final Instant DATE = dateTime.toInstant(ZoneOffset.UTC);

    private final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(UUID.randomUUID())
            .lastSendAttempt(DATE).build();

    @Test
    void getSubmissionsByBySchemeIdAndStatus_ReturnsList() {
        when(spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT.toString())).thenReturn(List.of(spotlightSubmission));

        final List<SpotlightSubmission> result = spotlightSubmissionService
                .getSubmissionsByBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT);

        verify(spotlightSubmissionRepository).findByGrantSchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT.toString());
        assertThat(result).isEqualTo(List.of(spotlightSubmission));
    }

    @Test
    void getCountBySchemeIdAndStatus_ReturnsCount() {
        when(spotlightSubmissionRepository.countByGrantSchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT.toString())).thenReturn(Long.valueOf(2));

        final long result = spotlightSubmissionService.getCountBySchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT);

        verify(spotlightSubmissionRepository).countByGrantSchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT.toString());
        assertThat(result).isEqualTo(2);

    }

    @Nested
    class getLastSubmissionDate {

        @Test
        void getLastSubmissionDate_NoSubmissions() {
            when(spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT.toString())).thenReturn(List.of());
            final String result = spotlightSubmissionService.getLastSubmissionDate(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
            assertThat(result).isNull();
        }

        @Test
        void getLastSubmissionDate_HasSubmissions() {
            when(spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT.toString())).thenReturn(List.of(spotlightSubmission));
            final String result = spotlightSubmissionService.getLastSubmissionDate(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
            assertThat(result).isEqualTo("25 September 2023");
        }

    }

    @Nested
    class getSpotlightSubmission {

        @Test
        void getSpotlightSubmission() {
            final SpotlightSubmission mockSpotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                    .build();

            when(spotlightSubmissionRepository.findById(spotlightSubmissionId))
                    .thenReturn(Optional.of(mockSpotlightSubmission));

            final SpotlightSubmission result = spotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId);

            assertEquals(mockSpotlightSubmission, result);
        }

        @Test
        void getSpotlightSubmissionNotFound() {

            when(spotlightSubmissionRepository.findById(spotlightSubmissionId)).thenReturn(Optional.empty());

            final NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> spotlightSubmissionService.getSpotlightSubmission(spotlightSubmissionId));

            assertEquals("A spotlight submission with id " + spotlightSubmissionId + " could not be found",
                    exception.getMessage());
        }

    }

    @Nested
    class addSpotlightBatchToSpotlightSubmission {

        @Test
        void addSpotlightBatchToSpotlightSubmission() {

            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                    .batches(new ArrayList<>()).build();

            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(spotlightBatchId).build();

            when(spotlightSubmissionRepository.findById(spotlightSubmissionId))
                    .thenReturn(Optional.of(spotlightSubmission));
            when(spotlightSubmissionRepository.save(any(SpotlightSubmission.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            spotlightSubmissionService.addSpotlightBatchToSpotlightSubmission(spotlightSubmissionId, spotlightBatch);

            assertNotNull(spotlightSubmission.getBatches());
            assertEquals(1, spotlightSubmission.getBatches().size());
            assertTrue(spotlightSubmission.getBatches().contains(spotlightBatch));
        }

        @Test
        void addSpotlightBatchToSpotlightSubmissionSubmissionNotFound() {

            when(spotlightSubmissionRepository.findById(spotlightSubmissionId)).thenReturn(Optional.empty());

            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(spotlightBatchId).build();

            final NotFoundException exception = assertThrows(NotFoundException.class, () -> spotlightSubmissionService
                    .addSpotlightBatchToSpotlightSubmission(spotlightSubmissionId, spotlightBatch));

            assertEquals("A spotlight submission with id " + spotlightSubmissionId + " could not be found",
                    exception.getMessage());
        }

    }

}
