package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.FeedbackEntity;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.FeedbackRepository;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public void addFeedback(int satisfactionScore, String userComment, String userJourney) {
        try {
            AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
            FeedbackEntity feedback = FeedbackEntity.builder().satisfaction(satisfactionScore).comment(userComment)
                    .journey(userJourney).created_by(session.getGrantAdminId()).build();
            this.feedbackRepository.save(feedback);
        }
        catch (Exception e) {
            log.error("Failed to add feedback: {}", e.getMessage());
        }
    }

}
