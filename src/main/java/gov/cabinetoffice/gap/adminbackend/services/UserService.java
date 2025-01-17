package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.client.UserServiceClient;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.UserV2DTO;
import gov.cabinetoffice.gap.adminbackend.dtos.ValidateSessionsRolesRequestBodyDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserDto;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.repositories.FundingOrganisationRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static gov.cabinetoffice.gap.adminbackend.utils.HelperUtils.encryptSecret;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

    public static final String EMPTY_EMAIL_VALUE = "-";

    private final GapUserRepository gapUserRepository;

    private final GrantAdminRepository grantAdminRepository;

    private final GrantApplicantRepository grantApplicantRepository;

    private final FundingOrganisationRepository fundingOrganisationRepository;

    private final UserServiceConfig userServiceConfig;

    private final RestTemplate restTemplate;

    private final WebClient.Builder webClientBuilder;

    private final UserServiceClient userServiceClient;

    private final AwsEncryptionServiceImpl encryptionService;


    @Transactional
    public void migrateUser(final String oneLoginSub, final UUID colaSub) {
        gapUserRepository.findByUserSub(colaSub.toString()).ifPresent(gapUser -> {
            gapUser.setUserSub(oneLoginSub);
            gapUserRepository.save(gapUser);
        });

        grantApplicantRepository.findByUserId(colaSub.toString()).ifPresent(grantApplicant -> {
            grantApplicant.setUserId(oneLoginSub);
            grantApplicantRepository.save(grantApplicant);
        });
    }

    @Transactional
    public void deleteUser(final Optional<String> oneLoginSubOptional, final Optional<UUID> colaSubOptional) {
        // Deleting COLA and OneLogin subs as either could be stored against the user
        oneLoginSubOptional.ifPresent(sub -> {
            grantApplicantRepository.deleteByUserId(sub);
            grantAdminRepository.deleteByGapUserUserSub(sub);
            gapUserRepository.deleteByUserSub(sub);
        });

        if (colaSubOptional.isPresent()) {
            grantApplicantRepository.deleteByUserId(colaSubOptional.get().toString());
            grantAdminRepository.deleteByGapUserUserSub(colaSubOptional.get().toString());
            gapUserRepository.deleteByUserSub(colaSubOptional.get().toString());
        }
    }

    @Transactional
    public void deleteAdminUser(String userSub) {
            grantAdminRepository.deleteByGapUserUserSub(userSub);
            gapUserRepository.deleteByUserSub(userSub);
    }

    public Boolean verifyAdminRoles(final String emailAddress, final String roles) {
        // TODO: after admin-session token handling is aligned with applicant we should
        // use '/is-user-logged-in'
        final String url = userServiceConfig.getDomain() + "/v2/validateSessionsRoles";
        ValidateSessionsRolesRequestBodyDTO requestBody = new ValidateSessionsRolesRequestBodyDTO(emailAddress, roles);

        final HttpEntity<ValidateSessionsRolesRequestBodyDTO> requestEntity = new HttpEntity<>(requestBody);

        Boolean isAdminSessionValid = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Boolean.class)
                .getBody();
        if (isAdminSessionValid == null) {
            throw new UnauthorizedException("Invalid roles");
        }
        return isAdminSessionValid;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public GrantAdmin getGrantAdminIdFromUserServiceEmail(final String email, final String jwt) {
        try {
            //UI might send camelcase. change it to lowercase

            UserV2DTO response = webClientBuilder.build().get()
                    .uri(userServiceConfig.getDomain() + "/user/email/" + email.toLowerCase())
                    .cookie(userServiceConfig.getCookieName(), jwt).retrieve().bodyToMono(UserV2DTO.class).block();

            return grantAdminRepository.findByGapUserUserSub(response.sub()).orElseThrow(() -> new NotFoundException(
                    "No grant admin found for email: " + email));

        } catch (Exception e) {
            throw new NotFoundException(
                    "Something went wrong while retrieving grant admin for email: "
                            + email,
                    e);
        }
    }

    public Optional<GrantAdmin> getGrantAdminIdFromSub(final String sub) {
        return grantAdminRepository.findByGapUserUserSub(sub);
    }

    public Optional<GrantAdmin> getGrantAdminById(final int id) {
        return grantAdminRepository.findById(id);
    }

    public String getDepartmentGGISId(Integer adminId) {
        final GrantAdmin admin = grantAdminRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("No admin found for id: " + adminId));
        final UserDto userDto = userServiceClient.getUserForSub(admin.getGapUser().getUserSub());

        return userDto.getDepartment().getGgisID();
    }

    public void updateFundingOrganisation(GrantAdmin grantAdmin, String departmentName) {
        Optional<FundingOrganisation> fundingOrganisation = this.fundingOrganisationRepository
                .findByName(departmentName);
        if (fundingOrganisation.isEmpty()) {
            FundingOrganisation newFundingOrg = fundingOrganisationRepository
                    .save(new FundingOrganisation(null, departmentName));
            grantAdmin.setFunder(newFundingOrg);
            grantAdminRepository.save(grantAdmin);

            log.info("Created new funding organisation: {}", newFundingOrg);
            log.info("Updated user's funding organisation: {}", grantAdmin.getGapUser());

        } else {
            grantAdmin.setFunder(fundingOrganisation.get());
            grantAdminRepository.save(grantAdmin);
            log.info("Updated user's funding organisation: {}", grantAdmin.getGapUser());

        }
    }

    public byte[] getEmailAddressForSub(final String sub) {
        final String url = userServiceConfig.getDomain() + "/users/emails";
        final ParameterizedTypeReference<List<UserEmailResponseDto>> responseType = new ParameterizedTypeReference<>() {
        };

        final List<UserEmailResponseDto> response = webClientBuilder.build()
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(List.of(sub)))
                .headers(h -> h.set("Authorization", encryptSecret(userServiceConfig.getSecret(),userServiceConfig.getPublicKey())))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    log.error("Unable to get email address for user with sub {}, HTTP status code {}", sub, clientResponse.statusCode());
                    return Mono.empty();
                })
                .bodyToMono(responseType)
                .block();

        return Optional.ofNullable(response)
                .orElse(Collections.emptyList())
                .stream()
                .map(UserEmailResponseDto::emailAddress)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Email not found for user with sub " + sub));
    }
}
