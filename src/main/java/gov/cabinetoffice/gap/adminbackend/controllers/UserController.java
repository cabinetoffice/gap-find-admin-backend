package gov.cabinetoffice.gap.adminbackend.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.CheckNewAdminEmailDto;
import gov.cabinetoffice.gap.adminbackend.dtos.MigrateUserDto;
import gov.cabinetoffice.gap.adminbackend.dtos.UserDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.mappers.UserMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        AdminSession adminSession = ((AdminSession) authentication.getPrincipal());
        if (!oneLoginEnabled) {
            return ResponseEntity.ok(Boolean.TRUE);
        }
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

    @DeleteMapping("/delete/{oneLoginSub}")
    public ResponseEntity<String> deleteUser(@PathVariable Optional<String> oneLoginSub,
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

        userService.deleteUser(oneLoginSub, colaSub);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping(value = "/validate-admin-email")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity checkNewAdminEmailIsValid(
            @RequestBody @Valid final CheckNewAdminEmailDto checkNewAdminEmailDto, final HttpServletRequest request) {
        final String jwt = HelperUtils.getJwtFromCookies(request, userServiceConfig.getCookieName());

        if (checkNewAdminEmailDto.getEmailAddress().equals(checkNewAdminEmailDto.getOldEmailAddress())) {
            throw new FieldViolationException("emailAddress", "This user already owns this grant.");
        }

        try {
            userService.getGrantAdminIdFromUserServiceEmail(checkNewAdminEmailDto.getEmailAddress(), jwt);
        }
        catch (Exception e) {
            throw new FieldViolationException("emailAddress", "Email address does not belong to an admin user");
        }
        return ResponseEntity.ok().build();
    }

}
