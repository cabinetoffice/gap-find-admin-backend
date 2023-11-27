package gov.cabinetoffice.gap.adminbackend.services;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.dtos.EventLog;
import gov.cabinetoffice.gap.adminbackend.enums.EventType;
import gov.cabinetoffice.gap.adminbackend.enums.ObjectType;
import gov.cabinetoffice.gap.adminbackend.testingextensions.ErrorLogCapture;
import gov.cabinetoffice.gap.adminbackend.testingextensions.ErrorLogCaptureExtension;
import gov.cabinetoffice.gap.adminbackend.testingextensions.InfoLogCapture;
import gov.cabinetoffice.gap.adminbackend.testingextensions.InfoLogCaptureExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@ExtendWith({InfoLogCaptureExtension.class, ErrorLogCaptureExtension.class})
class EventLogServiceTest {

    @Mock
    private AmazonSQS amazonSQS;
    @Mock
    private ObjectMapper objectMapper;

    private EventLogService eventLogService;

    private final Clock clock = Clock.fixed(Instant.parse("2023-01-01T12:00:00.00Z"), ZoneId.systemDefault());

    @Captor
    private ArgumentCaptor<EventLog> eventLogArgumentCaptor;

    @BeforeEach
    void setUp() {
        eventLogService = new EventLogService("eventLogQueue", true, amazonSQS, objectMapper, clock);
    }

    @Nested
    class logAdvertCreatedEvent {
        @Test
        public void success(InfoLogCapture logCapture) throws JsonProcessingException {

            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            when(objectMapper.writeValueAsString(eventLogArgumentCaptor.capture())).thenReturn("");
            when(amazonSQS.sendMessage(anyString(), any())).thenReturn(null);

            eventLogService.logAdvertCreatedEvent(sessionId, userSub, fundingOrgId, objectId);

            EventLog actualEventLog = eventLogArgumentCaptor.getValue();

            assertThat(actualEventLog.getEventType()).isEqualTo(EventType.ADVERT_CREATED);
            assertThat(actualEventLog.getFundingOrganisationId()).isEqualTo(fundingOrgId);
            assertThat(actualEventLog.getSessionId()).isEqualTo(sessionId);
            assertThat(actualEventLog.getUserSub()).isEqualTo(userSub);
            assertThat(actualEventLog.getObjectId()).isEqualTo(objectId);
            assertThat(actualEventLog.getObjectType()).isEqualTo(ObjectType.ADVERT);
            assertThat(actualEventLog.getTimestamp()).isEqualTo(clock.instant());


            assertThat(logCapture.getLoggingEventAt(1).getFormattedMessage())
                    .isEqualTo("Message sent successfully");
        }

        @Test
        public void cantSendToSQS(ErrorLogCapture logCapture) throws JsonProcessingException {
            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            String expectedMessageBody = "MessageBody";
            when(objectMapper.writeValueAsString(any(EventLog.class))).thenReturn(expectedMessageBody);
            when(amazonSQS.sendMessage(anyString(), anyString())).thenThrow(AmazonSQSException.class);

            eventLogService.logAdvertCreatedEvent(sessionId, userSub, fundingOrgId, objectId);

            assertThat(logCapture.getLoggingEventAt(0).getFormattedMessage()).startsWith("Message failed to send for event log");

        }

        @Test
        public void queueDisabled(InfoLogCapture logCapture) {
            eventLogService = new EventLogService("eventLogQueue", false, amazonSQS, objectMapper, clock);
            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            eventLogService.logAdvertCreatedEvent(sessionId, userSub, fundingOrgId, objectId);

            assertThat(logCapture.getLoggingEventAt(0).getFormattedMessage())
                    .isEqualTo("Event Service Queue is disabled. Returning without sending.");
            verifyNoInteractions(amazonSQS, objectMapper);
        }

    }

    @Nested
    class logAdvertUpdatedEvent {
        @Test
        public void success(InfoLogCapture logCapture) throws JsonProcessingException {
            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            when(objectMapper.writeValueAsString(eventLogArgumentCaptor.capture())).thenReturn("");
            when(amazonSQS.sendMessage(anyString(), any())).thenReturn(null);

            eventLogService.logAdvertUpdatedEvent(sessionId, userSub, fundingOrgId, objectId);


            EventLog actualEventLog = eventLogArgumentCaptor.getValue();

            assertThat(actualEventLog.getEventType()).isEqualTo(EventType.ADVERT_UPDATED);
            assertThat(actualEventLog.getFundingOrganisationId()).isEqualTo(fundingOrgId);
            assertThat(actualEventLog.getSessionId()).isEqualTo(sessionId);
            assertThat(actualEventLog.getUserSub()).isEqualTo(userSub);
            assertThat(actualEventLog.getObjectId()).isEqualTo(objectId);
            assertThat(actualEventLog.getObjectType()).isEqualTo(ObjectType.ADVERT);
            assertThat(actualEventLog.getTimestamp()).isEqualTo(clock.instant());

            assertThat(logCapture.getLoggingEventAt(1).getFormattedMessage())
                    .isEqualTo("Message sent successfully");
        }

        @Test
        public void cantSendToSQS(ErrorLogCapture logCapture) throws JsonProcessingException {
            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            String expectedMessageBody = "MessageBody";
            when(objectMapper.writeValueAsString(any(EventLog.class))).thenReturn(expectedMessageBody);
            when(amazonSQS.sendMessage(anyString(), eq(expectedMessageBody))).thenThrow(AmazonSQSException.class);

            eventLogService.logAdvertUpdatedEvent(sessionId, userSub, fundingOrgId, objectId);

            assertThat(logCapture.getLoggingEventAt(0).getFormattedMessage())
                    .startsWith("Message failed to send for event log");
        }

        @Test
        public void queueDisabled(InfoLogCapture logCapture) {
            ReflectionTestUtils.setField(eventLogService, "eventServiceQueueEnabled", false);
            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            eventLogService.logAdvertUpdatedEvent(sessionId, userSub, fundingOrgId, objectId);

            assertThat(logCapture.getLoggingEventAt(0).getFormattedMessage())
                    .isEqualTo("Event Service Queue is disabled. Returning without sending.");

            verifyNoInteractions(amazonSQS, objectMapper);
        }
    }

    @Nested
    class logAdvertPublishedEvent {
        @Test
        public void success(InfoLogCapture logCapture) throws JsonProcessingException {
            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            when(objectMapper.writeValueAsString(eventLogArgumentCaptor.capture())).thenReturn("");
            when(amazonSQS.sendMessage(anyString(), any())).thenReturn(null);

            eventLogService.logAdvertPublishedEvent(sessionId, userSub, fundingOrgId, objectId);

            EventLog actualEventLog = eventLogArgumentCaptor.getValue();

            assertThat(actualEventLog.getEventType()).isEqualTo(EventType.ADVERT_PUBLISHED);
            assertThat(actualEventLog.getFundingOrganisationId()).isEqualTo(fundingOrgId);
            assertThat(actualEventLog.getSessionId()).isEqualTo(sessionId);
            assertThat(actualEventLog.getUserSub()).isEqualTo(userSub);
            assertThat(actualEventLog.getObjectId()).isEqualTo(objectId);
            assertThat(actualEventLog.getObjectType()).isEqualTo(ObjectType.ADVERT);
            assertThat(actualEventLog.getTimestamp()).isEqualTo(clock.instant());

            assertThat(logCapture.getLoggingEventAt(1).getFormattedMessage())
                    .isEqualTo("Message sent successfully");

        }

        @Test
        public void cantSendToSQS(ErrorLogCapture logCapture) throws JsonProcessingException {
            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            String expectedMessageBody = "MessageBody";
            when(objectMapper.writeValueAsString(any(EventLog.class))).thenReturn(expectedMessageBody);
            when(amazonSQS.sendMessage(anyString(), eq(expectedMessageBody))).thenThrow(AmazonSQSException.class);

            eventLogService.logAdvertPublishedEvent(sessionId, userSub, fundingOrgId, objectId);

            assertThat(logCapture.getLoggingEventAt(0).getFormattedMessage()).startsWith("Message failed to send for event log");
        }

        @Test
        public void queueDisabled(InfoLogCapture logCapture) {
            ReflectionTestUtils.setField(eventLogService, "eventServiceQueueEnabled", false);
            String sessionId = "SessionId";
            String userSub = "UserSub";
            long fundingOrgId = 1L;
            String objectId = "ObjectId";

            eventLogService.logAdvertPublishedEvent(sessionId, userSub, fundingOrgId, objectId);

            assertThat(logCapture.getLoggingEventAt(0).getFormattedMessage())
                    .isEqualTo("Event Service Queue is disabled. Returning without sending.");
            verifyNoInteractions(amazonSQS, objectMapper);
        }
    }
}