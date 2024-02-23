package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.FeatureFlagsConfigurationProperties;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailRequestDto;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SchemeEditorRoleEnum;
import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.mappers.SchemeMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SchemeService {

    private final SchemeRepository schemeRepo;

    private final SchemeMapper schemeMapper;

    private final SessionsService sessionsService;

    private final UserService userService;

    private final GrantAdminRepository grantAdminRepository;

    private final FeatureFlagsConfigurationProperties featureFlagsConfigurationProperties;

    private final RestTemplate restTemplate;

    private final UserServiceConfig userServiceConfig;

    private final AwsEncryptionServiceImpl awsEncryptionService;

    @Value("${user-service.domain}")
    private String userServiceUrl;

    @PostAuthorize("returnObject.createdBy == authentication.principal.grantAdminId or hasRole('SUPER_ADMIN')")
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
                            () -> new SchemeEntityException("Something went wrong while creating a new grant scheme: No grant admin found for id: " + adminSession.getGrantAdminId())
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
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        try {

            SchemeEntity scheme = this.schemeRepo.findById(schemeId).orElseThrow(EntityNotFoundException::new);

            if (!scheme.getCreatedBy().equals(session.getGrantAdminId())) {
                throw new AccessDeniedException(
                        "User " + session.getGrantAdminId() + "is unable to update the scheme with id " + schemeId);
            }

            scheme.setLastUpdated(Instant.now());
            scheme.setLastUpdatedBy(session.getGrantAdminId());

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
    public List<SchemeDTO> getAdminsSchemes(final Integer adminId) {
        final List<SchemeEntity> schemes = this.schemeRepo.findByCreatedBy(adminId);
        return this.schemeMapper.schemeEntityListtoDtoList(schemes);
    }

    private SchemeEntity findSchemeById(Integer schemeId) {
       return this.schemeRepo.findById(schemeId)
                .orElseThrow(() -> new SchemeEntityException(
                        "Update grant ownership failed: Something went wrong while trying to find scheme with id: "
                                + schemeId));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void patchCreatedBy(GrantAdmin grantAdmin, Integer schemeId) {
        SchemeEntity scheme = this.findSchemeById(schemeId);
        scheme.setCreatedBy(grantAdmin.getId());
        scheme.setFunderId(grantAdmin.getFunder().getId());
        this.schemeRepo.save(scheme);
    }


    public Boolean doesAdminOwnScheme(Integer schemeId, Integer adminId) {
        SchemeEntity scheme = this.findSchemeById(schemeId);
        List<SchemeEntity> adminSchemes = this.schemeRepo.findByCreatedBy(adminId);
        if (adminSchemes.contains(scheme)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public List<SchemeEditorsDTO> getEditorsFromSchemeId(Integer schemeId, String authHeader) {
        SchemeEntity scheme = this.findSchemeById(schemeId);
        List<GrantAdmin> editors = scheme.getGrantAdmins();
        Integer createdBy = scheme.getCreatedBy();

        return this.mapEditorListToDto(editors, createdBy, authHeader);
    }

    private List<SchemeEditorsDTO> mapEditorListToDto(List<GrantAdmin> editors, Integer createdBy, String authHeader){
        List<String> userSubs = userService
                .getListOfSubsFromListOfGrantAdminIds(editors.stream().map(GrantAdmin::getId).toList());

        List<String> emails = this.getEmailsFromUserSubBatch(userSubs, authHeader);

        List<Integer> ids = editors.stream().map(GrantAdmin::getId).toList();

        List<SchemeEditorRoleEnum> roles = editors.stream()
                .map(grantAdmin -> grantAdmin.getId().equals(createdBy)
                        ? SchemeEditorRoleEnum.Owner
                        : SchemeEditorRoleEnum.Editor)
                .toList();

        return IntStream.range(0, editors.size())
                .mapToObj(i -> SchemeEditorsDTO.builder()
                        .id(ids.get(i))
                        .email(emails.get(i))
                        .role(roles.get(i))
                        .build())
                .toList();
    }

    private List<String> getEmailsFromUserSubBatch(final List<String> userSubs, final String authHeader) {
        final String url = userServiceUrl + "/user-emails-from-subs";

        HttpHeaders requestHeaders = new HttpHeaders();

        requestHeaders.add("Cookie", userServiceConfig.getCookieName() + "=" + authHeader);


        UserEmailRequestDto requestBody = UserEmailRequestDto.builder().userSubs(userSubs).build();
        HttpEntity<UserEmailRequestDto> httpEntity = new HttpEntity<>(requestBody, requestHeaders);

        ParameterizedTypeReference<List<UserEmailResponseDto>> responseType =
                new ParameterizedTypeReference<List<UserEmailResponseDto>>() {};

        ResponseEntity<List<UserEmailResponseDto>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                responseType
        );

        return Objects.requireNonNull(response.getBody()).stream().map((item) ->
                        awsEncryptionService.decryptField(item.getEmailAddress())).toList();


    }

}
