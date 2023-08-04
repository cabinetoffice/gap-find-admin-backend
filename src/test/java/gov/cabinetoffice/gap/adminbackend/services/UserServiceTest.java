package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationAuditDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.*;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantExportEntity;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import gov.cabinetoffice.gap.adminbackend.exceptions.ApplicationFormException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SpotlightExportException;
import gov.cabinetoffice.gap.adminbackend.mappers.SubmissionMapper;
import gov.cabinetoffice.gap.adminbackend.mappers.SubmissionMapperImpl;
import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantExportRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SubmissionRepository;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomGrantExportEntityGenerator;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSubmissionGenerator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import static gov.cabinetoffice.gap.adminbackend.services.SubmissionsService.*;
import static gov.cabinetoffice.gap.adminbackend.testdata.SubmissionTestData.*;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSubmissionGenerator.randomSubmission;
import static gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSubmissionGenerator.randomSubmissionDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
class UserServiceTest {

    @Spy
    @InjectMocks
    private UserService userService;

    @Mock
    private GapUserRepository gapUserRepository;

    @Mock
    private GrantApplicantRepository grantApplicantRepository;

    private final String oneLoginSub = "oneLoginSub";
    private final UUID colaSub = UUID.randomUUID();

    @Test
    void migrateUserNoMatches() {
        when(gapUserRepository.findByUserSub(any())).thenReturn(Optional.empty());
        when(grantApplicantRepository.findByUserId(any())).thenReturn(Optional.empty());

        userService.migrateUser(oneLoginSub, colaSub);

        verify(gapUserRepository, times(0)).save(any());
        verify(grantApplicantRepository, times(0)).save(any());
    }

    @Test
    void migrateUserMatchesGapUser() {
        final GapUser gapUser = GapUser.builder().build();
        when(gapUserRepository.findByUserSub(any())).thenReturn(Optional.of(gapUser));
        when(grantApplicantRepository.findByUserId(any())).thenReturn(Optional.empty());

        userService.migrateUser(oneLoginSub, colaSub);
        gapUser.setUserSub(oneLoginSub);

        verify(gapUserRepository, times(1)).save(gapUser);
        verify(grantApplicantRepository, times(0)).save(any());
    }

    @Test
    void migrateUserMatchesGrantApplicant() {
        final GrantApplicant grantApplicant = GrantApplicant.builder().build();
        when(grantApplicantRepository.findByUserId(any())).thenReturn(Optional.of(grantApplicant));
        when(gapUserRepository.findByUserSub(any())).thenReturn(Optional.empty());

        userService.migrateUser(oneLoginSub, colaSub);
        grantApplicant.setUserId(oneLoginSub);

        verify(gapUserRepository, times(0)).save(any());
        verify(grantApplicantRepository, times(1)).save(grantApplicant);
    }

    @Test
    void migrateUserMatchesGrantApplicantAndGapUser() {
        final GrantApplicant grantApplicant = GrantApplicant.builder().build();
        final GapUser gapUser = GapUser.builder().build();
        when(grantApplicantRepository.findByUserId(any())).thenReturn(Optional.of(grantApplicant));
        when(gapUserRepository.findByUserSub(any())).thenReturn(Optional.of(gapUser));

        userService.migrateUser(oneLoginSub, colaSub);
        grantApplicant.setUserId(oneLoginSub);
        gapUser.setUserSub(oneLoginSub);

        verify(gapUserRepository, times(1)).save(gapUser);
        verify(grantApplicantRepository, times(1)).save(grantApplicant);
    }
}