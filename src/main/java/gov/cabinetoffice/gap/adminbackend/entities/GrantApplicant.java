package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table
@Setter
@Getter
public class GrantApplicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(mappedBy = "applicant")
    @JsonBackReference
    private GrantApplicantOrganisationProfile organisationProfile;

    @OneToMany(mappedBy = "createdBy")
    @Builder.Default
    @ToString.Exclude
    @JsonBackReference
    private List<Submission> submissions = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_sub")
    private GapUser gapUser;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    @Builder.Default
    @ToString.Exclude
    private List<GrantBeneficiary> grantBeneficiaries = new ArrayList<>();

}
