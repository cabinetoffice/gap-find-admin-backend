package gov.cabinetoffice.gap.adminbackend.dtos;

import gov.cabinetoffice.gap.adminbackend.enums.EventType;
import gov.cabinetoffice.gap.adminbackend.enums.ObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLog {

    String userSub;

    Long fundingOrganisationId;

    String sessionId;

    EventType eventType;

    String objectId;

    ObjectType objectType;

    Instant timestamp;

}
