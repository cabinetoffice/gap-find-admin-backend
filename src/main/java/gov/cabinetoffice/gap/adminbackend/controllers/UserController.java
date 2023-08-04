package gov.cabinetoffice.gap.adminbackend.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.dtos.MigrateUserDto;
import gov.cabinetoffice.gap.adminbackend.dtos.UserDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.ForbiddenException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.mappers.UserMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.security.AuthManager;
import gov.cabinetoffice.gap.adminbackend.services.JwtService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    @PatchMapping("/migrate")
    public ResponseEntity<String> migrateUser(@RequestBody MigrateUserDto migrateUserDto, @RequestHeader("Authorization") String token) {
        // Called from our user service only. Does not have an admin session so authing via the jwt
        if (isEmpty(token) || !token.startsWith("Bearer "))
            throw new UnauthorizedException("Migrate user: Expected Authorization header not provided");
        final DecodedJWT decodedJWT = jwtService.verifyToken(token.split(" ")[1]);
        if (!Objects.equals(decodedJWT.getSubject(), migrateUserDto.getOneLoginSub()))
            throw new ForbiddenException("User not authorized to migrate user: " + migrateUserDto.getOneLoginSub());

        userService.migrateUser(migrateUserDto.getOneLoginSub(), migrateUserDto.getColaSub());
        return ResponseEntity.ok("User migrated successfully");
    }
}
