package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Slf4j
@Tag(name = "Feedback")
@RequestMapping("/feedback")
@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping(value = "/add")
    @Operation(summary = "Add user feedback")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully added feedback"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to add feedback"),
            @ApiResponse(responseCode = "400", description = "Bad request") })
    public ResponseEntity<String> addFeedback(@RequestParam @NotNull final int satisfaction,
            @RequestParam @NotNull final String comment, @RequestHeader("Authorization") String token) {

        // TODO: Do we need to check the authorization of the user to add the feedback?

        feedbackService.addFeedback(satisfaction, comment);

        return ResponseEntity.ok().build();
    }

}
