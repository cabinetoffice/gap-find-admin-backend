package gov.cabinetoffice.gap.adminbackend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.FeatureFlagsConfigurationProperties;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeDTO;
import gov.cabinetoffice.gap.adminbackend.entities.*;
import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.mappers.SchemeMapper;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdvertRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSchemeGenerator;
import org.apache.http.impl.client.HttpClients;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

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
    private UserService userService;

    @Mock
    private SchemeRepository schemeRepository;

    @Mock
    private GrantAdminRepository grantAdminRepository;

    @Mock
    private GrantAdvertService grantAdvertService;

    @Mock
    private ApplicationFormService applicationFormService;

    @Mock
    private FeatureFlagsConfigurationProperties featureFlagsConfigurationProperties;

    @InjectMocks
    private SchemeService schemeService;

    @Test
    void getSchemeBySchemeIdHappyPath_SchemeReturned() {
        SchemeEntity testEntity = RandomSchemeGenerator.randomSchemeEntity().build();
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
                .as("Return SchemeEntityException when entity not found in postgres.")
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getSchemeBySchemeIdSadPath_UnexpectedError() {
        when(this.schemeRepository.findById(SAMPLE_SCHEME_ID)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID))
                .as("Return SchemeEntityException when entity not found in postgres.")
                .isInstanceOf(SchemeEntityException.class);
    }

    @Test
    void sendSchemePatchRequest_SuccessfullyPatchScheme() {
        SchemeEntity testEntity = RandomSchemeGenerator.randomSchemeEntity().build();
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
    void postNewSchemeHappyPathTest() {
        SchemeEntity mockEntity = Mockito.mock(SchemeEntity.class);
        SchemeEntity testEntityAfterSave = RandomSchemeGenerator.randomSchemeEntity().build();
        Integer testSchemeId = testEntityAfterSave.getId();
        GrantAdmin mockAdmin = GrantAdmin.builder().id(1).build();

        MockHttpSession mockSession = new MockHttpSession();

        when(this.schemeMapper.schemePostDtoToEntity(SCHEME_POST_DTO_EXAMPLE)).thenReturn(mockEntity);

        when(this.schemeRepository.save(mockEntity)).thenReturn(testEntityAfterSave);
        when(this.featureFlagsConfigurationProperties.isNewMandatoryQuestionsEnabled()).thenReturn(false);

        when(this.grantAdminRepository.findById(mockAdmin.getId())).thenReturn(Optional.of(mockAdmin));


        Integer response = this.schemeService.postNewScheme(SCHEME_POST_DTO_EXAMPLE, mockSession);

        verify(mockEntity).setCreatedBy(1);
        verify(this.schemeRepository).save(mockEntity);
        verify(mockEntity).addAdmin(mockAdmin);
        verify(this.sessionsService).deleteObjectFromSession(SessionObjectEnum.newScheme, mockSession);
        assertThat(response).as("Scheme ID should match value from mock object").isEqualTo(testSchemeId);
    }

    @Test
    void postNewSchemeHappyPathTest_featureFlagForNewMandatoryQuestionIsOn() {
        SchemeEntity mockEntity = Mockito.mock(SchemeEntity.class);
        SchemeEntity testEntityAfterSave = RandomSchemeGenerator.randomSchemeEntity().build();
        Integer testSchemeId = testEntityAfterSave.getId();
        GrantAdmin mockAdmin = GrantAdmin.builder().id(1).build();


        MockHttpSession mockSession = new MockHttpSession();

        when(this.schemeMapper.schemePostDtoToEntity(SCHEME_POST_DTO_EXAMPLE)).thenReturn(mockEntity);

        when(this.schemeRepository.save(mockEntity)).thenReturn(testEntityAfterSave);
        when(this.featureFlagsConfigurationProperties.isNewMandatoryQuestionsEnabled()).thenReturn(true);

        when(this.grantAdminRepository.findById(mockAdmin.getId())).thenReturn(Optional.of(mockAdmin));

        Integer response = this.schemeService.postNewScheme(SCHEME_POST_DTO_EXAMPLE, mockSession);

        verify(mockEntity).setCreatedBy(1);
        verify(mockEntity).setVersion(2);
        verify(this.schemeRepository).save(mockEntity);
        verify(mockEntity).addAdmin(mockAdmin);
        verify(this.sessionsService).deleteObjectFromSession(SessionObjectEnum.newScheme, mockSession);
        assertThat(response).as("Scheme ID should match value from mock object").isEqualTo(testSchemeId);
    }

    @Test
    void postNewScheme_UnexpectedError() {
        SchemeEntity testEntity = RandomSchemeGenerator.randomSchemeEntity().build();
        when(this.schemeMapper.schemePostDtoToEntity(SCHEME_POST_DTO_EXAMPLE)).thenReturn(testEntity);
        when(this.schemeRepository.save(testEntity)).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.postNewScheme(SCHEME_POST_DTO_EXAMPLE, new MockHttpSession()))
                .isInstanceOf(SchemeEntityException.class)
                .hasMessage("Something went wrong while creating a new grant scheme.");

    }

    @Test
    void postNewScheme_IllegalArgumentException() {
        SchemeEntity testEntity = RandomSchemeGenerator.randomSchemeEntity().build();
        GrantAdmin mockAdmin = GrantAdmin.builder().id(1).build();

        when(this.schemeMapper.schemePostDtoToEntity(SCHEME_POST_DTO_EXAMPLE)).thenReturn(testEntity);
        when(this.grantAdminRepository.findById(mockAdmin.getId())).thenReturn(Optional.of(mockAdmin));
        when(this.schemeRepository.save(testEntity)).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> this.schemeService.postNewScheme(SCHEME_POST_DTO_EXAMPLE, new MockHttpSession()))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void postNewScheme_SchemeEntityException() {
        Integer invalidId = 999;
        SchemeEntity testEntity = RandomSchemeGenerator.randomSchemeEntity().build();
        when(this.schemeMapper.schemePostDtoToEntity(SCHEME_POST_DTO_EXAMPLE)).thenReturn(testEntity);
        when(this.grantAdminRepository.findById(invalidId)).thenReturn(null);


        assertThatThrownBy(() -> this.schemeService.postNewScheme(SCHEME_POST_DTO_EXAMPLE, new MockHttpSession()))
                .isInstanceOf(SchemeEntityException.class);

    }

    @Test
    void deleteASchemeHappyPath_Successful() {
        SchemeEntity testEntity = RandomSchemeGenerator.randomSchemeEntity().build();
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
    void getSchemesHappyPathTest() {
        when(this.schemeRepository.findByGrantAdminsIdOrderByCreatedDateDesc(SAMPLE_USER_ID))
                .thenReturn(SCHEME_ENTITY_LIST_EXAMPLE);
        when(this.schemeMapper.schemeEntityListtoDtoList(SCHEME_ENTITY_LIST_EXAMPLE)).thenReturn(SCHEME_DTOS_EXAMPLE);

        List<SchemeDTO> response = this.schemeService.getSignedInUsersSchemes();

        assertThat(response).as("Response should contain exactly 1 entry").hasSize(1);
        assertThat(response.get(0)).as("Response contents should match given DTO").isEqualTo(SCHEME_DTO_EXAMPLE);

    }

    @Test
    void getSchemesHappyPathNoResultsTest() {
        when(this.schemeRepository.findByGrantAdminsIdOrderByCreatedDateDesc(SAMPLE_USER_ID))
                .thenReturn(Collections.emptyList());

        List<SchemeDTO> response = this.schemeService.getSignedInUsersSchemes();

        assertThat(response).as("Response contents an empty list, no results").isEqualTo(Collections.emptyList());
    }

    @Test
    void getSchemes_UnexpectedError() {
        when(this.schemeRepository.findByGrantAdminsIdOrderByCreatedDateDesc(SAMPLE_USER_ID))
                .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.getSignedInUsersSchemes())
                .isInstanceOf(SchemeEntityException.class);
    }

    @Test
    void getPaginatedSchemesHappyPathTest() {
        when(this.schemeRepository.findByGrantAdminsIdOrderByCreatedDateDesc(SAMPLE_USER_ID, EXAMPLE_PAGINATION_PROPS))
                .thenReturn(SCHEME_ENTITY_LIST_EXAMPLE);
        when(this.schemeMapper.schemeEntityListtoDtoList(SCHEME_ENTITY_LIST_EXAMPLE)).thenReturn(SCHEME_DTOS_EXAMPLE);

        List<SchemeDTO> response = this.schemeService.getPaginatedSchemes(EXAMPLE_PAGINATION_PROPS);

        assertThat(response).as("Response should contain exactly 1 entry").hasSize(1);
        assertThat(response.get(0)).as("Response contents should match given DTO").isEqualTo(SCHEME_DTO_EXAMPLE);

    }

    @Test
    void getPaginatedSchemesHappyPathNoResultsTest() {
        when(this.schemeRepository.findByGrantAdminsIdOrderByCreatedDateDesc(SAMPLE_USER_ID, EXAMPLE_PAGINATION_PROPS))
                .thenReturn(Collections.emptyList());

        List<SchemeDTO> response = this.schemeService.getPaginatedSchemes(EXAMPLE_PAGINATION_PROPS);

        assertThat(response).as("Response contents an empty list, no results").isEqualTo(Collections.emptyList());
    }

    @Test
    void getPaginatedSchemes_UnexpectedError() {
        when(this.schemeRepository.findByGrantAdminsIdOrderByCreatedDateDesc(SAMPLE_USER_ID, EXAMPLE_PAGINATION_PROPS))
                .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> this.schemeService.getPaginatedSchemes(EXAMPLE_PAGINATION_PROPS))
                .isInstanceOf(SchemeEntityException.class);
    }

    @Nested
    class GetAdminsSchemes {

        @Test
        void happyPathTest() {
            when(schemeRepository.findByCreatedBy(1)).thenReturn(SCHEME_ENTITY_LIST_EXAMPLE);
            when(schemeMapper.schemeEntityListtoDtoList(SCHEME_ENTITY_LIST_EXAMPLE)).thenReturn(SCHEME_DTOS_EXAMPLE);

            List<SchemeDTO> response = schemeService.getAdminsSchemes(1);

            assertThat(response).as("Response should contain exactly 1 entry").hasSize(1);
            assertThat(response.get(0)).as("Response contents should match given DTO").isEqualTo(SCHEME_DTO_EXAMPLE);
        }

    }

    @Test
    void patchCreatedByUpdatesGrantScheme() {
        final int testAdmin = 1;
        final int patchedAdmin = 2;
        SchemeEntity testScheme = SchemeEntity.builder().id(1).createdBy(testAdmin).build();
        SchemeEntity patchedScheme = SchemeEntity.builder().id(1).createdBy(patchedAdmin).build();

        Mockito.when(SchemeServiceTest.this.schemeRepository.findById(1)).thenReturn(Optional.of(testScheme));
        Mockito.when(SchemeServiceTest.this.schemeRepository.save(testScheme)).thenReturn(patchedScheme);
        Mockito.when(SchemeServiceTest.this.schemeRepository.findById(2)).thenReturn(Optional.of(patchedScheme));

        SchemeServiceTest.this.schemeService.patchCreatedBy(
                GrantAdmin.builder().id(2).funder(FundingOrganisation.builder().id(1).build()).build(), 1);
        AssertionsForClassTypes.assertThat(testScheme.getCreatedBy()).isEqualTo(patchedScheme.getCreatedBy());
    }

    @Test
    void patchCreatedByThrowsAnErrorIfSchemeIsNotPresent() {
        Mockito.when(SchemeServiceTest.this.schemeRepository.findById(1)).thenReturn(Optional.empty());

        AssertionsForClassTypes.assertThatThrownBy(
                () -> SchemeServiceTest.this.schemeService.patchCreatedBy(GrantAdmin.builder().id(1).build(), 1))
                .isInstanceOf(SchemeEntityException.class).hasMessage(
                        "Update grant ownership failed: Something went wrong while trying to find scheme with id: 1");
    }

    @Test
    void getPaginatedOwnedSchemes_Success() {
        final int adminId = 1;
        final Pageable pagination = Pageable.ofSize(2);
        final SchemeEntity scheme = SchemeEntity.builder().build();
        final List<SchemeEntity> schemes = List.of(scheme);
        final SchemeDTO schemeDto = SchemeDTO.builder().build();
        final List<SchemeDTO> schemeDtos = List.of(schemeDto);

        when(schemeRepository.findByCreatedByOrderByLastUpdatedDescCreatedDateDesc(1, pagination))
                .thenReturn(schemes);

        when(schemeMapper.schemeEntityListtoDtoList(schemes))
                .thenReturn(schemeDtos);

        final List<SchemeDTO> methodResponse = schemeService.getPaginatedOwnedSchemesByAdminId(adminId, pagination);

        assertThat(methodResponse).isEqualTo(schemeDtos);
    }

    @Test
    void getOwnedSchemes_Success() {
        final int adminId = 1;
        final SchemeEntity scheme = SchemeEntity.builder().build();
        final List<SchemeEntity> schemes = List.of(scheme);
        final SchemeDTO schemeDto = SchemeDTO.builder().build();
        final List<SchemeDTO> schemeDtos = List.of(schemeDto);

        when(schemeRepository.findByCreatedByOrderByLastUpdatedDescCreatedDateDesc(1))
                .thenReturn(schemes);

        when(schemeMapper.schemeEntityListtoDtoList(schemes))
                .thenReturn(schemeDtos);

        final List<SchemeDTO> methodResponse = schemeService.getOwnedSchemesByAdminId(adminId);

        assertThat(methodResponse).isEqualTo(schemeDtos);
    }

    @Test
    void getPaginatedEditableSchemes_Success() {
        final int adminId = 1;
        final Pageable pagination = Pageable.ofSize(2);
        final SchemeEntity scheme = SchemeEntity.builder().build();
        final List<SchemeEntity> schemes = List.of(scheme);
        final SchemeDTO schemeDto = SchemeDTO.builder().build();
        final List<SchemeDTO> schemeDtos = List.of(schemeDto);

        when(schemeRepository.findByCreatedByNotAndGrantAdminsIdOrderByLastUpdatedDescCreatedDateDesc(1, 1, pagination))
                .thenReturn(schemes);

        when(schemeMapper.schemeEntityListtoDtoList(schemes))
                .thenReturn(schemeDtos);

        final List<SchemeDTO> methodResponse = schemeService.getPaginatedEditableSchemesByAdminId(adminId, pagination);

        assertThat(methodResponse).isEqualTo(schemeDtos);
    }

    @Test
    void getEditableSchemes_Success() {
        final int adminId = 1;
        final SchemeEntity scheme = SchemeEntity.builder().build();
        final List<SchemeEntity> schemes = List.of(scheme);
        final SchemeDTO schemeDto = SchemeDTO.builder().build();
        final List<SchemeDTO> schemeDtos = List.of(schemeDto);

        when(schemeRepository.findByCreatedByNotAndGrantAdminsIdOrderByLastUpdatedDescCreatedDateDesc(1, 1))
                .thenReturn(schemes);

        when(schemeMapper.schemeEntityListtoDtoList(schemes))
                .thenReturn(schemeDtos);

        final List<SchemeDTO> methodResponse = schemeService.getEditableSchemesByAdminId(adminId);

        assertThat(methodResponse).isEqualTo(schemeDtos);
    }

    @Test
    void testRemoveAdminReferenceWhenUserExists() {

        final String userSub = "123";
        final GrantAdmin grantAdmin = GrantAdmin.builder().id(1)
                .gapUser(GapUser.builder().userSub(userSub).build()).build();

        final SchemeEntity scheme = SchemeEntity.builder().id(1).lastUpdatedBy(grantAdmin.getId()).lastUpdatedBy(1).build();
        final List<SchemeEntity> schemes = List.of(scheme);

        when(grantAdminRepository.findByGapUserUserSub(anyString())).thenReturn(Optional.of(grantAdmin));
        when(schemeRepository.findByGrantAdminsIdOrderByCreatedDateDesc(any())).thenReturn(schemes);

        schemeService.removeAdminReference("userSub");

        verify(schemeRepository).saveAll(schemes);
        verify(grantAdvertService, times(1)).removeAdminReferenceBySchemeId(grantAdmin, 1);
        verify(applicationFormService, times(1)).removeAdminReferenceBySchemeId(grantAdmin, 1);
    }
}