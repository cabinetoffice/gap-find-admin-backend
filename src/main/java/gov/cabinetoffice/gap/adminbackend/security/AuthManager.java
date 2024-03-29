package gov.cabinetoffice.gap.adminbackend.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.repositories.FundingOrganisationRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@RequiredArgsConstructor
@Component
@Transactional
public class AuthManager implements AuthenticationManager {

    private final GrantAdminRepository grantAdminRepository;

    private final FundingOrganisationRepository fundingOrganisationRepository;

    private final JwtService jwtService;

    @Value("${feature.onelogin.enabled}")
    private boolean oneLoginEnabled;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String authHeader = authentication.getCredentials().toString();
        if (isEmpty(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Expected Authorization header not provided");
        }

        // remove "Bearer " from header
        String jwtBase64 = authHeader.split(" ")[1];

        DecodedJWT decodedJWT = this.jwtService.verifyToken(jwtBase64);

        JwtPayload JWTPayload;
        if (oneLoginEnabled) {
            JWTPayload = this.jwtService.getPayloadFromJwtV2(decodedJWT);

            if (!JWTPayload.getRoles().contains("ADMIN")) {
                throw new AccessDeniedException("User is not an admin");
            }
        }
        else {
            JWTPayload = this.jwtService.getPayloadFromJwt(decodedJWT);
        }

        final List<String> roles = List
                .of(JWTPayload.getRoles().replace("[", "").replace("]", "").replace(" ", "").split(","));

        Optional<GrantAdmin> grantAdmin = this.grantAdminRepository.findByGapUserUserSub(JWTPayload.getSub());

        // if JWT is valid and admin doesn't already exist, create admin user in database
        if (grantAdmin.isEmpty()) {
            grantAdmin = Optional.of(createNewAdmin(JWTPayload));
        }

        AdminSession adminSession = new AdminSession(grantAdmin.get().getId(), grantAdmin.get().getFunder().getId(),
                JWTPayload);

        return new UsernamePasswordAuthenticationToken(adminSession, null,
                roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toCollection(ArrayList::new)));
    }

    private GrantAdmin createNewAdmin(JwtPayload jwtPayload) {

        // check if funding org already exists. if not, create it
        Optional<FundingOrganisation> fundingOrganisation = this.fundingOrganisationRepository
                .findByName(jwtPayload.getDepartmentName());
        if (fundingOrganisation.isEmpty()) {
            fundingOrganisation = Optional.of(this.fundingOrganisationRepository
                    .save(new FundingOrganisation(null, jwtPayload.getDepartmentName())));
        }

        // save new admin to db. This also creates a matching GapUser
        return this.grantAdminRepository
                .save(new GrantAdmin(null, fundingOrganisation.get(), new GapUser(null, jwtPayload.getSub()), new ArrayList<>()));
    }

}
