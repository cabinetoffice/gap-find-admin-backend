package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationDefinitionDTO;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "grant_application")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationFormEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grant_application_id")
    private Integer grantApplicationId;

    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "grant_scheme_id", nullable = false)
    private SchemeEntity schemeEntity;

    @Column(name = "version")
    private Integer version;

    @Column(name = "created")
    private Instant created;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @Column(name = "last_update_by")
    private Integer lastUpdateBy;

    @Column(name = "last_published")
    private Instant lastPublished;

    @Column(name = "application_name")
    private String applicationName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApplicationStatusEnum applicationStatus;

    @OneToMany(mappedBy = "application")
    @Builder.Default
    @ToString.Exclude
    @JsonBackReference
    private List<Submission> submissions = new ArrayList<>();

    @Column(name = "definition", nullable = false, columnDefinition = "json")
    @Type(type = "json")
    private ApplicationDefinitionDTO definition;

    @OneToMany(mappedBy = "applicationFormEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @JsonBackReference
    private List<GrantExportEntity> grantExportEntities = new ArrayList<>();

    @OneToMany(mappedBy = "applicationFormEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @JsonBackReference
    private List<GrantBeneficiary> grantBeneficiaries = new ArrayList<>();

    public ApplicationFormEntity(SchemeEntity schemeEntity, String applicationName, Integer lastUpdateBy,
            ApplicationDefinitionDTO definition) {
        Instant now = Instant.now();
        this.version = 1;
        this.created = now;
        this.lastUpdated = now;
        this.applicationStatus = ApplicationStatusEnum.DRAFT;
        this.schemeEntity = schemeEntity;
        this.lastUpdateBy = lastUpdateBy;
        this.lastPublished = null;
        this.applicationName = applicationName;
        this.definition = definition;
    }

    public static ApplicationFormEntity createFromTemplate(SchemeEntity schemeEntity, String applicationName,
            Integer lastUpdateBy, ApplicationDefinitionDTO definition) {
        return new ApplicationFormEntity(schemeEntity, applicationName, lastUpdateBy, definition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ApplicationFormEntity that = (ApplicationFormEntity) o;
        return this.grantApplicationId != null && Objects.equals(this.grantApplicationId, that.grantApplicationId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}