package gov.cabinetoffice.gap.adminbackend.repositories;

import gov.cabinetoffice.gap.adminbackend.entities.TechSupportUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TechSupportUserRepository extends CrudRepository<TechSupportUser, Integer> {

    void deleteByUserSub(String userSub);

    Optional<TechSupportUser> findByUserSub(String userSub);

}
