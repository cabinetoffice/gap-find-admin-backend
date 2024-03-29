package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrantMandatoryQuestionDto {

    private UUID id;

    private int schemeId;

    private UUID submissionId;

    private String name;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String county;

    private String postcode;

    private String charityCommissionNumber;

    private String companiesHouseNumber;

    private String orgType;

    private String fundingAmount;

    private List<String> fundingLocation;

}
