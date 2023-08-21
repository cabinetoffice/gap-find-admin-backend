package gov.cabinetoffice.gap.adminbackend.dtos.application;

import gov.cabinetoffice.gap.adminbackend.enums.ApplicationStatusEnum;

import java.time.Instant;

public interface ApplicationFormNoSections {

    Integer getGrantApplicationId();

    Integer getGrantSchemeId();

    Integer getVersion();

    Instant getCreated();

    Integer getCreatedBy();

    Instant getLastUpdated();

    Integer getLastUpdateBy();

    Instant getLastPublished();

    String getApplicationName();

    ApplicationStatusEnum getApplicationStatus();

    Integer setGrantApplicationId(Integer grantApplicationId);

    Integer setGrantSchemeId(Integer grantSchemeId);

    Integer setVersion(Integer version);

    Instant setCreated(Instant created);

    void setCreatedBy(Integer createdBy);

    Instant setLastUpdated(Instant lastUpdated);

    Integer setLastUpdateBy(Integer lastUpdateBy);

    Instant setLastPublished(Instant lastPublished);

    String setApplicationName(String applicationName);

    ApplicationStatusEnum setApplicationStatus(ApplicationStatusEnum applicationStatusEnum);

}
