package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightBatchRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class SpotlightBatchServiceTest {

    private static final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Mock
    private SpotlightBatchRepository spotlightBatchRepository;

    @InjectMocks
    private SpotlightBatchService spotlightBatchService;

    @Nested
    class SpotlightBatchWithStatusExistsTests {

        @Test
        void spotlightBatchWithStatusExists() {
            when(spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt())).thenReturn(true);

            final boolean result = spotlightBatchService.existsByStatusAndMaxBatchSize(SpotlightBatchStatus.QUEUED,
                    200);

            assertTrue(result);
        }

        @Test
        void spotlightBatchWithStatusDoesNotExist() {
            when(spotlightBatchRepository.existsByStatusAndSpotlightSubmissionsSizeLessThan(any(), anyInt())).thenReturn(false);

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
            final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().build();
            final SpotlightBatch existingSpotlightBatch = SpotlightBatch.builder().id(uuid)
                    .spotlightSubmissions(new ArrayList<>()).build();

            when(spotlightBatchRepository.findById(uuid)).thenReturn(Optional.of(existingSpotlightBatch));
            when(spotlightBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            final SpotlightBatch result = spotlightBatchService
                    .addSpotlightSubmissionToSpotlightBatch(spotlightSubmission, uuid);

            assertNotNull(result);
            assertEquals(1, result.getSpotlightSubmissions().size());
            assertTrue(result.getSpotlightSubmissions().contains(spotlightSubmission));
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

}
