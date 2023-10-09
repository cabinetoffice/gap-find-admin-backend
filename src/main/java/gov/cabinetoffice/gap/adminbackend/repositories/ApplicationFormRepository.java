package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormNoSections;
import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormsFoundView;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationFormRepository extends JpaRepository<ApplicationFormEntity, Integer> {

    Optional<ApplicationFormNoSections> findByGrantApplicationId(Integer applicationId);

    Optional<List<ApplicationFormNoSections>> findAllByGrantSchemeId(Integer grantSchemeId);

    @Query(nativeQuery = true,
            value = "SELECT q->>'responseType' from grant_application a, "
                    + "json_array_elements(a.definition->'sections') sec, " + "json_array_elements(sec->'questions') q "
                    + "WHERE grant_application_id = :appId " + "AND sec->>'sectionId' = :sectionId "
                    + "AND q->>'questionId' = :questionId")
    Optional<String> determineQuestionResponseType(@Param("appId") Integer applicationId,
            @Param("sectionId") String sectionId, @Param("questionId") String questionId);

    @Query(nativeQuery = true, value = "SELECT a.grant_application_id AS applicationId, "
            + "(SELECT COUNT(*) from grant_submission s WHERE s.application_id = a.grant_application_id AND s.status = 'IN_PROGRESS') AS inProgressCount, "
            + "(SELECT COUNT(*) from grant_submission s WHERE s.application_id = a.grant_application_id AND s.status = 'SUBMITTED') AS submissionCount "
            + "FROM grant_application a WHERE a.created_by = :loggedInUser "
            + "	AND (:applicationId IS NULL OR a.grant_application_id = :applicationId) "
            + "	AND (:applicationName IS NULL OR a.application_name = :applicationName) "
            + "	AND (:grantSchemeId IS NULL OR a.grant_scheme_id = :grantSchemeId) ")
    List<ApplicationFormsFoundView> findMatchingApplicationForm(@Param("loggedInUser") Integer loggedInUserId,
            @Param("applicationId") Integer applicationId, @Param("applicationName") String applicationName,
            @Param("grantSchemeId") Integer grantSchemeId);

}
