package gov.cabinetoffice.gap.adminbackend.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.ForbiddenException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSessionV2;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayloadV2;
import gov.cabinetoffice.gap.adminbackend.repositories.FundingOrganisationRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

@RequiredArgsConstructor
@Component
@Transactional
public class AuthManagerV2 implements AuthenticationManager {

    private final GrantAdminRepository grantAdminRepository;

    private final FundingOrganisationRepository fundingOrganisationRepository;

    private final JwtService jwtService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String authHeader = authentication.getCredentials().toString();
        if (isEmpty(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Expected Authorization header not provided");
        }

        // remove "Bearer " from header
        String jwtBase64 = authHeader.split(" ")[1];

        DecodedJWT decodedJWT = this.jwtService.verifyToken(jwtBase64);
        JwtPayloadV2 jwtPayloadV2 = this.jwtService.getPayloadFromJwtV2(decodedJWT);
        AdminSessionV2 adminSessionV2 = createAdminSession(jwtPayloadV2);

        return new UsernamePasswordAuthenticationToken(adminSessionV2, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @NotNull
    private AdminSessionV2 createAdminSession(JwtPayloadV2 jwtPayloadV2) {
        Optional<GrantAdmin> grantAdmin = this.grantAdminRepository.findByGapUserUserSub(jwtPayloadV2.getSub());

        if (!jwtPayloadV2.getRoles().contains("ADMIN")) {
            throw new ForbiddenException("User is not an admin");
        }

        // if JWT is valid and admin doesn't already exist, create admin user in database
        if (grantAdmin.isEmpty()) {
            grantAdmin = Optional.of(createNewAdmin(jwtPayloadV2));
        }

        return new AdminSessionV2(grantAdmin.get().getId(), grantAdmin.get().getFunder().getId(), jwtPayloadV2);
    }

    private GrantAdmin createNewAdmin(JwtPayloadV2 jwtPayload) {

        // check if funding org already exists. if not, create it
        Optional<FundingOrganisation> fundingOrganisation = this.fundingOrganisationRepository
                .findByName(jwtPayload.getDepartment());
        if (fundingOrganisation.isEmpty()) {
            fundingOrganisation = Optional.of(
                    this.fundingOrganisationRepository.save(new FundingOrganisation(null, jwtPayload.getDepartment())));
        }

        // save new admin to db. This also creates a matching GapUser
        return this.grantAdminRepository
                .save(new GrantAdmin(null, fundingOrganisation.get(), new GapUser(null, jwtPayload.getSub())));
    }

}
