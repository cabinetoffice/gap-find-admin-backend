package gov.cabinetoffice.gap.adminbackend.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Id;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "satisfaction")
    private Integer satisfaction;

    @Column(name = "comment")
    private String comment;

    @Column(name = "created")
    @Builder.Default
    private Instant created = Instant.now();

    @Column(name = "journey")
    private String journey;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        FeedbackEntity that = (FeedbackEntity) o;
        return id == null || id.equals(that.id);
    }

}
