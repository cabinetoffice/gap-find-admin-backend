package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.GetSpotlightBatchErrorCountDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightBatchRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@WithAdminSession
class SpotlightBatchServiceTest {

    private static final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    Pageable pageable = PageRequest.of(0, 1);

    @Mock
    private SpotlightBatchRepository spotlightBatchRepository;

    @InjectMocks
    private SpotlightBatchService spotlightBatchService;

    @Nested
    class SpotlightBatchWithStatusExistsTests {

        @Test
        void spotlightBatchWithStatusExists() {
            when(spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt()))
                    .thenReturn(true);

            final boolean result = spotlightBatchService.existsByStatusAndMaxBatchSize(SpotlightBatchStatus.QUEUED,
                    200);

            assertTrue(result);
        }

        @Test
        void spotlightBatchWithStatusDoesNotExist() {
            when(spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt()))
                    .thenReturn(false);

            final boolean result = spotlightBatchService.existsByStatusAndMaxBatchSize(SpotlightBatchStatus.QUEUED,
                    200);

            assertFalse(result);
        }

    }

    @Nested
    class GetSpotlightBatchWithStatusTests {

        @Test
        void spotlightBatchWithStatusExists() {
            final SpotlightBatch mockSpotlightBatch = SpotlightBatch.builder().id(uuid).build();
            when(spotlightBatchRepository.findByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt()))
                    .thenReturn(Optional.of(mockSpotlightBatch));

            final SpotlightBatch result = spotlightBatchService.getSpotlightBatchWithStatus(SpotlightBatchStatus.QUEUED,
                    200);

            assertEquals(mockSpotlightBatch, result);
        }

        @Test
        void spotlightBatchWithStatusDoesNotExist() {
            when(spotlightBatchRepository.findByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt()))
                    .thenReturn(Optional.empty());

            final NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> spotlightBatchService.getSpotlightBatchWithStatus(SpotlightBatchStatus.QUEUED, 200));

            assertEquals("A spotlight batch with status QUEUED could not be found", exception.getMessage());
        }

    }

    @Nested
    class CreateSpotlightBatchTests {

        @Test
        void createSpotlightBatch() {
            final SpotlightBatch mockSpotlightBatch = SpotlightBatch.builder().id(uuid).build();
            when(spotlightBatchRepository.save(any())).thenReturn(mockSpotlightBatch);

            final SpotlightBatch result = spotlightBatchService.createSpotlightBatch();

            assertEquals(mockSpotlightBatch, result);
        }

    }

    @Nested
    class AddSpotlightSubmissionToSpotlightBatchTests {

        @Test
        void addSpotlightSubmissionToSpotlightBatch() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().batches(new ArrayList<>())
                    .build();
            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().id(uuid)
                    .spotlightSubmissions(new ArrayList<>()).build();

            when(spotlightBatchRepository.findById(uuid)).thenReturn(Optional.of(spotlightBatch));
            when(spotlightBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            final SpotlightBatch result = spotlightBatchService
                    .addSpotlightSubmissionToSpotlightBatch(spotlightSubmission, uuid);

            verify(spotlightBatchRepository, times(1)).findById(uuid);
            verify(spotlightBatchRepository, times(1)).save(spotlightBatch);

            List<SpotlightSubmission> resultSubmissions = result.getSpotlightSubmissions();
            List<SpotlightBatch> resultBatches = spotlightSubmission.getBatches();

            assertThat(resultSubmissions).hasSize(1);
            assertThat(resultBatches).hasSize(1);
            assertThat(resultSubmissions.get(0)).isEqualTo(spotlightSubmission);
            assertThat(resultBatches.get(0)).isEqualTo(result);
        }

        @Test
        void addSpotlightSubmissionToSpotlightBatchBatchNotFound() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().build();
            final SpotlightBatch existingSpotlightBatch = SpotlightBatch.builder().id(uuid).build();

            when(spotlightBatchRepository.findById(uuid)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> spotlightBatchService.addSpotlightSubmissionToSpotlightBatch(spotlightSubmission, uuid));

            assertEquals("A spotlight batch with id " + uuid + " could not be found", exception.getMessage());
        }

    }

    @Nested
    class GetSpotlightBatchErrorCountTests {

        final Integer schemeId = 1;

        final SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId).build();

        final List<SpotlightBatch> spotlightBatches = Collections
                .singletonList(SpotlightBatch.builder().spotlightSubmissions(new ArrayList<>()).build());

        @Test
        void noSubmissionsForSchemeId() {
            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId);

            assertEquals(0, result.getErrorCount());
            assertEquals("OK", result.getErrorStatus());
            assertFalse(result.isErrorFound());
        }

        @Test
        void returnAPIError() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .status(SpotlightSubmissionStatus.SEND_ERROR.toString()).grantScheme(schemeEntity).build();
            spotlightBatches.get(0).getSpotlightSubmissions().add(spotlightSubmission);
            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId);

            assertTrue(result.getErrorCount() > 0);
            assertEquals("API", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void returnGGISError() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .status(SpotlightSubmissionStatus.GGIS_ERROR.toString()).grantScheme(schemeEntity).build();
            spotlightBatches.get(0).getSpotlightSubmissions().add(spotlightSubmission);
            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId);

            assertTrue(result.getErrorCount() > 0);
            assertEquals("GGIS", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void returnValidationError() {
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder()
                    .status(SpotlightSubmissionStatus.VALIDATION_ERROR.toString()).grantScheme(schemeEntity).build();
            spotlightBatches.get(0).getSpotlightSubmissions().add(spotlightSubmission);
            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId);

            assertTrue(result.getErrorCount() > 0);
            assertEquals("VALIDATION", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

    }

    @Nested
    class OrderSpotlightErrorStatusesByPriorityTests {

        final int schemeId1 = 1;

        final int schemeId2 = 2;

        final SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId1).build();

        private SpotlightBatch createSpotlightBatchWithSubmissions(int schemeId,
                SpotlightSubmissionStatus... statuses) {
            final SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId).build();
            final List<SpotlightSubmission> spotlightSubmissions = Arrays.stream(statuses).map(
                    status -> SpotlightSubmission.builder().status(status.toString()).grantScheme(schemeEntity).build())
                    .collect(Collectors.toList());

            return SpotlightBatch.builder().spotlightSubmissions(spotlightSubmissions).build();
        }

        @Test
        void orderSpotlightErrorStatusesByHighestPriority_API() {
            final SpotlightBatch spotlightBatch = createSpotlightBatchWithSubmissions(schemeId1,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.SEND_ERROR,
                    SpotlightSubmissionStatus.VALIDATION_ERROR);
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(1, result.getErrorCount());
            assertEquals("API", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void orderSpotlightErrorStatusesBySecondPriority_GGIS() {
            final SpotlightBatch spotlightBatch = createSpotlightBatchWithSubmissions(schemeId1,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.VALIDATION_ERROR);
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(1, result.getErrorCount());
            assertEquals("GGIS", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void orderSpotlightErrorStatusesByLowestPriority_VALIDATION() {
            final SpotlightBatch spotlightBatch = createSpotlightBatchWithSubmissions(schemeId1,
                    SpotlightSubmissionStatus.VALIDATION_ERROR);
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(1, result.getErrorCount());
            assertEquals("VALIDATION", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void orderSpotlightErrorStatusesByPriorityAndFilterBySchemeId() {
            // Priority - API > GGIS > VALIDATION

            final SpotlightBatch spotlightBatch1 = createSpotlightBatchWithSubmissions(schemeId1,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.GGIS_ERROR,
                    SpotlightSubmissionStatus.VALIDATION_ERROR);

            final SpotlightBatch spotlightBatch2 = createSpotlightBatchWithSubmissions(schemeId2,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.SEND_ERROR);

            final List<SpotlightSubmission> spotlightSubmissions = Stream
                    .concat(spotlightBatch1.getSpotlightSubmissions().stream(),
                            spotlightBatch2.getSpotlightSubmissions().stream())
                    .toList();

            final SpotlightBatch spotlightBatch = SpotlightBatch.builder().spotlightSubmissions(spotlightSubmissions)
                    .build();
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(2, result.getErrorCount());
            assertEquals("GGIS", result.getErrorStatus());
            assertTrue(result.isErrorFound());
        }

        @Test
        void orderSpotlightErrorStatusesByPriorityAndFilterBySchemeId_NoErrors() {
            final SpotlightBatch spotlightBatch = createSpotlightBatchWithSubmissions(schemeId2,
                    SpotlightSubmissionStatus.GGIS_ERROR, SpotlightSubmissionStatus.SEND_ERROR,
                    SpotlightSubmissionStatus.VALIDATION_ERROR);
            final List<SpotlightBatch> spotlightBatches = new ArrayList<>();
            spotlightBatches.add(spotlightBatch);

            when(spotlightBatchRepository.findMostRecentSpotlightBatch(pageable)).thenReturn(spotlightBatches);

            final GetSpotlightBatchErrorCountDTO result = spotlightBatchService.getSpotlightBatchErrorCount(schemeId1);

            assertEquals(0, result.getErrorCount());
            assertEquals("OK", result.getErrorStatus());
            assertFalse(result.isErrorFound());
        }

    }

}
