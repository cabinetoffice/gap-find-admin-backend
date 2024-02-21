package gov.cabinetoffice.gap.adminbackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.apache.http.conn.scheme.Scheme;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grant_admin")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrantAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grant_admin_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funder_id", referencedColumnName = "funder_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private FundingOrganisation funder;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "gap_user_id")
    private GapUser gapUser;



    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "grantAdmins")
    @ToString.Exclude
    @JsonBackReference
    @Builder.Default
    private List<SchemeEntity> schemes = new ArrayList<>();

}
