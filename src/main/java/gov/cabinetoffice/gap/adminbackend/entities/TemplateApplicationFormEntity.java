package gov.cabinetoffice.gap.adminbackend.entities;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationDefinitionDTO;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "template_grant_application")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class TemplateApplicationFormEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "definition", nullable = false, columnDefinition = "json")
    @Type(type = "json")
    private ApplicationDefinitionDTO definition;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        TemplateApplicationFormEntity that = (TemplateApplicationFormEntity) o;
        return templateId != null && Objects.equals(templateId, that.templateId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}