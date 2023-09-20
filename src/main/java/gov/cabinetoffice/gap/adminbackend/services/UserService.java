package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.ValidateSessionsRolesRequestBodyDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GapUserRepository gapUserRepository;

    private final GrantApplicantRepository grantApplicantRepository;

    private final UserServiceConfig userServiceConfig;

    private final RestTemplate restTemplate;

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
        oneLoginSubOptional.ifPresent(grantApplicantRepository::deleteByUserId);
        colaSubOptional.ifPresent(sub -> grantApplicantRepository.deleteByUserId(sub.toString()));
    }

    public Boolean verifyAdminRoles(final String emailAddress, final String roles) {
        // TODO: after admin-session token handling is aligned with applicant we should
        // use '/is-user-logged-in'
        final String url = userServiceConfig.getDomain() + "/v2/validateSessionsRoles";
        ValidateSessionsRolesRequestBodyDTO requestBody = new ValidateSessionsRolesRequestBodyDTO();
        requestBody.setEmailAddress(emailAddress);
        requestBody.setRoles(roles);

        final HttpEntity<ValidateSessionsRolesRequestBodyDTO> requestEntity = new HttpEntity<ValidateSessionsRolesRequestBodyDTO>(
                requestBody);

        Boolean isAdminSessionValid = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Boolean.class)
                .getBody();
        if (isAdminSessionValid == null) {
            throw new UnauthorizedException("Invalid roles");
        }
        return isAdminSessionValid;
    }

}
