package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.UserDTO;
import gov.cabinetoffice.gap.adminbackend.mappers.UserMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserMapper userMapper;

    @GetMapping("/loggedInUser")
    public ResponseEntity<UserDTO> getLoggedInUserDetails() {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        return ResponseEntity.ok(userMapper.adminSessionToUserDTO(session));
    }

}
