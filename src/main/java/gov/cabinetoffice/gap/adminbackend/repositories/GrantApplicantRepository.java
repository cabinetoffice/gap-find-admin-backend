package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantApplicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrantApplicantRepository extends JpaRepository<GrantApplicant, Long> {

    void deleteByGapUserUserSub(String userId);

}
