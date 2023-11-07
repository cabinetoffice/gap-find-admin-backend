package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GrantMandatoryQuestionRepository extends JpaRepository<GrantMandatoryQuestions, UUID> {

    @Query("select g " +
            "from GrantMandatoryQuestions g " +
            "where g.schemeEntity.id = ?1 " +
            "and g.status = gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus.COMPLETED")
    List<GrantMandatoryQuestions> findBySchemeEntity_IdAndCompletedStatus(Integer id);

}
