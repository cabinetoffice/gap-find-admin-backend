package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.entities.FeedbackEntity;
import gov.cabinetoffice.gap.adminbackend.repositories.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig
@WithAdminSession
public class FeedbackServiceTest {

    public static final String instantExpected = "2024-12-22T10:15:30Z";

    public final static Instant SAMPLE_CREATED = Instant.parse(instantExpected);

    final Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));

    final Instant instant = Instant.now(clock);

    public final static String SAMPLE_COMMENT = "I am so satisfied!";

    public final static String DEFAULT_JOURNEY = "advert";

    public final static String APPLICATION_JOURNEY = "application";

    public static final FeedbackEntity SATISFACTION_ONLY_ENTITY = new FeedbackEntity(null, 1, "", SAMPLE_CREATED,
            DEFAULT_JOURNEY);

    public static final FeedbackEntity COMMENT_ONLY_ENTITY = new FeedbackEntity(null, 0, SAMPLE_COMMENT, SAMPLE_CREATED,
            DEFAULT_JOURNEY);

    public static final FeedbackEntity FEEDBACK_ENTITY = new FeedbackEntity(null, 5, SAMPLE_COMMENT, SAMPLE_CREATED,
            DEFAULT_JOURNEY);

    public static final FeedbackEntity APPLICATION_JOURNEY_ENTITY = new FeedbackEntity(null, 5, SAMPLE_COMMENT,
            SAMPLE_CREATED, APPLICATION_JOURNEY);

    public static final FeedbackEntity OUTWITH_LIMIT_ENTITY = new FeedbackEntity(null, 9, SAMPLE_COMMENT,
            SAMPLE_CREATED, DEFAULT_JOURNEY);

    public static final FeedbackEntity EMPTY_COMMENT_ENTITY = new FeedbackEntity(null, 0, "", SAMPLE_CREATED,
            DEFAULT_JOURNEY);

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    public void addFeedback() {
        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(Instant::now).thenReturn(instant);
            this.feedbackService.addFeedback(5, SAMPLE_COMMENT, DEFAULT_JOURNEY);
            verify(this.feedbackRepository).save(FEEDBACK_ENTITY);
        }
    }

    @Test
    public void addFeedbackWithSatisfactionScoreOnly() {
        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(Instant::now).thenReturn(instant);
            this.feedbackService.addFeedback(1, "", DEFAULT_JOURNEY);
            verify(this.feedbackRepository).save(SATISFACTION_ONLY_ENTITY);
        }
    }

    @Test
    public void addFeedbackWithSatisfactionScoreOutwithLimit() {
        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(Instant::now).thenReturn(instant);
            feedbackService.addFeedback(9, null, DEFAULT_JOURNEY);
            verify(this.feedbackRepository).save(OUTWITH_LIMIT_ENTITY);
        }
    }

    @Test
    public void addFeedbackWithCommentOnly() {
        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(Instant::now).thenReturn(instant);
            this.feedbackService.addFeedback(0, SAMPLE_COMMENT, DEFAULT_JOURNEY);
            verify(this.feedbackRepository).save(COMMENT_ONLY_ENTITY);
        }
    }

    @Test
    public void addFeedbackWithNoScoreAndEmptyComment() {
        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(Instant::now).thenReturn(instant);
            this.feedbackService.addFeedback(0, "", DEFAULT_JOURNEY);
            verify(this.feedbackRepository).save(EMPTY_COMMENT_ENTITY);
        }
    }

    @Test
    public void addFeedbackWithApplicationJourney() {
        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(Instant::now).thenReturn(instant);
            this.feedbackService.addFeedback(5, SAMPLE_COMMENT, DEFAULT_JOURNEY);
            verify(this.feedbackRepository).save(APPLICATION_JOURNEY_ENTITY);
        }
    }

}