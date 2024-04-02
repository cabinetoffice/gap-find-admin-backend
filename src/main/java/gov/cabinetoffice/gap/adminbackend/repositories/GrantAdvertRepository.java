package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrantAdvertRepository extends JpaRepository<GrantAdvert, UUID> {

    @Override
    Optional<GrantAdvert> findById(UUID id);

    @Query("select g from GrantAdvert g where g.id = ?1")
    @EntityGraph(attributePaths = {"scheme"})
    Optional<GrantAdvert> findByIdWithScheme(UUID id);

    @PreAuthorize("#grantAdminId == authentication.principal.grantAdminId")
    Long deleteByIdAndCreatedById(UUID advertId, Integer grantAdminId);

    Optional<GrantAdvert> findBySchemeId(Integer schemeId);

    @EntityGraph(attributePaths = {"scheme.grantAdmins"})
    @Transactional
    @Modifying
    @Query("DELETE FROM GrantAdvert g WHERE g.id = :id " +
            "AND EXISTS (SELECT 1 FROM g.scheme.grantAdmins ga WHERE ga.id = :grantAdminId)")
    int deleteByIdAndSchemeEditor(UUID id, Integer grantAdminId);
}
