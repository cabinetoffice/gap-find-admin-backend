package gov.cabinetoffice.gap.adminbackend.testdata;

public class SessionTestData {

    public final static String SESSION_BATCH_ADD_BODY_JSON = """
            {
                "grantName": "sampleSchemeName",
                "ggisReference": "sampleSchemeGGiSReference"
            }""";

    public final static String SESSION_BATCH_ADD_INVALID_BODY_JSON = """
            {
                "grantName": "",
                "ggisReference": ""
            }""";

    public final static String SESSION_VALIDATION_ERRORS_BATCH_ADD_JSON = """
            {
                fieldErrors: [
                    {
                        "fieldName": "grantName",
                        "errorMessage": "Enter the name of your grant"
                    },
                    {
                        "fieldName": "ggisReference",
                        "errorMessage": "Enter your GGIS Scheme Reference Number"
                    }
                ]
            }""";

}
