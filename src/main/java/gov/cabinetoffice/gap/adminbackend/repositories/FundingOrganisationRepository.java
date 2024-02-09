package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundingOrganisationRepository extends JpaRepository<FundingOrganisation, Integer> {

    Optional<FundingOrganisation> findByName(String name);

}
