package gov.cabinetoffice.gap.adminbackend.entities;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackEntity {

    @Column(name = "satisfaction")
    private Integer satisfaction;

    @Column(name = "comment")
    private String comment;

}
