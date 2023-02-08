package gov.cabinetoffice.gap.adminbackend.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.mappers.SchemeMapper;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomeSchemeGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.EXAMPLE_PAGINATION_PROPS;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_SCHEME_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SAMPLE_USER_ID;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SCHEME_DTOS_EXAMPLE;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SCHEME_DTO_EXAMPLE;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SCHEME_ENTITY_LIST_EXAMPLE;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SCHEME_PATCH_DTO_EXAMPLE;
import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.SCHEME_POST_DTO_EXAMPLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class SchemeServiceTest {

    @Mock
    private SchemeMapper schemeMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SessionsService sessionsService;

    @Mock
    private SchemeRepository schemeRepository;

    @InjectMocks
    private SchemeService schemeService;

    @Test
    void getSchemeBySchemeIdHappyPath_SchemeReturned() {
        SchemeEntity testEntity = RandomeSchemeGenerator.randomSchemeEntity().build();
        Integer testSchemeId = testEntity.getId();

        when(this.schemeRepository.findById(testSchemeId)).thenReturn(Optional.of(testEntity));
        when(this.schemeMapper.schemeEntityToDto(testEntity)).thenReturn(SCHEME_DTO_EXAMPLE);

        SchemeDTO response = this.schemeService.getSchemeBySchemeId(testSchemeId);

        assertThat(response).as("Return a single SchemeDTO which matches provided id.").isEqualTo(SCHEME_DTO_EXAMPLE);
    }

    @Test
    void getSchemeBySchemeIdSadPath_NoSchemeFound() {
        when(this.schemeRepository.findById(SAMPLE_SCHEME_ID)).thenThrow(new EntityNotFoundException());

        assertThatThrownBy(() -> this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID))
                .as("Return SchemeEntityException when entitiy not found in postgres.")
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getSchemeBySchemeIdSadPath_UnexpectedError() {
        when(this.schemeRepository.findById(SAMPLE_SCHEME_ID)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID))
                .as("Return SchemeEntityException when entitiy not found in postgres.")
                .isInstanceOf(SchemeEntityException.class);
    }

    @Test
    void getSchemeBySchemeIdSadPath_SchemeDoesntBelongToLoggedInUser() {
        SchemeEntity testEntity = RandomeSchemeGenerator.randomSchemeEntity().createdBy(2).build();
        Integer testSchemeId = testEntity.getId();

        when(this.schemeRepository.findById(testSchemeId)).thenReturn(Optional.of(testEntity));

        assertThatThrownBy(() -> this.schemeService.getSchemeBySchemeId(testSchemeId))
                .as("Return AccessDeniedException when found entitiy was nott created by logged in user.")
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void sendSchemePatchRequest_SuccessfullyPatchScheme() {
        SchemeEntity testEntity = RandomeSchemeGenerator.randomSchemeEntity().build();
        Integer testSchemeId = testEntity.getId();

        when(this.schemeRepository.findById(testSchemeId)).thenReturn(Optional.of(testEntity));
        when(this.schemeRepository.save(testEntity)).thenReturn(testEntity);

        this.schemeService.patchExistingScheme(testSchemeId, SCHEME_PATCH_DTO_EXAMPLE);

        verify(this.schemeRepository).findById(testSchemeId);
        verify(this.schemeMapper).updateSchemeEntityFromPatchDto(SCHEME_PATCH_DTO_EXAMPLE, testEntity);
        verify(this.schemeRepository).save(testEntity);

    }

    @Test
    void sendSchemePatchRequest_SchemeNotFound() {
        when(this.schemeRepository.findById(SAMPLE_SCHEME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.schemeService.patchExistingScheme(SAMPLE_SCHEME_ID, SCHEME_PATCH_DTO_EXAMPLE))
                .isInstanceOf(EntityNotFoundException.class);

    }

    @Test
    void sendSchemePatchRequest_IllegalArguments() {
        when(this.schemeRepository.findById(null)).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> this.schemeService.patchExistingScheme(null, SCHEME_PATCH_DTO_EXAMPLE))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void sendSchemePatchRequest_UnexpectedError() {
        when(this.schemeRepository.findById(SAMPLE_SCHEME_ID)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.patchExistingScheme(SAMPLE_SCHEME_ID, SCHEME_PATCH_DTO_EXAMPLE))
                .isInstanceOf(SchemeEntityException.class);

    }

    @Test
    void sendSchemePatchRequest_AttemptingToPatchSchemeNotCreatedByLoggedInUser() {
        SchemeEntity testEntity = RandomeSchemeGenerator.randomSchemeEntity().createdBy(2).build();
        when(this.schemeRepository.findById(SAMPLE_SCHEME_ID)).thenReturn(Optional.of(testEntity));

        assertThatThrownBy(() -> this.schemeService.patchExistingScheme(SAMPLE_SCHEME_ID, SCHEME_PATCH_DTO_EXAMPLE))
                .isInstanceOf(AccessDeniedException.class);

    }

    @Test
    void postNewSchemeHappyPathTest() {
        SchemeEntity mockEntity = Mockito.mock(SchemeEntity.class);
        SchemeEntity testEntityAfterSave = RandomeSchemeGenerator.randomSchemeEntity().build();
        Integer testSchemeId = testEntityAfterSave.getId();

        MockHttpSession mockSession = new MockHttpSession();

        when(this.schemeMapper.schemePostDtoToEntity(SCHEME_POST_DTO_EXAMPLE)).thenReturn(mockEntity);

        when(this.schemeRepository.save(mockEntity)).thenReturn(testEntityAfterSave);

        Integer response = this.schemeService.postNewScheme(SCHEME_POST_DTO_EXAMPLE, mockSession);

        verify(mockEntity).setCreatedBy(1);
        verify(this.schemeRepository).save(mockEntity);
        verify(this.sessionsService).deleteObjectFromSession(SessionObjectEnum.newScheme, mockSession);
        assertThat(response).as("Scheme ID should match value from mock object").isEqualTo(testSchemeId);
    }

    @Test
    void postNewScheme_UnexpectedError() {
        SchemeEntity testEntity = RandomeSchemeGenerator.randomSchemeEntity().build();
        when(this.schemeMapper.schemePostDtoToEntity(SCHEME_POST_DTO_EXAMPLE)).thenReturn(testEntity);
        when(this.schemeRepository.save(testEntity)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.postNewScheme(SCHEME_POST_DTO_EXAMPLE, new MockHttpSession()))
                .isInstanceOf(SchemeEntityException.class)
                .hasMessage("Something went wrong while creating a new grant scheme.");

    }

    @Test
    void postNewScheme_IllegalArgumentException() {
        SchemeEntity testEntity = RandomeSchemeGenerator.randomSchemeEntity().build();
        when(this.schemeMapper.schemePostDtoToEntity(SCHEME_POST_DTO_EXAMPLE)).thenReturn(testEntity);
        when(this.schemeRepository.save(testEntity)).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> this.schemeService.postNewScheme(SCHEME_POST_DTO_EXAMPLE, new MockHttpSession()))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void deleteASchemeHappyPath_Successful() {
        SchemeEntity testEntity = RandomeSchemeGenerator.randomSchemeEntity().build();
        Integer testSchemeId = testEntity.getId();

        when(this.schemeRepository.findById(testSchemeId)).thenReturn(Optional.of(testEntity));

        this.schemeService.deleteASchemeById(testSchemeId);
        verify(this.schemeRepository).delete(testEntity);

    }

    @Test
    void deleteASchemeById_EntityNotFound() {
        Mockito.doThrow(new EntityNotFoundException()).when(this.schemeRepository).findById(SAMPLE_SCHEME_ID);

        assertThatThrownBy(() -> this.schemeService.deleteASchemeById(SAMPLE_SCHEME_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteASchemeById_IllegalArguments() {
        Mockito.doThrow(new IllegalArgumentException()).when(this.schemeRepository).findById(null);

        assertThatThrownBy(() -> this.schemeService.deleteASchemeById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteASchemeById_UnknownException() {
        Mockito.doThrow(new RuntimeException()).when(this.schemeRepository).findById(SAMPLE_SCHEME_ID);

        assertThatThrownBy(() -> this.schemeService.deleteASchemeById(SAMPLE_SCHEME_ID))
                .isInstanceOf(SchemeEntityException.class);
    }

    @Test
    void deleteASchemeById_EntityFoundButNotCreatedByLoggedInUser() {
        SchemeEntity testEntity = RandomeSchemeGenerator.randomSchemeEntity().createdBy(2).build();
        Integer testSchemeId = testEntity.getId();

        when(this.schemeRepository.findById(testSchemeId)).thenReturn(Optional.of(testEntity));

        assertThatThrownBy(() -> this.schemeService.deleteASchemeById(testSchemeId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getSchemesHappyPathTest() {
        when(this.schemeRepository.findByCreatedByOrderByCreatedDateDesc(SAMPLE_USER_ID))
                .thenReturn(SCHEME_ENTITY_LIST_EXAMPLE);
        when(this.schemeMapper.schemeEntityListtoDtoList(SCHEME_ENTITY_LIST_EXAMPLE)).thenReturn(SCHEME_DTOS_EXAMPLE);

        List<SchemeDTO> response = this.schemeService.getSchemes();

        assertThat(response).as("Response should contain exactly 1 entry").hasSize(1);
        assertThat(response.get(0)).as("Response contents should match given DTO").isEqualTo(SCHEME_DTO_EXAMPLE);

    }

    @Test
    void getSchemesHappyPathNoResultsTest() {
        when(this.schemeRepository.findByCreatedByOrderByCreatedDateDesc(SAMPLE_USER_ID))
                .thenReturn(Collections.emptyList());

        List<SchemeDTO> response = this.schemeService.getSchemes();

        assertThat(response).as("Response contents an empty list, no results").isEqualTo(Collections.emptyList());
    }

    @Test
    void getSchemes_UnexpectedError() {
        when(this.schemeRepository.findByCreatedByOrderByCreatedDateDesc(SAMPLE_USER_ID))
                .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.getSchemes()).isInstanceOf(SchemeEntityException.class);
    }

    @Test
    void getPaginatedSchemesHappyPathTest() {
        when(this.schemeRepository.findByCreatedByOrderByCreatedDateDesc(SAMPLE_USER_ID, EXAMPLE_PAGINATION_PROPS))
                .thenReturn(SCHEME_ENTITY_LIST_EXAMPLE);
        when(this.schemeMapper.schemeEntityListtoDtoList(SCHEME_ENTITY_LIST_EXAMPLE)).thenReturn(SCHEME_DTOS_EXAMPLE);

        List<SchemeDTO> response = this.schemeService.getPaginatedSchemes(EXAMPLE_PAGINATION_PROPS);

        assertThat(response).as("Response should contain exactly 1 entry").hasSize(1);
        assertThat(response.get(0)).as("Response contents should match given DTO").isEqualTo(SCHEME_DTO_EXAMPLE);

    }

    @Test
    void getPaginatedSchemesHappyPathNoResultsTest() {
        when(this.schemeRepository.findByCreatedByOrderByCreatedDateDesc(SAMPLE_USER_ID, EXAMPLE_PAGINATION_PROPS))
                .thenReturn(Collections.emptyList());

        List<SchemeDTO> response = this.schemeService.getPaginatedSchemes(EXAMPLE_PAGINATION_PROPS);

        assertThat(response).as("Response contents an empty list, no results").isEqualTo(Collections.emptyList());
    }

    @Test
    void getPaginatedSchemes_UnexpectedError() {
        when(this.schemeRepository.findByCreatedByOrderByCreatedDateDesc(SAMPLE_USER_ID, EXAMPLE_PAGINATION_PROPS))
                .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.getPaginatedSchemes(EXAMPLE_PAGINATION_PROPS))
                .isInstanceOf(SchemeEntityException.class);
    }

}