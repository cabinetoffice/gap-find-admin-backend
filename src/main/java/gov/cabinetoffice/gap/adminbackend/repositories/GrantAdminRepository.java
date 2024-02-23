package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrantAdminRepository extends JpaRepository<GrantAdmin, Integer> {

    Optional<GrantAdmin> findByGapUserUserSub(String sub);

    Optional<List<GrantAdmin>> findAllByGapUser_IdIn(List<Integer> userIds);

    void deleteByGapUserUserSub(String sub);

}
