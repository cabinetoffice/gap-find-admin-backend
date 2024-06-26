package gov.cabinetoffice.gap.adminbackend.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.CheckNewAdminEmailDto;
import gov.cabinetoffice.gap.adminbackend.dtos.MigrateUserDto;
import gov.cabinetoffice.gap.adminbackend.dtos.UpdateFundingOrgDto;
import gov.cabinetoffice.gap.adminbackend.dtos.UserDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.CreateTechSupportUserDto;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.TechSupportUser;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.ForbiddenException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.UserMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import gov.cabinetoffice.gap.adminbackend.services.SchemeService;
import gov.cabinetoffice.gap.adminbackend.services.TechSupportUserService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.util.ObjectUtils.isEmpty;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Log4j2
public class UserController {

    private final UserMapper userMapper;

    private final JwtService jwtService;

    private final UserService userService;

    private final UserServiceConfig userServiceConfig;

    private final TechSupportUserService techSupportUserService;

    private final SchemeService schemeService;

    @Value("${feature.onelogin.enabled}")
    private boolean oneLoginEnabled;

    @GetMapping("/loggedInUser")
    public ResponseEntity<UserDTO> getLoggedInUserDetails() {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        return ResponseEntity.ok(userMapper.adminSessionToUserDTO(session));
    }

    @GetMapping("/validateAdminSession")
    public ResponseEntity<Boolean> validateAdminSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Boolean.FALSE);
        }

        if (!oneLoginEnabled) {
            return ResponseEntity.ok(Boolean.TRUE);
        }

        AdminSession adminSession = ((AdminSession) authentication.getPrincipal());
        String emailAddress = adminSession.getEmailAddress();
        String roles = adminSession.getRoles();

        return ResponseEntity.ok(userService.verifyAdminRoles(emailAddress, roles));
    }

    @PatchMapping("/migrate")
    public ResponseEntity<String> migrateUser(@RequestBody MigrateUserDto migrateUserDto,
            @RequestHeader("Authorization") String token) {
        // Called from our user service only. Does not have an admin session so authing
        // via the jwt
        if (isEmpty(token) || !token.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Migrate user: Expected Authorization header not provided");
        final DecodedJWT decodedJWT = jwtService.verifyToken(token.split(" ")[1]);
        if (!Objects.equals(decodedJWT.getSubject(), migrateUserDto.getOneLoginSub()))
            return ResponseEntity.status(403)
                    .body("User not authorized to migrate user: " + migrateUserDto.getOneLoginSub());

        userService.migrateUser(migrateUserDto.getOneLoginSub(), migrateUserDto.getColaSub());
        return ResponseEntity.ok("User migrated successfully");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam Optional<String> oneLoginSub,
            @RequestParam(required = false) Optional<UUID> colaSub, @RequestHeader("Authorization") String token) {
        // Called from our user service only. Does not have an admin session so authing
        // via the jwt
        if (isEmpty(token) || !token.startsWith("Bearer "))
            return ResponseEntity.status(401).body("Delete user: Expected Authorization header not provided");
        final DecodedJWT decodedJWT = jwtService.verifyToken(token.split(" ")[1]);
        final JwtPayload jwtPayload = this.jwtService.getPayloadFromJwtV2(decodedJWT);
        if (!jwtPayload.getRoles().contains("SUPER_ADMIN")) {
            return ResponseEntity.status(403).body("User not authorized to delete user: " + oneLoginSub);
        }


        String userSub = oneLoginSub.orElseGet(() -> colaSub.map(Object::toString).orElseThrow(() ->
                new IllegalStateException("oneLoginSub and colaSub are not present")));

        schemeService.removeAdminReference(userSub);
        userService.deleteUser(oneLoginSub, colaSub);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PatchMapping("/funding-organisation")
    public ResponseEntity<String> updateFundingOrganisation(@RequestBody UpdateFundingOrgDto updateFundingOrgDto,
            @RequestHeader("Authorization") String token) {

        if (isEmpty(token) || !token.startsWith("Bearer "))
            return ResponseEntity.status(401)
                    .body("Update user's funding organisation: " + "Expected Authorization header not provided");
        final DecodedJWT decodedJWT = jwtService.verifyToken(token.split(" ")[1]);
        final JwtPayload jwtPayload = this.jwtService.getPayloadFromJwtV2(decodedJWT);

        if (!jwtPayload.getRoles().contains("SUPER_ADMIN")) {
            return ResponseEntity.status(403)
                    .body("User not authorized to update user's funding organisation: " + jwtPayload.getSub());
        }

        Optional<TechSupportUser> techSupportUser = techSupportUserService
                .getTechSupportUserBySub(updateFundingOrgDto.sub());
        Optional<GrantAdmin> grantAdmin = userService.getGrantAdminIdFromSub(updateFundingOrgDto.sub());

        grantAdmin.ifPresent(user -> userService.updateFundingOrganisation(user, updateFundingOrgDto.departmentName()));

        techSupportUser.ifPresent(
                user -> techSupportUserService.updateFundingOrganisation(user, updateFundingOrgDto.departmentName()));

        if (grantAdmin.isEmpty() && techSupportUser.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("No grant Admin or tech support user found with sub " + jwtPayload.getSub());

        }

        return ResponseEntity.ok("User's funding organisation updated successfully");
    }

    @PostMapping(value = "/validate-admin-email")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> checkNewAdminEmailIsValid(
            @RequestBody @Valid final CheckNewAdminEmailDto checkNewAdminEmailDto, final HttpServletRequest request) {
        // the email we store comes from One Login, which will always convert the value the user entered to lowercase
        final String newAdminEmail = checkNewAdminEmailDto.getEmailAddress().toLowerCase();
        if (newAdminEmail.equals(checkNewAdminEmailDto.getOldEmailAddress())) {
            throw new FieldViolationException("emailAddress", "This user already owns this grant.");
        }

        try {
            final String jwt = HelperUtils.getJwtFromCookies(request, userServiceConfig.getCookieName());
            userService.getGrantAdminIdFromUserServiceEmail(newAdminEmail, jwt);
        }
        catch (Exception e) {
            throw new FieldViolationException("emailAddress", "Email address does not belong to an admin user");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "tech-support-user")
    public ResponseEntity<String> createTechSupportUser(@RequestBody CreateTechSupportUserDto techSupportUserDto,
            @RequestHeader("Authorization") String token) {
        final String logMessage = String.format("User not authorized to create user: %s", techSupportUserDto.userSub());
        validateToken(token, logMessage);

        techSupportUserService.createTechSupportUser(techSupportUserDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/tech-support-user/{userSub}")
    public ResponseEntity<String> deleteTechSupportUser(@PathVariable String userSub,
            @RequestHeader("Authorization") String token) {
        final String logMessage = String.format("User not authorized to delete user: %s", userSub);
        validateToken(token, logMessage);

        techSupportUserService.deleteTechSupportUser(userSub);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/admin-user/{userSub}")
    @Transactional
    public ResponseEntity<String> removeAdminReference(@PathVariable String userSub,
                                                        @RequestHeader("Authorization") String token) {
        final String logMessage = String.format("User not authorized to remove admin reference: %s", userSub);
        validateToken(token, logMessage);

        schemeService.removeAdminReference(userSub);
        userService.deleteAdminUser(userSub);
        return ResponseEntity.ok().build();
    }

    @Nullable
    private void validateToken(String token, String message) {
        if (isEmpty(token) || !token.startsWith("Bearer "))
            throw new UnauthorizedException("Delete user: Expected Authorization header not provided");

        final DecodedJWT decodedJWT = jwtService.verifyToken(token.split(" ")[1]);
        final JwtPayload jwtPayload = this.jwtService.getPayloadFromJwtV2(decodedJWT);

        if (!jwtPayload.getRoles().contains("SUPER_ADMIN")) {
            throw new ForbiddenException(message);
        }
    }

}
