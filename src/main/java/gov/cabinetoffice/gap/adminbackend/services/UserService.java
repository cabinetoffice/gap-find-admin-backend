package gov.cabinetoffice.gap.adminbackend.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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

    public Boolean verifyAdminRoles(final String emailAddress, final String roles) {
        final String url = userServiceConfig.getDomain() + "/v2/validateSessionsRoles?emailAddress=" + emailAddress
                + "&roles=" + roles;
        final HttpEntity<String> requestEntity = new HttpEntity<>(null);
        Boolean isAdminSessionValid = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Boolean.class)
                .getBody();
        if(isAdminSessionValid == null) {
            return Boolean.FALSE;
        }
        return isAdminSessionValid;
    }

}
