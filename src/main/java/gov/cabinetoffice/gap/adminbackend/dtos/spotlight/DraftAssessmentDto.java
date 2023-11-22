package gov.cabinetoffice.gap.adminbackend.dtos.spotlight;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DraftAssessmentDto {

    @JsonProperty("OrganisationName")
    private String organisationName;

    @JsonProperty("AddressPostcode")
    private String addressPostcode;

    @JsonProperty("ApplicationAmount")
    private String applicationAmount;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("CityTown")
    private String cityTown;

    @JsonProperty("AddressLine1")
    private String addressLine1;

    @JsonProperty("CharityCommissionRegNo")
    private String charityCommissionRegNo;

    @JsonProperty("CompaniesHouseRegNo")
    private String companiesHouseRegNo;

    @JsonProperty("OrganisationType")
    private String organisationType;

    @JsonProperty("GGISSchemeId")
    private String ggisSchemeId;

    @JsonProperty("FunderID")
    private String funderID;

    @JsonProperty("ApplicationNumber")
    private String applicationNumber;

}
