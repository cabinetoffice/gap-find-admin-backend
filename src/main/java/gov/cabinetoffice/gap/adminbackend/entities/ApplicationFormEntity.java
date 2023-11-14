package gov.cabinetoffice.gap.adminbackend.entities;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationDefinitionDTO;
import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
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

    @Column(name = "grant_scheme_id")
    private Integer grantSchemeId;

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

    @Column(name = "definition", nullable = false, columnDefinition = "json")
    @Type(type = "json")
    private ApplicationDefinitionDTO definition;

    public ApplicationFormEntity(Integer grantSchemeId, String applicationName, Integer lastUpdateBy,
            ApplicationDefinitionDTO definition, int version) {
        Instant now = Instant.now();

        this.version = version;
        this.created = now;
        this.lastUpdated = now;
        this.applicationStatus = ApplicationStatusEnum.DRAFT;
        this.grantSchemeId = grantSchemeId;
        this.lastUpdateBy = lastUpdateBy;
        this.lastPublished = null;
        this.applicationName = applicationName;
        this.definition = definition;
    }

    public static ApplicationFormEntity createFromTemplate(Integer grantSchemeId, String applicationName,
            Integer lastUpdateBy, ApplicationDefinitionDTO definition, int version) {
        return new ApplicationFormEntity(grantSchemeId, applicationName, lastUpdateBy, definition, version);
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