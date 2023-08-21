package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "grant_scheme")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grant_scheme_id")
    private Integer id;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created_date", nullable = false)
    @Builder.Default
    private Instant createdDate = Instant.now();

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @Column(name = "ggis_identifier", nullable = false)
    private String ggisIdentifier;

    @Column(name = "scheme_name", nullable = false)
    private String name;

    @Column(name = "scheme_contact")
    private String email;

    @OneToMany(mappedBy = "scheme", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "funder_id")
    @ToString.Exclude
    private FundingOrganisation fundingOrganisation;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    @ToString.Exclude
    private GrantAdmin createdBy;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "last_updated_by", nullable = false)
    @ToString.Exclude
    private GrantAdmin lastUpdatedBy;

    @OneToMany(mappedBy = "schemeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @JsonBackReference
    private List<GrantAdvert> grantAdverts = new ArrayList<>();

    @OneToMany(mappedBy = "schemeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @JsonBackReference
    private List<ApplicationFormEntity> applicationFormEntities = new ArrayList<>();

    @OneToMany(mappedBy = "schemeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @JsonBackReference
    private List<GrantBeneficiary> grantBeneficiaries = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        SchemeEntity that = (SchemeEntity) o;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
