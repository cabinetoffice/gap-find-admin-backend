package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class SchemeEditorService {

    final SchemeRepository schemeRepository;
    final GrantAdminRepository grantAdminRepository;

    public void deleteEditor(final Integer schemeId, final Integer editorId) {
        final SchemeEntity scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new NotFoundException("Delete scheme editor: Scheme not found"));

        if (scheme.getCreatedBy().equals(editorId))
            throw new NotFoundException("Delete scheme editor: Cannot delete scheme creator");

        final GrantAdmin grantAdmin = grantAdminRepository.findById(editorId)
                .orElseThrow(() -> new NotFoundException("Delete scheme editor: Grant Admin with editor access not found for this scheme"));

        scheme.removeAdmin(grantAdmin);
        schemeRepository.save(scheme);
    }
}
