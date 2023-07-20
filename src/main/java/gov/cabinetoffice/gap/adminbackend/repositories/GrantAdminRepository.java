package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrantAdminRepository extends JpaRepository<GrantAdmin, Integer> {

    Optional<GrantAdmin> findByGapUserUserSub(String userId);

}
