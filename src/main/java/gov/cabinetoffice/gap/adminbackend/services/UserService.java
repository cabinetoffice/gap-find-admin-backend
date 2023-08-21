package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.repositories.GapUserRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GapUserRepository gapUserRepository;

    private final GrantApplicantRepository grantApplicantRepository;

    @Transactional
    public void migrateUser(final String oneLoginSub, final UUID colaSub) {
        gapUserRepository.findByUserSub(colaSub.toString()).ifPresent(gapUser -> {
            gapUser.setUserSub(oneLoginSub);
            gapUserRepository.save(gapUser);
        });

        grantApplicantRepository.findByUserId(colaSub.toString()).ifPresent(grantApplicant -> {
            grantApplicant.setUserId(oneLoginSub);
            grantApplicantRepository.save(grantApplicant);
        });
    }

    public void deleteUser(final String oneLoginSub, final Optional<UUID> colaSubOptional) {
        // Deleting by both COLA and OneLogin sub as either could be stored against the
        // user
        gapUserRepository.deleteByUserSub(oneLoginSub);
        grantApplicantRepository.deleteByUserId(oneLoginSub);
        if (colaSubOptional.isPresent()) {
            gapUserRepository.deleteByUserSub(colaSubOptional.get().toString());
            grantApplicantRepository.deleteByUserId(colaSubOptional.get().toString());
        }
    }

}
