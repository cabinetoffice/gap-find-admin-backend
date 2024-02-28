package gov.cabinetoffice.gap.adminbackend.entities;

import gov.cabinetoffice.gap.adminbackend.enums.GrantExportStatus;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "grant_export_batch")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrantExportBatchEntity {


    @Column(name = "export_batch_id")
    @Id
    private UUID id;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        GrantExportBatchEntity that = (GrantExportBatchEntity) o;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
