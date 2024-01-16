package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.FeedbackEntity;
import gov.cabinetoffice.gap.adminbackend.repositories.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Log4j2
public class FeedbackService {

    private static final int minimumSatisfaction = 1;

    private static final int maximumSatisfaction = 5;

    private final FeedbackRepository feedbackRepository;

    public void addFeedback(int satisfactionScore, String userComment, String userJourney) {
        try {
            FeedbackEntity feedback = FeedbackEntity.builder().satisfaction(satisfactionScore).comment(userComment)
                    .journey(userJourney).created(Instant.now()).build();
            this.feedbackRepository.save(feedback);
        }
        catch (Exception e) {
            log.error("Failed to add feedback: {}", e.getMessage());
        }
    }

}
