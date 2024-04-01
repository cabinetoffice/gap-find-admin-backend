package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertStatus;
import gov.cabinetoffice.gap.adminbackend.models.GrantAdvertResponse;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grant_advert")
public class GrantAdvert extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "grant_advert_id")
    private UUID id;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    @ToString.Exclude
    @JsonIgnoreProperties({ "hibernateLazyInitializer" })
    private SchemeEntity scheme;

    @Column(name = "version", nullable = false)
    private Integer version;

    @CreatedDate
    @Column(name = "created", nullable = false)
    private Instant created;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "grant_admin_id", nullable = false)
    @ToString.Exclude
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private GrantAdmin createdBy;

    @LastModifiedDate
    @Column(name = "last_updated")
    private Instant lastUpdated;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by", referencedColumnName = "grant_admin_id")
    @ToString.Exclude
    @JsonIgnoreProperties({ "hibernateLazyInitializer" })
    private GrantAdmin lastUpdatedBy;

    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Column(name = "closing_date")
    private LocalDateTime closingDate;

    @Column(name = "first_published_date")
    private Instant firstPublishedDate;

    @Column(name = "last_published_date")
    private Instant lastPublishedDate;

    @Column(name = "unpublished_date")
    private Instant unpublishedDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GrantAdvertStatus status;

    @Column(name = "contentful_entry_id", unique = true)
    private String contentfulEntryId;

    @Column(name = "contentful_slug", unique = true)
    private String contentfulSlug;

    @Column(name = "grant_advert_name")
    private String grantAdvertName;

    @Type(type = "json")
    @Column(name = "response", columnDefinition = "json")
    private GrantAdvertResponse response;

    /**
     * The "lastUpdated" field has historically not being set correctly (it's not being updated with the advert).
     * A recent PR has fixed this, see: https://github.com/cabinetoffice/gap-find-admin-backend/pull/216
     * Advert rows created prior to the PR above have an invalid lastUpdated.
     * Going forward, this field (validLastUpdated) needs to populate with "true" when adverts are created or updated.
     * This field is used by the front-end to conditionally render the lastUpdated field (only when it's valid).
     */
    @Column(name="valid_last_updated", columnDefinition = "boolean default false")
    private boolean validLastUpdated;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        GrantAdvert that = (GrantAdvert) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
