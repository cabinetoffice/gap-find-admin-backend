package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
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
@Table(name = "spotlight_submission")
public class SpotlightSubmission extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_mandatory_questions_id", referencedColumnName = "id")
    @ToString.Exclude
    private GrantMandatoryQuestions grantMandatoryQuestions;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_scheme")
    @ToString.Exclude
    private SchemeEntity scheme;

    @Column
    @Enumerated(EnumType.STRING)
    private SpotlightSubmissionStatus status;

    @Column
    private LocalDateTime lastSendAttempt;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        SpotlightSubmission that = (SpotlightSubmission) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
