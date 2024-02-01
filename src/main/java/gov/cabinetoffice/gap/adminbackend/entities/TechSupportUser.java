package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;


@Entity
@Table(name = "tech_support_user")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TechSupportUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_sub")
    private String userSub;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "funder_id", referencedColumnName = "funder_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private FundingOrganisation funder;

}
