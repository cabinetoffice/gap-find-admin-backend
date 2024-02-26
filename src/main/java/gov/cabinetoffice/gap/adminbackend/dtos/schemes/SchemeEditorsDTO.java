package gov.cabinetoffice.gap.adminbackend.dtos.schemes;

import gov.cabinetoffice.gap.adminbackend.enums.SchemeEditorRoleEnum;
import lombok.*;

import lombok.Builder;

@Builder
public record SchemeEditorsDTO(Integer id, String email, SchemeEditorRoleEnum role) {

}