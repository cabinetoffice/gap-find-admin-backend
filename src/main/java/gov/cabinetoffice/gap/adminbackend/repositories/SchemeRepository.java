package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemeRepository extends JpaRepository<SchemeEntity, Integer> {

    List<SchemeEntity> findByIdAndCreatedBy(Integer schemeId, Integer grantAdminId);

    List<SchemeEntity> findByGrantAdminsIdOrderByCreatedDateDesc(Integer grantAdminId, Pageable pageable);
    List<SchemeEntity> findByGrantAdminsIdOrderByCreatedDateDesc(Integer grantAdminId);

    // todo co-auth refactor query
    @Query("select s from SchemeEntity s where s.createdBy = ?1")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    List<SchemeEntity> findByCreatedBy(Integer createdBy);

}
