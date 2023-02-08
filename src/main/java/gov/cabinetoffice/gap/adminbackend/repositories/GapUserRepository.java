package gov.cabinetoffice.gap.adminbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import gov.cabinetoffice.gap.adminbackend.entities.GapUser;

public interface GapUserRepository extends JpaRepository<GapUser, Integer> {

}
