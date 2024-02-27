package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailRequestDto;
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

    private String marryEmailFromSub(final List<String> emails, final List<String> userSubs, final GrantAdmin editor) {
        return emails.get(userSubs.indexOf(editor.getGapUser().getUserSub()));
    }

    private List<SchemeEditorsDTO> mapEditorListToDto(List<GrantAdmin> editors, Integer createdBy, String authHeader){
        List<String> userSubs = editors.stream()
                .map(editor -> editor.getGapUser().getUserSub())
                .toList();
        List<String> emails = getEmailsFromUserSubBatch(userSubs, authHeader);

        return editors.stream()
                .map(editor -> {
                    String email = marryEmailFromSub(emails, userSubs, editor);
                    Integer id = editor.getId();
                    SchemeEditorRoleEnum role = id.equals(createdBy)
                            ? SchemeEditorRoleEnum.Owner : SchemeEditorRoleEnum.Editor;

                    return SchemeEditorsDTO.builder().id(id).email(email).role(role).build();
                })
                .sorted(Comparator.comparing(SchemeEditorsDTO::role, Comparator.reverseOrder()))
                .toList();
    }


    private List<String> getEmailsFromUserSubBatch(final List<String> userSubs, final String authHeader) {
        final String url = userServiceConfig.getDomain() + "/user-emails-from-subs";

        UserEmailRequestDto requestBody = UserEmailRequestDto.builder().userSubs(userSubs).build();

        ParameterizedTypeReference<List<UserEmailResponseDto>> responseType =
                new ParameterizedTypeReference<>() {};

                List<UserEmailResponseDto> response = webClientBuilder.build().post()
                .uri(url).body(BodyInserters.fromValue(requestBody))
                .cookie(userServiceConfig.getCookieName(), authHeader).retrieve().bodyToMono(responseType).block();


        return Objects.requireNonNull(response).stream().map((item) ->
                awsEncryptionService.decryptField(item.emailAddress())).toList();
    }

}

