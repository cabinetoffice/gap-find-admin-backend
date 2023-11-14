package gov.cabinetoffice.gap.adminbackend.testdata;

import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;

public class LoginTestData {

    public final static Integer GAP_USER_ID = 1;

    public final static String GAP_USER_COGNITO_SUBSCRIPTION = "82f9cde7-66b9-42e1-bbeb-07555d6dc6db";

    public final static Integer FUNDING_ORGANISATION_ID = 11;

    public final static String FUNDING_ORGANISATION_NAME = "Test Org";

    public final static Integer GRANT_ADMIN_ID = 111;

    public final static FundingOrganisation FUNDING_ORGANISATION = new FundingOrganisation(FUNDING_ORGANISATION_ID,
            FUNDING_ORGANISATION_NAME);

    public final static GapUser GAP_USER = new GapUser(GAP_USER_ID, GAP_USER_COGNITO_SUBSCRIPTION);

    public final static GrantAdmin GRANT_ADMIN_USER = new GrantAdmin(GRANT_ADMIN_ID, FUNDING_ORGANISATION, GAP_USER);

}
