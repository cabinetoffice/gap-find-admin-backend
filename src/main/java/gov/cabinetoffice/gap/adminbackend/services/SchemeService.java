package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.FeatureFlagsConfigurationProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.mappers.SchemeMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchemeService {

    private final SchemeRepository schemeRepo;

    private final SchemeMapper schemeMapper;

    private final SessionsService sessionsService;

    private final FeatureFlagsConfigurationProperties featureFlagsConfigurationProperties;

    @PreAuthorize("#grantAdminId == authentication.principal.grantAdminId or hasRole('SUPER_ADMIN')")
    public SchemeDTO getSchemeBySchemeId(Integer schemeId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        try {
            SchemeEntity scheme = this.schemeRepo.findById(schemeId).orElseThrow(EntityNotFoundException::new);

            return this.schemeMapper.schemeEntityToDto(scheme);
        }
        catch (EntityNotFoundException | AccessDeniedException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new SchemeEntityException("Something went wrong while retreiving admin " + session.getGrantAdminId()
                    + "'s grant scheme with id: " + schemeId, e);
        }

    }

    public Integer postNewScheme(SchemePostDTO newScheme, HttpSession session) {
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();
        try {
            SchemeEntity entity = this.schemeMapper.schemePostDtoToEntity(newScheme);
            entity.setFunderId(adminSession.getFunderId());
            entity.setCreatedBy(adminSession.getGrantAdminId());
            if (featureFlagsConfigurationProperties.isNewMandatoryQuestionsEnabled()) {
                entity.setVersion(2);
            }
            entity = this.schemeRepo.save(entity);
            this.sessionsService.deleteObjectFromSession(SessionObjectEnum.newScheme, session);

            return entity.getId();
        }
        catch (IllegalArgumentException iae) {
            throw iae;
        }
        catch (Exception e) {
            throw new SchemeEntityException("Something went wrong while creating a new grant scheme.", e);
        }
    }

    public void patchExistingScheme(Integer schemeId, SchemePatchDTO schemePatchDTO) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        try {

            SchemeEntity scheme = this.schemeRepo.findById(schemeId).orElseThrow(EntityNotFoundException::new);

            if (!scheme.getCreatedBy().equals(session.getGrantAdminId())) {
                throw new AccessDeniedException(
                        "User " + session.getGrantAdminId() + "is unable to update the scheme with id " + schemeId);
            }

            scheme.setLastUpdated(Instant.now());
            scheme.setLastUpdatedBy(Integer.valueOf(session.getGrantAdminId()));

            this.schemeMapper.updateSchemeEntityFromPatchDto(schemePatchDTO, scheme);
            this.schemeRepo.save(scheme);
        }
        catch (IllegalArgumentException | EntityNotFoundException | AccessDeniedException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new SchemeEntityException(
                    "Something went wrong while trying to update scheme with the id of: " + schemeId, e);
        }

    }

    public void deleteASchemeById(final Integer schemeId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        try {
            SchemeEntity scheme = this.schemeRepo.findById(schemeId).orElseThrow(EntityNotFoundException::new);

            if (!scheme.getCreatedBy().equals(session.getGrantAdminId())) {
                throw new AccessDeniedException(
                        "User " + session.getGrantAdminId() + "is unable to delete the scheme with id " + schemeId);
            }

            this.schemeRepo.delete(scheme);
        }
        catch (EntityNotFoundException | IllegalArgumentException | AccessDeniedException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new SchemeEntityException(
                    "Something went wrong while trying to delete the scheme with the id of: " + schemeId, e);
        }

    }

    public List<SchemeDTO> getSignedInUsersSchemes() {
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();

        try {
            List<SchemeEntity> schemes;
            schemes = this.schemeRepo.findByCreatedByOrderByCreatedDateDesc(adminSession.getGrantAdminId());
            return this.schemeMapper.schemeEntityListtoDtoList(schemes);
        }
        catch (Exception e) {
            throw new SchemeEntityException("Something went wrong while trying to find all schemes belonging to: "
                    + adminSession.getGrantAdminId(), e);
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<SchemeDTO> getAdminsSchemes(final Integer adminId) {
        final List<SchemeEntity> schemes = this.schemeRepo.findByCreatedBy(adminId);
        return this.schemeMapper.schemeEntityListtoDtoList(schemes);
    }

    public List<SchemeDTO> getPaginatedSchemes(Pageable pagination) {
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();
        try {
            List<SchemeEntity> schemes;
            schemes = this.schemeRepo.findByCreatedByOrderByCreatedDateDesc(adminSession.getGrantAdminId(), pagination);
            return this.schemeMapper.schemeEntityListtoDtoList(schemes);
        }
        catch (Exception e) {
            throw new SchemeEntityException("Something went wrong while trying to find all schemes belonging to: "
                    + adminSession.getGrantAdminId(), e);
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void patchCreatedBy(Integer grantAdminId, Integer schemeId) {
        SchemeEntity scheme = this.schemeRepo.findById(schemeId).orElseThrow(
                () -> new SchemeEntityException("Update grant ownership failed, Something went wrong while trying to find scheme with id: " + schemeId)
        );
        scheme.setCreatedBy(grantAdminId);
        this.schemeRepo.save(scheme);
    }

}
