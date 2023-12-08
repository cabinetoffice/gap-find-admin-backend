package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.services.FileService;
import gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;

@Log4j2
@RestController
@RequestMapping("/mandatory-questions")
@Tag(name = "Mandatory questions", description = "API for handling mandatory questions")
@RequiredArgsConstructor
public class GrantMandatoryQuestionsController {

    final private GrantMandatoryQuestionService grantMandatoryQuestionService;

    final private FileService fileService;

    @GetMapping("/scheme/{schemeId}/isCompleted")
    public ResponseEntity<Boolean> hasCompletedMandatoryQuestions(@PathVariable Integer schemeId,
            @RequestParam boolean isInternal) {
        return ResponseEntity.ok(grantMandatoryQuestionService.hasCompletedMandatoryQuestions(schemeId, isInternal));
    }

    @GetMapping(value = "/due-diligence/{schemeId}", produces = EXPORT_CONTENT_TYPE)
    public ResponseEntity<InputStreamResource> exportDueDiligenceData(@PathVariable Integer schemeId,
            @RequestParam(name = "internal") boolean isInternal) {
        final ByteArrayOutputStream stream = grantMandatoryQuestionService.getDueDiligenceData(schemeId, isInternal);
        final String exportFileName = grantMandatoryQuestionService.generateExportFileName(schemeId, null);
        return getInputStreamResourceResponseEntity(schemeId, stream, exportFileName);
    }

    @GetMapping(value = "/spotlight-export/{schemeId}", produces = EXPORT_CONTENT_TYPE)
    public ResponseEntity<InputStreamResource> exportSpotlightChecks(@PathVariable Integer schemeId) {
        final ByteArrayOutputStream stream = grantMandatoryQuestionService.getSpotlightChecks(schemeId);
        final String exportFileName = "spotlight_checks.zip";
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
