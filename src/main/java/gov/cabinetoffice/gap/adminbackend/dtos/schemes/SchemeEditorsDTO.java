package gov.cabinetoffice.gap.adminbackend.dtos.schemes;

import gov.cabinetoffice.gap.adminbackend.enums.SchemeEditorRoleEnum;
import lombok.*;

import lombok.Builder;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemeEditorsDTO {
    private Integer id;
    private String email;
    private SchemeEditorRoleEnum role;

    public void setEmail(String email) {
        this.email = email;
    }
}