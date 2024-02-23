package gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemeEditorPostDTO {

    @Email(message = "Input a valid email address")
    @NotBlank(message = "Input a valid email address")
    private String editorEmailAddress;
}
