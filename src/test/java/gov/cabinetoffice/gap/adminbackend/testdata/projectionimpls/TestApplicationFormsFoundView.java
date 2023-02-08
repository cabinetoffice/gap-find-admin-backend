package gov.cabinetoffice.gap.adminbackend.testdata.projectionimpls;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormsFoundView;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestApplicationFormsFoundView implements ApplicationFormsFoundView {

    private Integer applicationId = 1;

    private Integer inProgressCount = 0;

    private Integer submissionCount = 0;

    @Override
    public Integer getApplicationId() {
        return this.applicationId;
    }

    @Override
    public Integer getInProgressCount() {
        return this.inProgressCount;
    }

    @Override
    public Integer getSubmissionCount() {
        return this.submissionCount;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public void setInProgressCount(Integer inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }

}
