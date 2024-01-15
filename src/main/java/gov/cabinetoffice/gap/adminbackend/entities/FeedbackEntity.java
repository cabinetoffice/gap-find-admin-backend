package gov.cabinetoffice.gap.adminbackend.entities;

import lombok.*;
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
    private Instant created;

}
