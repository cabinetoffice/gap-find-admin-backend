package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.GrantApplicant;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionFundingLocation;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionOrgType;
import gov.cabinetoffice.gap.adminbackend.enums.GrantMandatoryQuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grant_mandatory_questions")
@TypeDef(typeClass = EnumArrayType.class, defaultForType = GrantMandatoryQuestionFundingLocation[].class, parameters = {
        @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE, value = "grant_mandatory_question_funding_location") })
public class GrantMandatoryQuestions extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "grant_scheme_id")
    private SchemeEntity schemeEntity;

    @OneToOne
    @JoinColumn(name = "submission_id", referencedColumnName = "id")
    @JsonIgnoreProperties({ "application", "scheme", "applicant", "createdBy", "lastUpdatedBy", "definition" })
    private Submission submission;

    @Column(name = "name")
    private String name;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "county")
    private String county;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "org_type")
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::grant_mandatory_question_type")
    private GrantMandatoryQuestionOrgType orgType;

    @Column(name = "companies_house_number")
    private String companiesHouseNumber;

    @Column(name = "charity_commission_number")
    private String charityCommissionNumber;

    @Column(name = "funding_amount", precision = 16) // this should match your database
                                                     // column definition
    private BigDecimal fundingAmount;

    @Column(name = "funding_location")
    private GrantMandatoryQuestionFundingLocation[] fundingLocation;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::grant_mandatory_question_status")
    @Builder.Default
    private GrantMandatoryQuestionStatus status = GrantMandatoryQuestionStatus.NOT_STARTED;

    @Builder.Default
    private Integer version = 1;

    @Column(name = "created", nullable = false)
    @Builder.Default
    private Instant created = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @JsonIgnoreProperties({ "submissions", "organisationProfile" })
    private GrantApplicant createdBy;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by", referencedColumnName = "id")
    @JsonIgnoreProperties({ "submissions", "organisationProfile" })
    private GrantApplicant lastUpdatedBy;

    @Column
    private String gapId;

}
