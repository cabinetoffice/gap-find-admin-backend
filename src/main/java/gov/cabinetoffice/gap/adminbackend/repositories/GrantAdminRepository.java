package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrantAdminRepository extends JpaRepository<GrantAdmin, Integer> {

    Optional<GrantAdmin> findByGapUserUserSub(String sub);

    void deleteByGapUserUserSub(String sub);

}
