package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.UserDTO;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    AdminSession userDTOToAdminSession(UserDTO userDTO);

    UserDTO adminSessionToUserDTO(AdminSession adminSession);

}
