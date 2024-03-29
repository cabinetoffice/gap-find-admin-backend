package gov.cabinetoffice.gap.adminbackend.entities;

import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAttachmentStatus;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "grant_attachment")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrantAttachment {

    @Id
    @GeneratedValue
    @Column(name = "grant_attachment_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "submission_id", referencedColumnName = "id")
    private Submission submission;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "created", nullable = false)
    @Builder.Default
    private Instant created = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private GrantApplicant createdBy;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GrantAttachmentStatus status;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "location", nullable = false, columnDefinition = "TEXT")
    private String location;
}
