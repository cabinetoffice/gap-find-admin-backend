package gov.cabinetoffice.gap.adminbackend.dtos.user;

import lombok.Builder;

@Builder
public record SchemeEditorEmailResponse(byte[] emailAddress, String userSub) {

}