package gov.cabinetoffice.gap.adminbackend.dtos.schemes;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SchemeDTO {

    private Integer schemeId;

    private Integer funderId;

    private String name;

    private String ggisReference;

    private String version;

    private Instant createdDate;

    private String contactEmail;

    private Integer createdBy;

}
