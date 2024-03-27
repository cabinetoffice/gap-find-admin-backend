package gov.cabinetoffice.gap.adminbackend.dtos.schemes;


import java.util.List;

public record OwnedAndEditableSchemesDto(List<SchemeDTO> ownedSchemes, List<SchemeDTO> editableSchemes) {
}
