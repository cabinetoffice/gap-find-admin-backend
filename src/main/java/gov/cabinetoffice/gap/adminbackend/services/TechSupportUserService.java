package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.user.CreateTechSupportUserDto;
import gov.cabinetoffice.gap.adminbackend.entities.FundingOrganisation;
import gov.cabinetoffice.gap.adminbackend.entities.TechSupportUser;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.repositories.FundingOrganisationRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.TechSupportUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Log4j2
public class TechSupportUserService {

    private final TechSupportUserRepository techSupportUserRepository;
    private final FundingOrganisationRepository fundingOrganisationRepository;

    public void createTechSupportUser(CreateTechSupportUserDto techSupportUserDto) {

        FundingOrganisation fundingOrganisation = fundingOrganisationRepository
                .findById(techSupportUserDto.departmentId()).orElseThrow(()
                        -> new NotFoundException("Department not found with id: "
                        .concat(String.valueOf(techSupportUserDto.departmentId()))));

        techSupportUserRepository.save(TechSupportUser.builder()
                .userSub(techSupportUserDto.userSub())
                .funder(fundingOrganisation).build());
    }

    @Transactional
    public void deleteTechSupportUser(String userSub) {
        log.info("Removing tech support user: {} ", userSub);
        techSupportUserRepository.deleteByUserSub(userSub);
    }

    public Optional<TechSupportUser> getTechSupportUserBySub(String userSub) {
        return techSupportUserRepository.findByUserSub(userSub);
    }

    public void updateFundingOrganisation(TechSupportUser techSupportUser, String departmentName) {
        Optional<FundingOrganisation> fundingOrganisation = this.fundingOrganisationRepository
                .findByName(departmentName);

        if (fundingOrganisation.isEmpty()) {
            FundingOrganisation newFundingOrg = fundingOrganisationRepository
                    .save(new FundingOrganisation(null, departmentName));
            techSupportUser.setFunder(newFundingOrg);
            techSupportUserRepository.save(techSupportUser);

            log.info("Created new funding organisation: {}", newFundingOrg);
            log.info("Updated tech support user's funding organisation: {}", techSupportUser.getUserSub());

        }
        else {
            techSupportUser.setFunder(fundingOrganisation.get());
            techSupportUserRepository.save(techSupportUser);
            log.info("Updated tech support user's funding organisation: {}", techSupportUser.getUserSub());
        }
    }
}
