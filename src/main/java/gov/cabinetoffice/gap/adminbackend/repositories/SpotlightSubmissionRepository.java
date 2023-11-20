package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.SpotlightSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpotlightSubmissionRepository extends JpaRepository<SpotlightSubmission, UUID> {

    List<SpotlightSubmission> findByGrantSchemeIdAndStatus(Integer id, String status);

    long countByGrantSchemeIdAndStatus(Integer id, String status);

}
