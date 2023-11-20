package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.SpotlightPublisherHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.dtos.spotlightBatch.SpotlightBatchDto;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightBatch;
import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import gov.cabinetoffice.gap.adminbackend.enums.SpotlightBatchStatus;
import gov.cabinetoffice.gap.adminbackend.mappers.SpotlightBatchMapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/spotlight-batch")
@Tag(name = "Spotlight Batch", description = "API for handling spotlight batches")
@RequiredArgsConstructor
public class SpotlightBatchController {

    private final SpotlightBatchService spotlightBatchService;

    private final SpotlightSubmissionService spotlightSubmissionService;

    private final SpotlightBatchMapper spotlightBatchMapper;

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
        log.info("Checking if a spotlight batch with status {} exists", status);
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

        final SpotlightSubmission spotlightSubmission = spotlightSubmissionService
            .getSpotlightSubmission(spotlightSubmissionId);

        final SpotlightBatch spotlightBatch = spotlightBatchService
            .addSpotlightSubmissionToSpotlightBatch(spotlightSubmission, spotlightBatchId);
        log.info("Spotlight submission with id {} added to spotlight batch with id {}", spotlightSubmissionId,
                spotlightBatchId);

        spotlightSubmissionService.addSpotlightBatchToSpotlightSubmission(spotlightSubmissionId, spotlightBatch);
        log.info("Spotlight batch with id {} added to spotlight submission with id {}", spotlightBatchId,
                spotlightSubmissionId);

        return ResponseEntity.ok()
            .body(String.format("Spotlight submission with id %s added to spotlight batch with id %s",
                    spotlightSubmissionId, spotlightBatchId));
    }

}
