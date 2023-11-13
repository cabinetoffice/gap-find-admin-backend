package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.repositories.SpotlightSubmissionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class SpotlightSubmissionServiceTest {

    @Mock
    private SpotlightSubmissionRepository spotlightSubmissionRepository;

    @InjectMocks
    private SpotlightSubmissionService spotlightSubmissionService;

    private final Integer SCHEME_ID = 1;

    private final LocalDateTime DATE = LocalDateTime.of(2023, 9, 25, 12, 0);

    private final SpotlightSubmission spotlightSubmission = SpotlightSubmission.builder().id(UUID.randomUUID())
            .lastSendAttempt(DATE).build();

    @Test
    void getSubmissionsByBySchemeIdAndStatus_ReturnsList() {
        when(spotlightSubmissionRepository.findBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                .thenReturn(List.of(spotlightSubmission));

        final List<SpotlightSubmission> result = spotlightSubmissionService
                .getSubmissionsByBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT);

        verify(spotlightSubmissionRepository).findBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT);
        assertThat(result).isEqualTo(List.of(spotlightSubmission));
    }

    @Test
    void getCountBySchemeIdAndStatus_ReturnsCount() {
        when(spotlightSubmissionRepository.countBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                .thenReturn(Long.valueOf(2));

        final long result = spotlightSubmissionService.getCountBySchemeIdAndStatus(SCHEME_ID,
                SpotlightSubmissionStatus.SENT);

        verify(spotlightSubmissionRepository).countBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT);
        assertThat(result).isEqualTo(2);

    }

    @Test
    void getLastSubmissionDate_NoSubmissions() {
        when(spotlightSubmissionRepository.findBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                .thenReturn(List.of());
        final String result = spotlightSubmissionService.getLastSubmissionDate(SCHEME_ID,
                SpotlightSubmissionStatus.SENT);
        assertThat(result).isNull();
    }

    @Test
    void getLastSubmissionDate_HasSubmissions() {
        when(spotlightSubmissionRepository.findBySchemeIdAndStatus(SCHEME_ID, SpotlightSubmissionStatus.SENT))
                .thenReturn(List.of(spotlightSubmission));
        final String result = spotlightSubmissionService.getLastSubmissionDate(SCHEME_ID,
                SpotlightSubmissionStatus.SENT);
        assertThat(result).isEqualTo("25 September 2023");
    }

}