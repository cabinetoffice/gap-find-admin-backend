package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.client.UserServiceClient;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.UserV2DTO;
import gov.cabinetoffice.gap.adminbackend.dtos.ValidateSessionsRolesRequestBodyDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserDto;
import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.repositories.FundingOrganisationRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

    private final GapUserRepository gapUserRepository;

    private final GrantAdminRepository grantAdminRepository;

    private final GrantApplicantRepository grantApplicantRepository;

    private final FundingOrganisationRepository fundingOrganisationRepository;

    private final UserServiceConfig userServiceConfig;

    private final RestTemplate restTemplate;

    private final WebClient.Builder webClientBuilder;

    private final UserServiceClient userServiceClient;

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

        if (colaSubOptional.isPresent()) {
            grantApplicantRepository.deleteByUserId(colaSubOptional.get().toString());
            grantAdminRepository.deleteByGapUserUserSub(colaSubOptional.get().toString());
            gapUserRepository.deleteByUserSub(colaSubOptional.get().toString());
        }
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

    @PreAuthorize("hasRole('ADMIN')")
    public GrantAdmin getGrantAdminIdFromUserServiceEmail(final String email, final String jwt) {
        try {
            UserV2DTO response = webClientBuilder.build().get()
                    .uri(userServiceConfig.getDomain() + "/user/email/" + email + "?role=ADMIN")
                    .cookie(userServiceConfig.getCookieName(), jwt).retrieve().bodyToMono(UserV2DTO.class).block();

            return grantAdminRepository.findByGapUserUserSub(response.sub()).orElseThrow(() -> new NotFoundException(
                    "No grant admin found for email: " + email));

        }
        catch (Exception e) {
            throw new NotFoundException(
                    "Something went wrong while retrieving grant admin for email: "
                            + email,
                    e);
        }
    }

    public Optional<GrantAdmin> getGrantAdminIdFromSub(final String sub) {
        return grantAdminRepository.findByGapUserUserSub(sub);
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

        }
        else {
            grantAdmin.setFunder(fundingOrganisation.get());
            grantAdminRepository.save(grantAdmin);
            log.info("Updated user's funding organisation: {}", grantAdmin.getGapUser());

        }
    }
}
