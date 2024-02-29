package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    // This will become a FK to Organisation
    @Column(name = "funder_id", nullable = false)
    private Integer funderId;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created_date", nullable = false)
    @Builder.Default
    private Instant createdDate = Instant.now();

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    // This will become a FK to User
    @Column(name = "last_updated_by")
    private Integer lastUpdatedBy;

    @Column(name = "ggis_identifier", nullable = false)
    private String ggisIdentifier;

    @Column(name = "scheme_name", nullable = false)
    private String name;

    @Column(name = "scheme_contact")
    private String email;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinTable(name = "scheme_editors", joinColumns = {
            @JoinColumn(name = "grant_scheme_id", referencedColumnName = "grant_scheme_id") }, inverseJoinColumns = {
                    @JoinColumn(name = "grant_admin_id", referencedColumnName = "grant_admin_id") })
    @ToString.Exclude
    @JsonManagedReference
    @Builder.Default
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private List<GrantAdmin> grantAdmins = new ArrayList<>();

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

    public void addAdmin(final GrantAdmin admin) {
        this.grantAdmins.add(admin);
    }

    public void removeAdmin(final GrantAdmin admin) {
        this.grantAdmins.remove(admin);
    }
}
