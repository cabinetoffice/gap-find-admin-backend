package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.services.SchemeEditorService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Tag(name = "Scheme Editors", description = "API for handling scheme editors.")
@RequestMapping("/schemeEditors/{schemeId}")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SchemeEditorController {

    private final UserService userService;
    private final UserServiceConfig userServiceConfig;
    private final SchemeEditorService schemeEditorService;

    @Value("${user-service.domain}")
    private String userServiceDomain;
    @GetMapping("/isOwner")
    public ResponseEntity<Boolean> isSchemeOwner(@PathVariable final Integer schemeId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        try {
            return ResponseEntity.ok().body(schemeEditorService.doesAdminOwnScheme(schemeId, session.getGrantAdminId()));
        } catch(Exception e){
            log.error("Error checking if admin owns scheme", e);
            throw e;
        }
    }

    @GetMapping()
    public ResponseEntity<List<SchemeEditorsDTO>> getSchemeEditors(@PathVariable final Integer schemeId,
                                                                   final HttpServletRequest request) {
        final String jwt = HelperUtils.getJwtFromCookies(request, userServiceConfig.getCookieName());
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        Optional<GrantAdmin> grantAdmin = userService.getGrantAdminIdFromSub(session.getUserSub());
        if (grantAdmin.isEmpty()) {
            throw new UnauthorizedException("User is not a grant admin");
        }
        try {
            List<SchemeEditorsDTO> res = schemeEditorService.getEditorsFromSchemeId(schemeId, jwt);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            log.error("Error getting scheme editors", e);
            throw e;
        }
    }

}
