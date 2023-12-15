package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.SessionValueDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.FieldErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import gov.cabinetoffice.gap.adminbackend.services.SessionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
@Tag(name = "Sessions", description = "API for handling users sessions.")
@Slf4j
public class SessionsController {

    private final SessionsService sessionsService = new SessionsService();

    @PatchMapping("/batch-add")
    @Operation(summary = "Add an object to users session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Object successfully added to session.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Request params 'objectKey' is required.",
                    content = @Content(mediaType = "application/json")), })
    public ResponseEntity<FieldErrorsDTO> batchAddToSession(final @RequestParam @NotNull SessionObjectEnum objectKey,
            final @RequestBody Map<String, String> object, final HttpSession session) {
        final FieldErrorsDTO fieldErrors = sessionsService.validateSessionObject(objectKey, object, session);
        if (fieldErrors.getFieldErrors().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(fieldErrors, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/add")
    @Operation(summary = "Add key-value pair to users session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Key value pair successfully added to session.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Request params 'key' and 'value' are required.",
                    content = @Content(mediaType = "application/json")), })
    public ResponseEntity addToSession(@RequestParam final String key, @RequestParam final String value,
            HttpSession session) {
        log.info("********* SESSION - ADD " + key + " - " + value + " *********");

        FieldErrorsDTO fieldErrors = sessionsService.validateFieldOnDto(key, value, session);
        if (fieldErrors == null) {
            return new ResponseEntity(HttpStatus.OK);
        }
        else {
            return new ResponseEntity(fieldErrors, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get value from users session which matches the given key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Value found for given key.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SessionValueDTO.class)) }),
            @ApiResponse(responseCode = "204", description = "No value found for given key.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "415", description = "Value found but in an unsupported format.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity getFromSession(@PathVariable final String key, HttpSession session) {
        String value;
        try {
            value = (String) session.getAttribute(key);
        }
        catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        if (StringUtils.hasText(value)) {
            return ResponseEntity.ok(new SessionValueDTO(value));
        }
        else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/object/{objectKey}")
    @Operation(summary = "Return an object from a user's session which matches the given key object key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Object found and returned to user",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid object key",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "204", description = "No data found for given key") })
    public ResponseEntity getSessionObject(@PathVariable final @NotNull SessionObjectEnum objectKey,
            HttpSession session) {
        HashMap<String, String> returnObj = sessionsService.retrieveObjectFromSession(objectKey, session);

        if (returnObj.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        else {
            return new ResponseEntity(returnObj, HttpStatus.OK);
        }

    }

    @DeleteMapping("/object/{objectKey}")
    @Operation(summary = "Delete an object from a user's session which matches the given key object key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Object deleted",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Object.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid object key",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No data found for given key",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity deleteSessionObject(@PathVariable final @NotNull SessionObjectEnum objectKey,
            HttpSession session) {
        boolean valuesDeleted = sessionsService.deleteObjectFromSession(objectKey, session);

        if (valuesDeleted) {
            return new ResponseEntity(HttpStatus.OK);
        }
        else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

    }

}
