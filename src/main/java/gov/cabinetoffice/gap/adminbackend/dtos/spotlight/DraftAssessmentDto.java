package gov.cabinetoffice.gap.adminbackend.dtos.spotlight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DraftAssessmentDto {

    private String OrganisationName;

    private String AddressPostcode;

    private String ApplicationAmount;

    private String Country;

    private String CityTown;

    private String AddressLine1;

    private String CharityCommissionRegNo;

    private String CompaniesHouseRegNo;

    private String OrganisationType;

    private String GGISSchemeId;

    private String FunderID;

    private String ApplicationNumber;

}
