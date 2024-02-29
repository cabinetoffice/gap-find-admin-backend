package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.DecryptedUserEmailResponse;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailRequestDto;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import org.springframework.web.reactive.function.BodyInserters;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SchemeEditorRoleEnum;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SchemeEditorService {

    private final SchemeService schemeService;
    private final AwsEncryptionServiceImpl awsEncryptionService;
    private final SchemeRepository schemeRepo;
    private final GrantAdminRepository grantAdminRepository;
    private final UserServiceConfig userServiceConfig;
    private final WebClient.Builder webClientBuilder;

    public Boolean doesAdminOwnScheme(Integer schemeId, Integer adminId) {
        return schemeRepo.existsByIdAndGrantAdminsId(schemeId, adminId);
    }

    public List<SchemeEditorsDTO> getEditorsFromSchemeId(Integer schemeId, String authHeader) {
        SchemeEntity scheme = schemeService.findSchemeById(schemeId);
        List<GrantAdmin> editors = scheme.getGrantAdmins();
        Integer createdBy = scheme.getCreatedBy();

        return this.mapEditorListToDto(editors, createdBy, authHeader);
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

    public void deleteEditor(final Integer schemeId, final Integer editorId) {
        final SchemeEntity scheme = schemeRepo.findById(schemeId)
                .orElseThrow(() -> new NotFoundException("Delete scheme editor: Scheme not found"));

        if (scheme.getCreatedBy().equals(editorId))
            throw new NotFoundException("Delete scheme editor: Cannot delete scheme creator");

        final GrantAdmin grantAdmin = grantAdminRepository.findById(editorId)
                .orElseThrow(() -> new NotFoundException("Delete scheme editor: Grant Admin with editor access not found for this scheme"));

        scheme.removeAdmin(grantAdmin);
        schemeRepo.save(scheme);
    }
}
