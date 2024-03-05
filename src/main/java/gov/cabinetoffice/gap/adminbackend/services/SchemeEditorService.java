package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.DecryptedUserEmailResponse;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailRequestDto;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SchemeEditorRoleEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.ForbiddenException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SchemeEditorService {

    private final SchemeService schemeService;
    private final AwsEncryptionServiceImpl awsEncryptionService;
    private final UserService userService;
    private final SchemeRepository schemeRepo;
    private final GrantAdminRepository grantAdminRepository;
    private final UserServiceConfig userServiceConfig;
    private final WebClient.Builder webClientBuilder;

    public Boolean doesAdminOwnScheme(Integer schemeId, Integer adminId) {
        return schemeRepo.existsByIdAndCreatedBy(schemeId, adminId);
    }

    public List<SchemeEditorsDTO> getEditorsFromSchemeId(Integer schemeId, String authHeader) {
        SchemeEditorsDTO dto = SchemeEditorsDTO.builder().id(1).role(SchemeEditorRoleEnum.Owner).email("paul.lawlor@and.digital").build();
        SchemeEditorsDTO dto2 = SchemeEditorsDTO.builder().id(2).role(SchemeEditorRoleEnum.Editor).email("paul.lawlor+2@and.digital").build();
        return List.of(dto, dto2);
//        SchemeEntity scheme = schemeService.findSchemeById(schemeId);
//        List<GrantAdmin> editors = scheme.getGrantAdmins();
//        Integer createdBy = scheme.getCreatedBy();
//
//        return this.mapEditorListToDto(editors, createdBy, authHeader);
    }

    private String getEmailFromUserResponse(final List<DecryptedUserEmailResponse> userResponse,
            final String editorsSub) {
        DecryptedUserEmailResponse editorsRow = userResponse.stream()
                .filter(item -> item.userSub().equals(editorsSub)).findFirst()
                .orElseThrow(() -> new NotFoundException("Could not find users email using sub: " + editorsSub));
        return editorsRow.emailAddress();
    }

    private List<SchemeEditorsDTO> mapEditorListToDto(List<GrantAdmin> editors, Integer createdBy, String authHeader) {
        List<String> userSubs = editors.stream()
                .map(editor -> editor.getGapUser().getUserSub())
                .toList();
        List<DecryptedUserEmailResponse> userResponse = getEmailsFromUserSubBatch(userSubs, authHeader);

        return editors.stream()
                .map(editor -> {
                    String editorsSub = editor.getGapUser().getUserSub();
                    String email = getEmailFromUserResponse(userResponse, editorsSub);
                    Integer id = editor.getId();
                    SchemeEditorRoleEnum role = id.equals(createdBy)
                            ? SchemeEditorRoleEnum.Owner
                            : SchemeEditorRoleEnum.Editor;

                    return SchemeEditorsDTO.builder().id(id).email(email).role(role).build();
                })
                .sorted(Comparator.comparing(SchemeEditorsDTO::role, Comparator.reverseOrder()))
                .toList();
    }

    private List<DecryptedUserEmailResponse> getEmailsFromUserSubBatch(final List<String> userSubs, final String authHeader) {
        final String url = userServiceConfig.getDomain() + "/user-emails-from-subs";

        UserEmailRequestDto requestBody = UserEmailRequestDto.builder().userSubs(userSubs).build();

        ParameterizedTypeReference<List<UserEmailResponseDto>> responseType = new ParameterizedTypeReference<>() {};

        List<UserEmailResponseDto> response = webClientBuilder.build().post()
                .uri(url).body(BodyInserters.fromValue(requestBody))
                .cookie(userServiceConfig.getCookieName(), authHeader).retrieve().bodyToMono(responseType).block();

        return Objects.requireNonNull(response).stream()
                .map((item) -> DecryptedUserEmailResponse.builder().userSub(item.sub()).emailAddress(
                        awsEncryptionService.decryptField(item.emailAddress())).build())
                .toList();
    }

    public SchemeEntity addEditorToScheme(final Integer schemeId, final String editorEmailAddress, final String jwt) {
        SchemeEntity scheme = this.schemeRepo.findById(schemeId).orElseThrow(EntityNotFoundException::new);

        final AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        if (!session.getGrantAdminId().equals(scheme.getCreatedBy()))
            throw new ForbiddenException("Add editor: Only the scheme creator can add editors");

        List<GrantAdmin> existingEditors = scheme.getGrantAdmins();
        GrantAdmin editorToAdd;
        try {
            editorToAdd = userService.getGrantAdminIdFromUserServiceEmail(editorEmailAddress, jwt);
        } catch (NotFoundException e) {
            throw new FieldViolationException("editorEmailAddress", "This account does not have an 'Administrator' account.");
        }

        if (existingEditors.stream().anyMatch(editor -> editor.getId().equals(editorToAdd.getId()))) {
            throw new FieldViolationException("editorEmailAddress", "This email address is already an editor for this scheme");
        }

        scheme.addAdmin(editorToAdd);
        return this.schemeRepo.save(scheme);
    }

    public void deleteEditor(final Integer schemeId, final Integer editorId) {
        final SchemeEntity scheme = schemeRepo.findById(schemeId)
                .orElseThrow(() -> new NotFoundException("Delete scheme editor: Scheme not found"));

        final AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        if (!session.getGrantAdminId().equals(scheme.getCreatedBy()))
            throw new ForbiddenException("Delete scheme editor: Only the scheme creator can delete editors");

        if (scheme.getCreatedBy().equals(editorId))
            throw new ForbiddenException("Delete scheme editor: Cannot delete scheme creator");

        final GrantAdmin grantAdmin = grantAdminRepository.findById(editorId)
                .orElseThrow(() -> new NotFoundException("Delete scheme editor: Grant Admin not found for this scheme"));

        scheme.removeAdmin(grantAdmin);
        schemeRepo.save(scheme);
    }
}
