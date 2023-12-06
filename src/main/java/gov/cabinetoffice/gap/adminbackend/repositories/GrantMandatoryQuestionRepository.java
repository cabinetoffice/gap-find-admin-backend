package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.GrantMandatoryQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GrantMandatoryQuestionRepository extends JpaRepository<GrantMandatoryQuestions, UUID> {

    @Query("select g " + "from GrantMandatoryQuestions g " + "where g.schemeEntity.id = ?1 "
            + "and g.status = gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus.COMPLETED")
    List<GrantMandatoryQuestions> findBySchemeEntity_IdAndCompletedStatus(Integer id);

    @Query("select g from GrantMandatoryQuestions g " + "where g.schemeEntity.id = ?1 "
            + "and g.status = gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus.COMPLETED "
            + "and g.orgType in (gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.CHARITY, "
            + "gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.REGISTERED_CHARITY, "
            + "gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.UNREGISTERED_CHARITY, "
            + "gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.LIMITED_COMPANY)")
    List<GrantMandatoryQuestions> findCharitiesAndCompaniesBySchemeEntityIdAndCompletedStatus(Integer id);

    @Query("select g from GrantMandatoryQuestions g " + "where g.schemeEntity.id = ?1 "
            + "and g.status = gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus.COMPLETED "
            + "and g.orgType = gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.NON_LIMITED_COMPANY")
    List<GrantMandatoryQuestions> findNonLimitedCompaniesBySchemeEntityIdAndCompletedStatus(Integer id);

    @Query("select (count(g) > 0) from GrantMandatoryQuestions g where g.schemeEntity.id = ?1 "
            + "and g.status = gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus.COMPLETED")
    boolean existsBySchemeEntity_IdAndCompletedStatus(Integer id);

    @Query("select (count(g) > 0) from GrantMandatoryQuestions g " + "where g.schemeEntity.id = ?1 "
            + "and g.status = gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus.COMPLETED "
            + "and g.orgType in (gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.CHARITY, "
            + "gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.REGISTERED_CHARITY, "
            + "gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.UNREGISTERED_CHARITY, "
            + "gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.LIMITED_COMPANY, "
            + "gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType.NON_LIMITED_COMPANY)")
    boolean existsBySchemeEntityIdAndCompleteStatusAndOrgType(Integer id);

}
