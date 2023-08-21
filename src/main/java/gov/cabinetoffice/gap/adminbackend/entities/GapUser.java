package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "gap_user")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GapUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gap_user_id")
    private Integer id;

    @Column(name = "user_sub")
    private String userSub;

    @OneToOne(mappedBy = "gapUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("gapUsers")
    @ToString.Exclude
    private GrantApplicant grantApplicant;

    @OneToOne(mappedBy = "gapUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("gapUsers")
    @ToString.Exclude
    private GrantAdmin grantAdmin;

}
