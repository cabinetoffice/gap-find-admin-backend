package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrantAdvertRepository extends JpaRepository<GrantAdvert, UUID> {
    @Override
    Optional<GrantAdvert> findById(UUID id);

    @PreAuthorize("#grantAdminId == authentication.principal.grantAdminId")
    Long deleteByIdAndCreatedById(UUID advertId, Integer grantAdminId);

    @PostAuthorize("(!returnObject.isEmpty() ? returnObject.get().createdBy.id == authentication.principal.grantAdminId : true) or hasRole('SUPER_ADMIN')")
    Optional<GrantAdvert> findBySchemeId(Integer schemeId);
}
