package gov.cabinetoffice.gap.adminbackend.dtos.spotlightSubmissions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpotlightMandatoryQuestionDto {

    private UUID id;

    private Integer schemeId;

    private UUID submissionId;

    private String name;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String county;

    private String postcode;

    private String orgType;

    private String companiesHouseNumber;

    private String charityCommissionNumber;

    private BigDecimal fundingAmount;

    private String[] fundingLocation;

    private String status;

    private Integer version;

    private Instant created;

    private long createdBy;

    private String gapId;

}
