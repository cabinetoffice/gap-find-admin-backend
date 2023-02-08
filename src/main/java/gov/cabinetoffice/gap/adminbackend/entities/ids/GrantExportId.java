package gov.cabinetoffice.gap.adminbackend.entities.ids;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class GrantExportId implements Serializable {

    @Column(name = "export_batch_id")
    private UUID exportBatchId;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

}
