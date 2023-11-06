package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.services.GrantMandatoryQuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

import static gov.cabinetoffice.gap.adminbackend.controllers.SubmissionsController.EXPORT_CONTENT_TYPE;

@Log4j2
@RestController
@RequestMapping("/mandatory-questions")
@Tag(name = "Mandatory questions", description = "API for handling mandatory questions")
@RequiredArgsConstructor
public class GrantMandatoryQuestionsController {
    final private GrantMandatoryQuestionService grantMandatoryQuestionService;

    @GetMapping("/does-scheme-have-completed-mandatory-questions/{schemeId}")
    public ResponseEntity<Boolean> doesSchemeHaveCompletedMandatoryQuestions(@PathVariable Integer schemeId) {
        return ResponseEntity.ok(grantMandatoryQuestionService.doesSchemeHaveCompletedMandatoryQuestions(schemeId));
    }

    @GetMapping(value = "/spotlight-export/{schemeId}", produces = EXPORT_CONTENT_TYPE)
    public ResponseEntity<InputStreamResource> exportSpotlightChecks(@PathVariable Integer schemeId) {
        log.info("Started mandatory questions export for scheme " + schemeId);
        long start = System.currentTimeMillis();

        final ByteArrayOutputStream stream = grantMandatoryQuestionService.exportSpotlightChecks(schemeId);
        final String exportFileName = grantMandatoryQuestionService.generateExportFileName(schemeId);
        final InputStreamResource resource = createTemporaryFile(stream, exportFileName);

        final int length = stream.toByteArray().length;

        // setting HTTP headers to tell caller we are returning a file
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.parse("attachment; filename=" + exportFileName));

        long end = System.currentTimeMillis();
        log.info("Finished mandatory questions export for scheme " + schemeId + ". Export time in millis: "
                + (end - start));

        return ResponseEntity.ok().headers(headers).contentLength(length)
                .contentType(MediaType.parseMediaType(EXPORT_CONTENT_TYPE)).body(resource);
    }

    private InputStreamResource createTemporaryFile(ByteArrayOutputStream stream, String filename) {
        try {
            File tempFile = File.createTempFile(filename, ".xlsx");
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                stream.writeTo(out);
            }
            return new InputStreamResource(new ByteArrayInputStream(stream.toByteArray()));
        }
        catch (Exception e) {
            log.error("Error creating temporary for file {} problem reported {}", filename, e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
