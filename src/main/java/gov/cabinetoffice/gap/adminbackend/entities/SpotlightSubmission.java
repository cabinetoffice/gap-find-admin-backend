package gov.cabinetoffice.gap.adminbackend.entities;

import gov.cabinetoffice.gap.adminbackend.enums.SpotlightSubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "spotlight_submission")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpotlightSubmission {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "grant_mandatory_questions_id")
    private GrantMandatoryQuestions mandatoryQuestions;

    @ManyToOne
    @JoinColumn(name = "grant_scheme")
    private SchemeEntity grantScheme;

    @Builder.Default
    @Column
    private String status = SpotlightSubmissionStatus.QUEUED.toString();

    @Column(name = "last_send_attempt")
    private Instant lastSendAttempt;

    @Column
    private int version;

    @Column(name = "created", nullable = false)
    @Builder.Default
    private Instant created = Instant.now();

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @ManyToMany
    private List<SpotlightBatch> batches = new ArrayList<>();

}
