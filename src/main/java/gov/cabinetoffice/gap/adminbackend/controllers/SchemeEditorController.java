package gov.cabinetoffice.gap.adminbackend.controllers;


import gov.cabinetoffice.gap.adminbackend.services.SchemeEditorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/schemes/{schemeId}/editors")
@Log4j2
public class SchemeEditorController {

    private final SchemeEditorService schemeEditorService;

    @DeleteMapping(value = "/{editorId}")
    public ResponseEntity<String> deleteSchemeEditor(@PathVariable Integer schemeId, @PathVariable Integer editorId) {
        schemeEditorService.deleteEditor(schemeId, editorId);
        return ResponseEntity.ok().build();
    }
}
