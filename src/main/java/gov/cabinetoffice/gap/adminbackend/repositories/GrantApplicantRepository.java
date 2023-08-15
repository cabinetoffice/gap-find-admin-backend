package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrantApplicantRepository extends JpaRepository<GrantApplicant, Long> {

    Optional<GrantApplicant> findByUserId(String userId);

}
