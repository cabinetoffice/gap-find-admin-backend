package gov.cabinetoffice.gap.adminbackend.utils;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.ConflictException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@WithAdminSession
class ApplicationFormUtilsTest {

    private static MockedStatic<HelperUtils> utilMock;

    @BeforeEach
    public void setup() {
       utilMock = mockStatic(HelperUtils.class);
    }

    @AfterEach
    public void close() {
        utilMock.close();
    }


    @Test
    void updateAuditDetailsAfterFormChange_UpdatingExpectedAuditDetails() {

        Instant fiveSecondsAgo = Instant.now().minusSeconds(5);
        Integer revision = 1;
        ApplicationFormEntity applicationForm = RandomApplicationFormGenerators.randomApplicationFormEntity()
                .lastUpdateBy(7).lastUpdated(fiveSecondsAgo).revision(revision).build();

        AdminSession session = new AdminSession(1, 1, "Test", "User", "AND Digital", "test.user@and.digital",
                "[FIND, APPLY, ADMIN]", "UserSub");
        utilMock.when(HelperUtils::getAdminSessionForAuthenticatedUser).thenReturn(session);
        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, false);

        assertThat(applicationForm.getLastUpdated()).isAfter(fiveSecondsAgo);
        assertEquals(session.getGrantAdminId(), applicationForm.getLastUpdateBy());
        assertEquals(Integer.valueOf(2), applicationForm.getRevision());
    }

    @Test
    void updateAuditDetailsAfterFormChange_DoesNotCallSetLastUpdateByWhenIsLambdaEqualsTrue() {
        Instant fiveSecondsAgo = Instant.now().minusSeconds(5);
        Integer revision = 1;
        ApplicationFormEntity applicationForm = Mockito.spy(RandomApplicationFormGenerators
                .randomApplicationFormEntity().lastUpdateBy(7).lastUpdated(fiveSecondsAgo).revision(revision).build());
        Mockito.verify(applicationForm, Mockito.times(0)).setLastUpdateBy(any());
        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, true);

        assertThat(applicationForm.getLastUpdated()).isAfter(fiveSecondsAgo);
        assertEquals(Integer.valueOf(2), applicationForm.getRevision());
    }

    @Test
    void verifyApplicationFormRevision_DoesNotThrowErrorWhenRevisionMatches() {
        Integer revision = 2;
        ApplicationFormEntity applicationForm = RandomApplicationFormGenerators
                .randomApplicationFormEntity()
                .revision(revision)
                .build();

        assertDoesNotThrow(() ->
                ApplicationFormUtils.verifyApplicationFormRevision(revision, applicationForm)
        );
    }

    @Test
    void verifyApplicationFormRevision_ThrowsErrorWhenRevisionDoesNotMatch() {
        Integer revision = 2;
        ApplicationFormEntity applicationForm = RandomApplicationFormGenerators
                .randomApplicationFormEntity()
                .revision(revision)
                .build();

        assertThrows(
                ConflictException.class,
                () -> ApplicationFormUtils.verifyApplicationFormRevision(1, applicationForm)
        );
    }

}
