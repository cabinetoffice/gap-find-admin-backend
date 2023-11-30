package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.SpotlightPublisherHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.GetSpotlightBatchErrorCountDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.SpotlightBatchDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.SpotlightBatchMapper;
import gov.cabinetoffice.gap.adminbackend.services.FileService;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightBatchService;
import gov.cabinetoffice.gap.adminbackend.services.SpotlightSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;

@Log4j2
@RestController
@RequestMapping("/spotlight-batch")
@Tag(name = "Spotlight Batch", description = "API for handling spotlight batches")
@RequiredArgsConstructor
public class SpotlightBatchController {

    private final SpotlightBatchService spotlightBatchService;

    private final SpotlightSubmissionService spotlightSubmissionService;

    private final SpotlightBatchMapper spotlightBatchMapper;

    private final FileService fileService;

    // check spring security whitelist before adding endpoints

    @GetMapping("/status/{status}/exists")
    @Operation(summary = "Check if a spotlight batch with the given status exists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully checked if a spotlight batch with the given status exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "403",
                    description = "Insufficient permissions to check spotlight batch existence",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json")) })
    @SpotlightPublisherHeaderValidator
    public ResponseEntity<Boolean> spotlightBatchWithStatusExist(@PathVariable final SpotlightBatchStatus status,
            @RequestParam(name = "batchSizeLimit", required = false,
                    defaultValue = "200") final String batchSizeLimit) {
        log.info("Checking if a spotlight batch with status {} and max size limit {} exists", status, batchSizeLimit);
        return ResponseEntity.ok()
                .body(spotlightBatchService.existsByStatusAndMaxBatchSize(status, Integer.parseInt(batchSizeLimit)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Retrieve the first spotlight batch found with the given status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully checked if a spotlight batch with the given status exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "Spotlight batch with the given status does not exist",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403",
                    description = "Insufficient permissions to check spotlight batch existence",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json")) })
    @SpotlightPublisherHeaderValidator
    public ResponseEntity<SpotlightBatchDto> retrieveSpotlightBatchWithStatus(
            @PathVariable final SpotlightBatchStatus status, @RequestParam(name = "batchSizeLimit", required = false,
                    defaultValue = "200") final String batchSizeLimit) {
        log.info("Retrieving spotlight batch with status {}", status);

        final SpotlightBatch spotlightBatch = spotlightBatchService.getSpotlightBatchWithStatus(status,
                Integer.parseInt(batchSizeLimit));

        return ResponseEntity.ok().body(spotlightBatchMapper.spotlightBatchToGetSpotlightBatchDto(spotlightBatch));
    }

    @PostMapping()
    @Operation(summary = "Create an entry in to the spotlight batch table ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created a spotlight batch",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "403",
                    description = "Insufficient permissions to created spotlight batch existence",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json")) })
    @SpotlightPublisherHeaderValidator
    public ResponseEntity<SpotlightBatchDto> createSpotlightBatch() {
        log.info("Creating spotlight batch");

        final SpotlightBatch spotlightBatch = spotlightBatchService.createSpotlightBatch();

        return ResponseEntity.ok().body(spotlightBatchMapper.spotlightBatchToGetSpotlightBatchDto(spotlightBatch));
    }

    @PatchMapping("/{spotlightBatchId}/add-spotlight-submission/{spotlightSubmissionId}")
    @Operation(summary = "Create an entry in to the spotlight batch submission table ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created a spotlight batch submission entry",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "403",
                    description = "Insufficient permissions to created spotlight batch existence",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json")) })
    @SpotlightPublisherHeaderValidator
    public ResponseEntity<String> addSpotlightSubmissionToSpotlightBatch(@PathVariable final UUID spotlightBatchId,
            @PathVariable final UUID spotlightSubmissionId) {

        log.info("Adding spotlight submission with id {} to spotlight batch with id {}", spotlightSubmissionId,
                spotlightBatchId);

        spotlightSubmissionService.getSpotlightSubmissionById(spotlightSubmissionId).ifPresent(s -> {
            spotlightBatchService.addSpotlightSubmissionToSpotlightBatch(s, spotlightBatchId);

            log.info("Successfully added spotlight submission with id {} to spotlight batch with id {}",
                    spotlightSubmissionId, spotlightBatchId);
        });

        return ResponseEntity.ok().body("Successfully added spotlight submission to spotlight batch");
    }

    @PostMapping("/send-to-spotlight")
    @Operation(summary = "send queued batches to spotlight")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created the list of dtos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to created Dto",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json")) })
    @SpotlightPublisherHeaderValidator
    public ResponseEntity<String> sendQueuedBatchesAndProcessSpotlightResponse() {
        log.info("Sending queued batches to Spotlight");

        spotlightBatchService.sendQueuedBatchesToSpotlightAndProcessThem();

        log.info("Successfully generated data for Spotlight");

        return ResponseEntity.ok().body("Success");
    }

    @GetMapping("/get-spotlight-scheme-errors/{schemeId}")
    @Operation(summary = "Fetches the highest-priority Spotlight error type and any associated counts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved error type and count",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "No Spotlight errors exist",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to check Spotlight errors",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<GetSpotlightBatchErrorCountDTO> retrieveSpotlightBatchErrorCount(
            @PathVariable final String schemeId) {
        log.info("Retrieving Spotlight errors for scheme {}", schemeId);

        final GetSpotlightBatchErrorCountDTO spotlightBatchErrorCount = spotlightBatchService
                .getSpotlightBatchErrorCount(Integer.parseInt(schemeId));

        return ResponseEntity.ok().body(spotlightBatchErrorCount);
    }

    @GetMapping(value = "/get-validation-error-files/{schemeId}", produces = EXPORT_CONTENT_TYPE)
    public ResponseEntity<InputStreamResource> exportSpotlightValidationErrorFiles(@PathVariable Integer schemeId) {
        final ByteArrayOutputStream stream = spotlightBatchService
                .getFilteredSpotlightSubmissionsWithValidationErrors(schemeId);
        final String exportFileName = "spotlight_validation_errors.zip";
        return getInputStreamResourceResponseEntity(schemeId, stream, exportFileName);
    }

    @NotNull
    private ResponseEntity<InputStreamResource> getInputStreamResourceResponseEntity(@PathVariable Integer schemeId,
            ByteArrayOutputStream stream, String exportFileName) {
        log.info("Started Spotlight validation error file export for scheme " + schemeId);
        long start = System.currentTimeMillis();

        final InputStreamResource resource = fileService.createTemporaryFile(stream, exportFileName);
        final int length = stream.toByteArray().length;

        // setting HTTP headers to tell caller we are returning a file
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.parse("attachment; filename=" + exportFileName));

        long end = System.currentTimeMillis();
        log.info("Finished Spotlight validation error file export for scheme " + schemeId + ". Export time in millis: "
                + (end - start));

        return ResponseEntity.ok().headers(headers).contentLength(length)
                .contentType(MediaType.parseMediaType(EXPORT_CONTENT_TYPE)).body(resource);
    }

}
