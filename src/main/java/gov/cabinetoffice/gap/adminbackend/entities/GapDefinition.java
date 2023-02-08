package gov.cabinetoffice.gap.adminbackend.entities;

import gov.cabinetoffice.gap.adminbackend.models.AdvertDefinition;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "gap_definition")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GapDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gap_definition_id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private AdvertDefinition definition;

}
