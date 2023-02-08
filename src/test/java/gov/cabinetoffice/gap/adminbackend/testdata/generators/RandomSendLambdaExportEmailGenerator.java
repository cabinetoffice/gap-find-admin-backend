package gov.cabinetoffice.gap.adminbackend.testdata.generators;

import gov.cabinetoffice.gap.adminbackend.dtos.SendLambdaExportEmailDTO;

import java.util.UUID;

public class RandomSendLambdaExportEmailGenerator {

    public static SendLambdaExportEmailDTO.SendLambdaExportEmailDTOBuilder randomSendLambdaExportEmailGenerator() {
        return SendLambdaExportEmailDTO.builder().exportId(UUID.randomUUID()).submissionId(UUID.randomUUID())
                .emailAddress("test@gmail.com");
    }

}
