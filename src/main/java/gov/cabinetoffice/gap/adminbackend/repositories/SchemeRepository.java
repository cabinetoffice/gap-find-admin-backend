package gov.cabinetoffice.gap.adminbackend.repositories;

import java.util.List;

import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemeRepository extends JpaRepository<SchemeEntity, Integer> {

    List<SchemeEntity> findByCreatedByOrderByCreatedDateDesc(Integer grantAdminId);

    List<SchemeEntity> findByCreatedByOrderByCreatedDateDesc(Integer grantAdminId, Pageable pageable);

}
