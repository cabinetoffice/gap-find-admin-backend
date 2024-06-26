package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.FeatureFlagsConfigurationProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.mappers.SchemeMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemeService {

    private final SchemeRepository schemeRepo;

    private final SchemeMapper schemeMapper;

    private final SessionsService sessionsService;


    private final GrantAdminRepository grantAdminRepository;

    private final GrantAdvertService grantAdvertService;

    private final ApplicationFormService applicationFormService;

    private final FeatureFlagsConfigurationProperties featureFlagsConfigurationProperties;

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
            throw new SchemeEntityException("Something went wrong while retrieving admin " + session.getGrantAdminId()
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

            this.grantAdminRepository.findById(adminSession.getGrantAdminId())
                    .ifPresentOrElse(
                            entity::addAdmin,
                            () -> new SchemeEntityException("Something went wrong while creating a new grant scheme: " +
                                    "No grant admin found for id: " + adminSession.getGrantAdminId())
                    );

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

        try {
            SchemeEntity scheme = this.schemeRepo.findById(schemeId).orElseThrow(EntityNotFoundException::new);

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

        try {
            SchemeEntity scheme = this.schemeRepo.findById(schemeId).orElseThrow(EntityNotFoundException::new);
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
            List<SchemeEntity> schemes = this.schemeRepo.findByGrantAdminsIdOrderByCreatedDateDesc(adminSession.getGrantAdminId());
            return this.schemeMapper.schemeEntityListtoDtoList(schemes);
        }
        catch (Exception e) {
            throw new SchemeEntityException("Something went wrong while trying to find all schemes belonging to: "
                    + adminSession.getGrantAdminId(), e);
        }
    }

    public List<SchemeDTO> getPaginatedSchemes(Pageable pagination) {
        AdminSession adminSession = HelperUtils.getAdminSessionForAuthenticatedUser();
        try {
            List<SchemeEntity> schemes = this.schemeRepo
                    .findByGrantAdminsIdOrderByCreatedDateDesc(adminSession.getGrantAdminId(), pagination);
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

    public SchemeEntity findSchemeById(Integer schemeId) {
       return this.schemeRepo.findById(schemeId)
                .orElseThrow(() -> new SchemeEntityException(
                        "Update grant ownership failed: Something went wrong while trying to find scheme with id: "
                                + schemeId));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void updateGrantSchemeOwner(GrantAdmin grantAdmin, Integer schemeId) {
        final SchemeEntity scheme = this.findSchemeById(schemeId);
        scheme.getGrantAdmins().stream()
                .filter(admin -> admin.getId().equals(scheme.getCreatedBy()))
                .findAny()
                .ifPresent(scheme::removeAdmin);

        scheme.setCreatedBy(grantAdmin.getId());
        scheme.setFunderId(grantAdmin.getFunder().getId());

        scheme.getGrantAdmins().stream()
                .filter(admin -> admin.getId().equals(grantAdmin.getId()))
                .findAny()
                .ifPresentOrElse(
                        admin -> log.info("Admin with ID {} is already an editor on scheme {}", admin.getId(), scheme.getId()),
                        () -> scheme.addAdmin(grantAdmin)
                );

        this.schemeRepo.save(scheme);
    }

    @Transactional
    public void removeAdminReference(String userSub) {
        grantAdminRepository.findByGapUserUserSub(userSub).ifPresent(grantAdmin -> {

            List<SchemeEntity> schemes = schemeRepo.findByGrantAdminsIdOrderByCreatedDateDesc(grantAdmin.getId());

            for (SchemeEntity scheme: schemes) {

                if (scheme.getLastUpdatedBy() != null && scheme.getLastUpdatedBy().equals(grantAdmin.getId())) {
                    scheme.setLastUpdatedBy(null);
                }

                scheme.removeAdmin(grantAdmin);
            }

            grantAdvertService.removeAdminReferenceBySchemeId(grantAdmin);
            applicationFormService.removeAdminReferenceBySchemeId(grantAdmin);
            schemeRepo.saveAll(schemes);
        });

    }


    public List<SchemeDTO> getPaginatedOwnedSchemesByAdminId(int adminId, Pageable pagination) {
        final List<SchemeEntity> schemes = this.schemeRepo
                    .findByCreatedByOrderByLastUpdatedDescCreatedDateDesc(adminId, pagination);

        return this.schemeMapper.schemeEntityListtoDtoList(schemes);
    }

    public List<SchemeDTO> getOwnedSchemesByAdminId(int adminId) {
        final List<SchemeEntity> schemes = this.schemeRepo
                .findByCreatedByOrderByLastUpdatedDescCreatedDateDesc(adminId);

        return this.schemeMapper.schemeEntityListtoDtoList(schemes);
    }

    public List<SchemeDTO> getPaginatedEditableSchemesByAdminId(int adminId, Pageable pagination) {
        final List<SchemeEntity> schemes = this.schemeRepo
                .findByCreatedByNotAndGrantAdminsIdOrderByLastUpdatedDescCreatedDateDesc(adminId, adminId, pagination);

        return this.schemeMapper.schemeEntityListtoDtoList(schemes);
    }

    public List<SchemeDTO> getEditableSchemesByAdminId(int adminId) {
        final List<SchemeEntity> schemes = this.schemeRepo
                .findByCreatedByNotAndGrantAdminsIdOrderByLastUpdatedDescCreatedDateDesc(adminId, adminId);

        return this.schemeMapper.schemeEntityListtoDtoList(schemes);
    }
}
