package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Feedback")
@RequestMapping("/feedback")
@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping(value = "/add")
    @Operation(summary = "Add user feedback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added feedback",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to add feedback",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json")), })
    public ResponseEntity<String> addFeedback(
            @RequestParam(required = false, defaultValue = "0") final Integer satisfaction,
            @RequestParam(required = false, defaultValue = "") final String comment,
            @RequestParam final String journey) {

        if ((satisfaction != 0 && (1 <= satisfaction && satisfaction <= 5)) || !comment.equals("")) {
            feedbackService.addFeedback(satisfaction, comment, journey);
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.badRequest().build();
        }
    }

}
