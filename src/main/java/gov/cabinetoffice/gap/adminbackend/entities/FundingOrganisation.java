package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grant_funding_organisation")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundingOrganisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "funder_id")
    private Integer id;

    @Column(name = "organisation_name", nullable = false)
    private String name;

    @OneToOne(mappedBy = "fundingOrganisation", orphanRemoval = true)
    @ToString.Exclude
    private GrantAdmin grantAdmin;

    @ToString.Exclude
    @OneToMany(mappedBy = "fundingOrganisation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    @Builder.Default
    private List<SchemeEntity> schemeEntities = new ArrayList<>();

}
