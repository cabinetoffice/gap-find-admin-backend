package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.FeedbackDto;
import gov.cabinetoffice.gap.adminbackend.services.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Tag(name = "Feedback")
@RequestMapping("/feedback")
@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping(value = "/add")
    public ResponseEntity<String> addFeedback(@RequestBody FeedbackDto feedbackDto, final HttpServletRequest request,
            @RequestHeader("Authorization") String token) {

        feedbackService.addFeedback(feedbackDto);

        // userService.createGapUser(createAdminUserDto.userSub());
        // GapUser gapUser = userService.getGapUserBySub(createAdminUserDto.userSub());
        // userService.createAdminUser(gapUser);

        return ResponseEntity.ok().build();
    }

}
