package gov.cabinetoffice.gap.adminbackend.utils;

import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomApplicationFormGenerators;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationFormUtilsTest {

    @Test
    void updateAuditDetailsAfterFormChange_UpdatingExpectedAuditDetails() {
        Instant fiveSecondsAgo = Instant.now().minusSeconds(5);
        Integer version = 1;
        ApplicationFormEntity applicationForm = RandomApplicationFormGenerators.randomApplicationFormEntity()
                .lastUpdateBy(007).lastUpdated(fiveSecondsAgo).version(version).build();

        AdminSession session = new AdminSession(1, 1, "Test", "User", "AND Digital", "test.user@and.digital");

        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, session);

        assertThat(applicationForm.getLastUpdated()).isAfter(fiveSecondsAgo);
        assertEquals(session.getGrantAdminId(), applicationForm.getLastUpdateBy());
        assertEquals(Integer.valueOf(2), applicationForm.getVersion());
    }

}
