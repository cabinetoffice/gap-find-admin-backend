package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.SendLambdaExportEmailDTO;
import gov.cabinetoffice.gap.adminbackend.services.GovNotifyService;
import gov.cabinetoffice.gap.adminbackend.services.SecretAuthService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "Emails", description = "API for sending emails via gov notify")
@RestController
@RequiredArgsConstructor
@RequestMapping("/emails")
public class GovNotifyController {

    private final GovNotifyService govNotifyService;

    private final SecretAuthService secretAuthService;

    @PostMapping("/sendLambdaConfirmationEmail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request body",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Email failed to send") })
    public ResponseEntity<String> sendLambdaExportEmail(
            final @RequestBody @Valid SendLambdaExportEmailDTO lambdaExportEmailDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        secretAuthService.authenticateSecret(authHeader);

        if (govNotifyService.sendLambdaExportEmail(lambdaExportEmailDTO))
            return ResponseEntity.ok("Email successfully sent");
        return ResponseEntity.internalServerError().body("Email failed to send, check logs");
    }

}
