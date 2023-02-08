package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import java.time.Instant;

import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_GGIS_REFERENCE;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_ORGANISATION_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_SCHEME_CONTACT;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_SCHEME_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_SCHEME_NAME;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_USER_ID;

public class RandomeSchemeGenerator {

    public static SchemeEntity.SchemeEntityBuilder randomSchemeEntity() {
        return SchemeEntity.builder().id(SAMPLE_SCHEME_ID).funderId(SAMPLE_ORGANISATION_ID).version(1)
                .createdDate(Instant.now()).createdBy(SAMPLE_USER_ID).lastUpdated(null).lastUpdatedBy(null)
                .ggisIdentifier(SAMPLE_GGIS_REFERENCE).name(SAMPLE_SCHEME_NAME).email(SAMPLE_SCHEME_CONTACT);
    }

}
