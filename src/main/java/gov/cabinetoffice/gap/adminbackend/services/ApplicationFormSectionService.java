package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormSectionDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.application.PostSectionDTO;
import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SectionStatusEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.utils.ApplicationFormUtils;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationFormSectionService {

    private final ApplicationFormRepository applicationFormRepository;

    public ApplicationFormSectionDTO getSectionById(Integer applicationId, String sectionId, Boolean withQuestions) {
        ApplicationFormEntity entity = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(
                        "Application with id " + applicationId + " does not exist or insufficient permissions"));

        ApplicationFormSectionDTO sectionById = entity.getDefinition().getSectionById(sectionId);
        if (!withQuestions) {
            sectionById.setQuestions(null);
        }
        return sectionById;
    }

    public String addSectionToApplicationForm(Integer applicationId, PostSectionDTO sectionDTO) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(
                        "Application with id " + applicationId + " does not exist or insufficient permissions"));

        ApplicationFormSectionDTO newSection = new ApplicationFormSectionDTO(sectionDTO.getSectionTitle());

        List<ApplicationFormSectionDTO> sections = applicationForm.getDefinition().getSections();

        // check if any sections already exist with the new name
        boolean isUniqueSectionName = sections.stream()
                .noneMatch(section -> Objects.equals(section.getSectionTitle(), newSection.getSectionTitle()));

        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, session, false);

        if (isUniqueSectionName) {
            sections.add(newSection);
            this.applicationFormRepository.save(applicationForm);
        }
        else {
            throw new FieldViolationException("sectionTitle", "Section name has to be unique");
        }

        return newSection.getSectionId();
    }

    public void deleteSectionFromApplication(Integer applicationId, String sectionId) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(
                        "Application with id " + applicationId + " does not exist or insufficient permissions"));

        boolean sectionDeleted = applicationForm.getDefinition().getSections()
                .removeIf(section -> Objects.equals(section.getSectionId(), sectionId));

        if (!sectionDeleted) {
            throw new NotFoundException("Section with id " + sectionId + " does not exist");
        }

        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, session, false);

        this.applicationFormRepository.save(applicationForm);

    }

    public void updateSectionStatus(final Integer applicationId, final String sectionId,
            final SectionStatusEnum newStatus) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();
        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(
                        "Application with id " + applicationId + " does not exist or insufficient permissions"));

        applicationForm.getDefinition().getSectionById(sectionId).setSectionStatus(newStatus);

        ApplicationFormUtils.updateAuditDetailsAfterFormChange(applicationForm, session, false);

        this.applicationFormRepository.save(applicationForm);
    }

    public void updateSectionOrder(final Integer applicationId, final String sectionId, final Integer increment) {
        ApplicationFormEntity applicationForm = this.applicationFormRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(
                        "Application with id " + applicationId + " does not exist or insufficient permissions"));

        List<ApplicationFormSectionDTO> sections = applicationForm.getDefinition().getSections();
        ApplicationFormSectionDTO section = applicationForm.getDefinition().getSectionById(sectionId);
        int index = sections.indexOf(section);

        final int SECTION_LIST_SIZE = sections.size() - 1;
        final int ESSENTIAL_AND_ELIGIBILITY = 2;
        final int NEW_SECTION_INDEX = index + increment;

        if (NEW_SECTION_INDEX < ESSENTIAL_AND_ELIGIBILITY)
            throw new FieldViolationException("sectionId", "Section is already at the top");

        if (NEW_SECTION_INDEX > SECTION_LIST_SIZE)
            throw new FieldViolationException("sectionId", "Section is already at the bottom");

        sections.remove(index);
        sections.add(NEW_SECTION_INDEX, section);

        applicationForm.getDefinition().setSections(sections);
        this.applicationFormRepository.save(applicationForm);
    }

}
