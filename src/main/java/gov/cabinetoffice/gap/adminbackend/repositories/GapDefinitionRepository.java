package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GapDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GapDefinitionRepository extends JpaRepository<GapDefinition, Integer> {

    GapDefinition findByName(String name);

}
