package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.enums.SubmissionStatus;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grant_submission")
public class Submission extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", referencedColumnName = "id")
    @JsonIgnoreProperties("submissions")
    @ToString.Exclude
    private GrantApplicant applicant;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id")
    @ToString.Exclude
    private SchemeEntity scheme;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    @JsonIgnoreProperties("schemes")
    @ToString.Exclude
    private ApplicationFormEntity application;

    @Column
    private int version;

    @CreatedDate
    private LocalDateTime created;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @JsonIgnoreProperties("submissions")
    @ToString.Exclude
    private GrantApplicant createdBy;

    @LastModifiedDate
    private LocalDateTime lastUpdated;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by", referencedColumnName = "id")
    @JsonIgnoreProperties("submissions")
    @ToString.Exclude
    private GrantApplicant lastUpdatedBy;

    @Column
    private ZonedDateTime submittedDate;

    @Column
    private String applicationName;

    @Column(name = "submission_name")
    private String submissionName;

    @Column
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private SubmissionDefinition definition;

    @Column(name = "gap_id")
    private String gapId;

    @Column(name = "last_required_checks_export")
    private Instant lastRequiredChecksExport;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Submission that = (Submission) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
