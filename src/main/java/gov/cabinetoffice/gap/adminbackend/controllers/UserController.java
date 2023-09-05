package gov.cabinetoffice.gap.adminbackend.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.dtos.MigrateUserDto;
import gov.cabinetoffice.gap.adminbackend.dtos.UserDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.UserMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static org.springframework.util.ObjectUtils.isEmpty;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Log4j2
public class UserController {

    private final UserMapper userMapper;

    private final JwtService jwtService;

    private final UserService userService;

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
        boolean isV2Payload = adminSession.isV2Payload();
        if (!isV2Payload) {
            return ResponseEntity.ok(Boolean.FALSE);
        }
        String emailAddress = adminSession.getEmailAddress();
        String roles = adminSession.getRoles();
        try {
            userService.verifyAdminRoles(emailAddress, roles);
            return ResponseEntity.ok(Boolean.TRUE);
        }
        catch (UnauthorizedException error) {
            return ResponseEntity.ok(Boolean.FALSE);
        }
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

}
