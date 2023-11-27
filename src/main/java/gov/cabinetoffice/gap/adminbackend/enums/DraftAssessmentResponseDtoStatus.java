package gov.cabinetoffice.gap.adminbackend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DraftAssessmentResponseDtoStatus {
    SUCCESS("Success"), FAILURE("Failure");

    private String name;

    private DraftAssessmentResponseDtoStatus(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.name;
    }

    public static DraftAssessmentResponseDtoStatus valueOfName(String name) {
        for (DraftAssessmentResponseDtoStatus type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    @JsonCreator
    public static DraftAssessmentResponseDtoStatus getDraftAssessmentResponseDtoStatusFromName(String name) {

        return valueOfName(name);

    }
}
