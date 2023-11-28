package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.dtos.EventLog;
import gov.cabinetoffice.gap.adminbackend.enums.EventType;
import gov.cabinetoffice.gap.adminbackend.enums.ObjectType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@Service
public class EventLogService {

    private final String eventLogQueue;

    private final boolean eventServiceQueueEnabled;

    private final AmazonSQS amazonSQS;

    private final ObjectMapper objectMapper;

    private final Clock clock;

    public EventLogService(@Value("${cloud.aws.sqs.event-service-queue}") String eventLogQueue,
                           @Value("${cloud.aws.sqs.event-service-queue-enabled}") boolean eventServiceQueueEnabled,
                           AmazonSQS amazonSQS, ObjectMapper objectMapper, Clock clock) {
        this.eventLogQueue = eventLogQueue;
        this.eventServiceQueueEnabled = eventServiceQueueEnabled;
        this.amazonSQS = amazonSQS;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public void logAdvertCreatedEvent(String sessionId, String userSub, long fundingOrganisationId, String objectId) {

        EventLog eventLog = EventLog.builder().objectType(ObjectType.ADVERT).eventType(EventType.ADVERT_CREATED)
                .sessionId(sessionId).userSub(userSub).fundingOrganisationId(fundingOrganisationId).objectId(objectId)
                .timestamp(Instant.now(clock)).build();

        logEvent(eventLog);

    }

    public void logAdvertUpdatedEvent(String sessionId, String userSub, long fundingOrganisationId, String objectId) {

        EventLog eventLog = EventLog.builder().objectType(ObjectType.ADVERT).eventType(EventType.ADVERT_UPDATED)
                .sessionId(sessionId).userSub(userSub).fundingOrganisationId(fundingOrganisationId).objectId(objectId)
                .timestamp(Instant.now(clock)).build();

        logEvent(eventLog);

    }

    public void logAdvertPublishedEvent(String sessionId, String userSub, long fundingOrganisationId, String objectId) {
        EventLog eventLog = EventLog.builder().objectType(ObjectType.ADVERT).eventType(EventType.ADVERT_PUBLISHED)
                .sessionId(sessionId).userSub(userSub).fundingOrganisationId(fundingOrganisationId).objectId(objectId)
                .timestamp(Instant.now(clock)).build();

        logEvent(eventLog);

    }

    private void logEvent(EventLog eventLog) {

        if (!eventServiceQueueEnabled) {
            log.info("Event Service Queue is disabled. Returning without sending.");
            return;
        }

        try {
            log.info("Sending event to {} : {}", eventLogQueue, eventLog);
            amazonSQS.sendMessage(eventLogQueue, objectMapper.writeValueAsString(eventLog));
            log.info("Message sent successfully");
        } catch (Exception e) {
            log.error("Message failed to send for event log " + eventLog, e);
        }

    }

}
