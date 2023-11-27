package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig
@WithAdminSession
class SpotlightSubmissionServiceTest {

    private static final UUID spotlightSubmissionId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Mock
    private SpotlightSubmissionRepository spotlightSubmissionRepository;

    @Spy
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
    class getSpotlightSubmissionByMandatoryQuestionGapId {

        @Test
        void getSpotlightSubmissionByMandatoryQuestionGapId_success() {
            when(spotlightSubmissionRepository.findByMandatoryQuestions_GapId("gapId"))
                    .thenReturn(Optional.of(spotlightSubmission));

            final SpotlightSubmission result = spotlightSubmissionService
                    .getSpotligtSubmissionByMandatoryQuestionGapId("gapId");

            verify(spotlightSubmissionRepository).findByMandatoryQuestions_GapId("gapId");

            assertThat(result).isEqualTo(spotlightSubmission);
        }

        @Test
        void getSpotlightSubmissionByMandatoryQuestionGapId_notFound() {
            when(spotlightSubmissionRepository.findByMandatoryQuestions_GapId("gapId")).thenReturn(Optional.empty());

            final NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> spotlightSubmissionService.getSpotligtSubmissionByMandatoryQuestionGapId("gapId"));

            assertEquals("A spotlight submission with mandatory question gapId gapId could not be found",
                    exception.getMessage());
        }

    }

    @Nested
    class getLastSubmissionDate {

        @Test
        void getLastSubmissionDate_NoSubmissions() {
            when(spotlightSubmissionRepository.findByGrantSchemeIdAndStatus(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT.toString())).thenReturn(List.of());
            final String result = spotlightSubmissionService.getLastSubmissionDate(SCHEME_ID,
                    SpotlightSubmissionStatus.SENT);
            assertThat(result).isEmpty();
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
    class getSpotlightSubmissionById {

        @Test
        void returnsExpectedSubmission() {
            final SpotlightSubmission mockSpotlightSubmission = SpotlightSubmission.builder().id(spotlightSubmissionId)
                    .build();

            doReturn(mockSpotlightSubmission).when(spotlightSubmissionService)
                    .getSpotlightSubmission(spotlightSubmissionId);

            final Optional<SpotlightSubmission> result = spotlightSubmissionService
                    .getSpotlightSubmissionById(spotlightSubmissionId);

            assertEquals(Optional.of(mockSpotlightSubmission), result);
        }

        @Test
        void returnsOptionalEmpty() {

            doThrow(NotFoundException.class).when(spotlightSubmissionService)
                    .getSpotlightSubmission(spotlightSubmissionId);

            final Optional<SpotlightSubmission> result = spotlightSubmissionService
                    .getSpotlightSubmissionById(spotlightSubmissionId);

            assertEquals(Optional.empty(), result);
        }

    }

}
