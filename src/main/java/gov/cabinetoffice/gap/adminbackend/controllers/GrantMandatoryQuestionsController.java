package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.services.FileService;
import gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService;
import gov.cabinetoffice.gap.adminbackend.services.SubmissionsService;
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

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;

@Log4j2
@RestController
@RequestMapping("/mandatory-questions")
@Tag(name = "Mandatory questions", description = "API for handling mandatory questions")
@RequiredArgsConstructor
public class GrantMandatoryQuestionsController {

    private final GrantMandatoryQuestionService grantMandatoryQuestionService;

    private final FileService fileService;

    private final SubmissionsService submissionsService;

    @GetMapping("/scheme/{schemeId}/is-completed")
    public ResponseEntity<Boolean> hasCompletedMandatoryQuestions(@PathVariable Integer schemeId,
            @RequestParam boolean isInternal) {
        log.info("Checking if mandatory questions are completed for scheme " + schemeId);

        final Boolean hasCompletedMandatoryQuestions = grantMandatoryQuestionService
                .hasCompletedMandatoryQuestions(schemeId, isInternal);

        log.info("Mandatory questions are completed for scheme " + schemeId + "? : " + hasCompletedMandatoryQuestions);

        return ResponseEntity.ok(hasCompletedMandatoryQuestions);
    }

    @GetMapping(value = "/scheme/{schemeId}/due-diligence", produces = EXPORT_CONTENT_TYPE)
    public ResponseEntity<InputStreamResource> exportDueDiligenceData(@PathVariable Integer schemeId,
            @RequestParam boolean isInternal) {
        final String logMessage = isInternal ? "internal" : "external";
        log.info("Exporting all due diligence data for {} scheme with id {}", logMessage, schemeId);

        final ByteArrayOutputStream stream = grantMandatoryQuestionService.getDueDiligenceData(schemeId, isInternal);
        final String exportFileName = grantMandatoryQuestionService.generateExportFileName(schemeId, null);

        submissionsService.updateLastRequiredChecksExportBySchemeIdAndStatus(schemeId);

        return getInputStreamResourceResponseEntity(schemeId, stream, exportFileName);
    }

    @NotNull
    private ResponseEntity<InputStreamResource> getInputStreamResourceResponseEntity(@PathVariable Integer schemeId,
            ByteArrayOutputStream stream, String exportFileName) {
        log.info("Started due diligence data export for scheme " + schemeId);
        long start = System.currentTimeMillis();

        final InputStreamResource resource = fileService.createTemporaryFile(stream, exportFileName);
        final int length = stream.toByteArray().length;

        // setting HTTP headers to tell caller we are returning a file
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.parse("attachment; filename=" + exportFileName));

        long end = System.currentTimeMillis();
        log.info("Finished due diligence data export for scheme " + schemeId + ". Export time in millis: "
                + (end - start));

        return ResponseEntity.ok().headers(headers).contentLength(length)
                .contentType(MediaType.parseMediaType(EXPORT_CONTENT_TYPE)).body(resource);
    }

}
