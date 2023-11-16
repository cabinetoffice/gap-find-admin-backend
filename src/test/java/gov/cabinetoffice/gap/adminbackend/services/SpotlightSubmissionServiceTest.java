package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
