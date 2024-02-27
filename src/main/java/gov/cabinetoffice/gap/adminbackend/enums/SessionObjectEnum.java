package gov.cabinetoffice.gap.adminbackend.enums;

import gov.cabinetoffice.gap.adminbackend.dtos.application.questions.QuestionSessionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;

public enum SessionObjectEnum {

    newScheme(SchemePostDTO.class), newQuestion(QuestionSessionDTO.class), updatedQuestion(QuestionSessionDTO.class);

    public final Class classType;

    SessionObjectEnum(Class classType) {
        this.classType = classType;
    }

    public Class getClassType() {
        return classType;
    }

}
