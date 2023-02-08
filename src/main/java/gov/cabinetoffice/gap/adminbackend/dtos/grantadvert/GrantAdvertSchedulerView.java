package gov.cabinetoffice.gap.adminbackend.dtos.grantadvert;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.cabinetoffice.gap.adminbackend.enums.GrantAdvertSchedulerAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Immutable;

@Immutable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "advert_scheduler_view")
public class GrantAdvertSchedulerView {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private GrantAdvertSchedulerAction action;

}
