package gov.cabinetoffice.gap.adminbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import gov.cabinetoffice.gap.adminbackend.entities.GapUser;

import java.util.Optional;

public interface GapUserRepository extends JpaRepository<GapUser, Integer> {

    Optional<GapUser> findByUserSub(String userSub);

    void deleteByUserSub(String userSub);

}
