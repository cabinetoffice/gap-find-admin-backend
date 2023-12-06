package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "spotlight_batch")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpotlightBatch {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "status", nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private SpotlightBatchStatus status = SpotlightBatchStatus.QUEUED;

    @Column(name = "last_send_attempt")
    private Instant lastSendAttempt;

    @Column
    private int version;

    @Column(name = "created", nullable = false)
    @Builder.Default
    private Instant created = Instant.now();

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @ManyToMany
    @JoinTable(name = "spotlight_batch_submission", joinColumns = @JoinColumn(name = "spotlight_batch_id"),
            inverseJoinColumns = @JoinColumn(name = "spotlight_submission_id"))
    @JsonIgnoreProperties("batches")
    @Builder.Default
    private List<SpotlightSubmission> spotlightSubmissions = new ArrayList<>();

}
