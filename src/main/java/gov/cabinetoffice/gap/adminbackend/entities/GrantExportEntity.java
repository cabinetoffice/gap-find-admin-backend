package gov.cabinetoffice.gap.adminbackend.entities;

import gov.cabinetoffice.gap.adminbackend.entities.ids.GrantExportId;
import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "grant_export")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrantExportEntity {

    @EmbeddedId
    private GrantExportId id;

    @Column(name = "application_id", nullable = false)
    private Integer applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GrantExportStatus status;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "created")
    @Builder.Default
    private Instant created = Instant.now();

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @Column(name = "location")
    private String location;

    // Not a database column, needed to support SQS message for Submission Export
    @Transient
    private Integer schemeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        GrantExportEntity that = (GrantExportEntity) o;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
