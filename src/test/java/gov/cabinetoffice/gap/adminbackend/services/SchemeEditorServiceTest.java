package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SchemeEditorRoleEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.FieldViolationException;
import gov.cabinetoffice.gap.adminbackend.exceptions.ForbiddenException;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdminRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import gov.cabinetoffice.gap.adminbackend.testdata.generators.RandomSchemeGenerator;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@WithAdminSession
public class SchemeEditorServiceTest {
    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private SchemeService schemeService;

    @Mock
    private UserServiceConfig userServiceConfig;

    @Mock
    private SchemeRepository schemeRepository;

    @Mock
    private UserService userService;

    @Mock
    private GrantAdminRepository grantAdminRepository;

    @Mock
    private AwsEncryptionServiceImpl awsEncryptionService;

    @InjectMocks
    private SchemeEditorService schemeEditorService;

    @Test
    public void testDoesAdminOwnScheme() {
        Integer schemeId = 1;
        Integer adminId = 1;
        Mockito.when(schemeRepository.existsByIdAndCreatedBy(schemeId, adminId)).thenReturn(Boolean.TRUE);

        boolean result = schemeEditorService.doesAdminOwnScheme(schemeId, adminId);
        Assertions.assertTrue(result);
    }

    @Test
    public void testDoesAdminOwnScheme_returnsFalse() {
        Integer schemeId = 1;
        Integer adminId = 1;
        Mockito.when(schemeRepository.existsByIdAndCreatedBy(schemeId, adminId)).thenReturn(Boolean.FALSE);

        boolean result = schemeEditorService.doesAdminOwnScheme(schemeId, adminId);
        Assertions.assertFalse(result);
    }


    @Test
    void testGetEditorsFromSchemeId_happyPath() {
        GrantAdmin grantAdmin1 = GrantAdmin.builder().id(1).gapUser(GapUser.builder().userSub("sub1").build()).build();
        GrantAdmin grantAdmin2 = GrantAdmin.builder().id(2).gapUser(GapUser.builder().userSub("sub2").build()).build();
        List<GrantAdmin> editors = Arrays.asList(grantAdmin1, grantAdmin2);
        SchemeEntity schemeEntity = SchemeEntity.builder().id(1).createdBy(1).grantAdmins(editors).build();
        when(userServiceConfig.getDomain()).thenReturn("domain");
        when(schemeService.findSchemeById(anyInt())).thenReturn(schemeEntity);

        final WebClient webClient = mock(WebClient.class);
        final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.cookie(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(eq(new ParameterizedTypeReference<List<UserEmailResponseDto>>() {})))
                .thenReturn(Mono.just(Arrays.asList(
                        UserEmailResponseDto.builder().emailAddress("email".getBytes()).sub("sub1").build(),
                        UserEmailResponseDto.builder().emailAddress("email".getBytes()).sub("sub2").build())));

        when(awsEncryptionService.decryptField(any())).thenReturn("decrypted-email");

        List<SchemeEditorsDTO> result = schemeEditorService.getEditorsFromSchemeId(1, "authHeader");

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(1, result.get(0).id());
        Assertions.assertEquals("decrypted-email", result.get(0).email());
        Assertions.assertEquals(SchemeEditorRoleEnum.Owner, result.get(0).role());
        Assertions.assertEquals(2, result.get(1).id());
        Assertions.assertEquals("decrypted-email", result.get(1).email());
        Assertions.assertEquals(SchemeEditorRoleEnum.Editor, result.get(1).role());
    }

    @Test
    void testgetEditorsFromSchemeId_ThrowsException() {
        when(schemeService.findSchemeById(anyInt())).thenThrow(new SchemeEntityException());

        Assertions.assertThrows(SchemeEntityException.class, () -> {
            schemeEditorService.getEditorsFromSchemeId(1, "authHeader");
        });
    }

    @Test
    void testGetEditorsFromSchemeId_ThrowsRestClientException() {
        SchemeEntity schemeEntity = SchemeEntity.builder().id(1).createdBy(1).build();
        when(schemeService.findSchemeById(anyInt())).thenReturn(schemeEntity);
        when(webClientBuilder.build())
                .thenThrow(new RestClientException("Test RestClientException"));

        Assertions.assertThrows(RestClientException.class, () -> {
            schemeEditorService.getEditorsFromSchemeId(1, "authHeader");
        });
    }

    @Test
    void testGetEditorsFromSchemeId_DecryptField_ThrowsIllegalStateException() {
        GrantAdmin grantAdmin1 = GrantAdmin.builder().id(1).gapUser(GapUser.builder().userSub("sub").build()).build();
        GrantAdmin grantAdmin2 = GrantAdmin.builder().id(2).gapUser(GapUser.builder().userSub("sub").build()).build();
        List<GrantAdmin> editors = Arrays.asList(grantAdmin1, grantAdmin2);
        SchemeEntity schemeEntity = SchemeEntity.builder().id(1).createdBy(1).grantAdmins(editors).build();
        when(schemeService.findSchemeById(anyInt())).thenReturn(schemeEntity);

        final WebClient webClient = mock(WebClient.class);
        final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.cookie(any(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(eq(new ParameterizedTypeReference<List<UserEmailResponseDto>>() {})))
                .thenReturn(Mono.just(Arrays.asList(
                        UserEmailResponseDto.builder().emailAddress("email".getBytes()).build(),
                        UserEmailResponseDto.builder().emailAddress("email".getBytes()).build())));


        when(awsEncryptionService.decryptField(any()))
                .thenThrow(new IllegalStateException("Wrong Encryption Context!"));

        Assertions.assertThrows(IllegalStateException.class, () -> {
            schemeEditorService.getEditorsFromSchemeId(1, "authHeader");
        });
    }

    @Test
    void addEditorToSchemeReturnsSchemeWithGrantAdmin() {
        final SchemeEntity testScheme = RandomSchemeGenerator.randomSchemeEntity().build();
        final GrantAdmin testAdmin = GrantAdmin.builder().id(1).build();
        final SchemeEntity patchedScheme = RandomSchemeGenerator.randomSchemeEntity().build();
        patchedScheme.addAdmin(testAdmin);

        Mockito.when(SchemeEditorServiceTest.this.userService.getGrantAdminIdFromUserServiceEmail("test@test.gov", "jwt"))
                .thenReturn(testAdmin);
        Mockito.when(SchemeEditorServiceTest.this.schemeRepository.findById(1)).thenReturn(Optional.of(testScheme));
        Mockito.when(SchemeEditorServiceTest.this.grantAdminRepository.findById(1)).thenReturn(Optional.of(testAdmin));
        Mockito.when(SchemeEditorServiceTest.this.schemeRepository.save(testScheme)).thenReturn(patchedScheme);

        SchemeEditorServiceTest.this.schemeEditorService.addEditorToScheme(1, "test@test.gov", "jwt");
        AssertionsForClassTypes.assertThat(patchedScheme.getGrantAdmins().contains(testAdmin)).isTrue();

    }

    @Test
    void addEditorToSchemeThrowsAnErrorIfSchemeIsNotPresent() {
        Mockito.when(SchemeEditorServiceTest.this.schemeRepository.findById(1)).thenReturn(Optional.empty());

        AssertionsForClassTypes.assertThatThrownBy(
                        () -> SchemeEditorServiceTest.this.schemeEditorService.addEditorToScheme(1, "test@test.gov", "jwt"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void addEditorToSchemeThrowsAnErrorIfGrantAdminIsNotPresent() {
        final SchemeEntity testScheme = RandomSchemeGenerator.randomSchemeEntity().build();
        Mockito.when(SchemeEditorServiceTest.this.schemeRepository.findById(1)).thenReturn(Optional.of(testScheme));
        Mockito.when(SchemeEditorServiceTest.this.userService.getGrantAdminIdFromUserServiceEmail("test@test.gov", "jwt"))
                .thenThrow(new EntityNotFoundException());
    }

    @Test
    void addEditorThrowsAnErrorIfGrantAdminIsAlreadyAnEditor() {
        final SchemeEntity testScheme = RandomSchemeGenerator.randomSchemeEntity().build();
        final GrantAdmin testAdmin = GrantAdmin.builder().id(1).build();
        testScheme.addAdmin(testAdmin);
        Mockito.when(SchemeEditorServiceTest.this.schemeRepository.findById(1)).thenReturn(Optional.of(testScheme));
        Mockito.when(SchemeEditorServiceTest.this.userService.getGrantAdminIdFromUserServiceEmail("test@test.gov", "jwt"))
                .thenReturn(testAdmin);
        AssertionsForClassTypes.assertThatThrownBy(
                        () -> SchemeEditorServiceTest.this.schemeEditorService.addEditorToScheme(1, "test@test.gov", "jwt"))
                .isInstanceOf(FieldViolationException.class);
    }


    @Nested
    class DeleteEditor {

        final Integer schemeId = 1;
        final Integer editorId = 2;
        @Test
        void removesAnEditor() {
            final Date now = new Date();
            final SchemeEntity scheme = SchemeEntity.builder()
                    .createdBy(1)
                    .createdDate(now.toInstant())
                    .build();
            final GrantAdmin admin = GrantAdmin.builder()
                    .schemes(Collections.singletonList(scheme))
                    .build();
            scheme.addAdmin(admin);

            assertThat(scheme.getGrantAdmins()).hasSize(1);
            final ArgumentCaptor<SchemeEntity> schemeArgumentCaptor = ArgumentCaptor.forClass(SchemeEntity.class);
            when(schemeRepository.findById(schemeId)).thenReturn(Optional.of(scheme));
            when(grantAdminRepository.findById(editorId)).thenReturn(Optional.of(admin));

            schemeEditorService.deleteEditor(schemeId, editorId);

            verify(schemeRepository).save(schemeArgumentCaptor.capture());
            assertThat(schemeArgumentCaptor.getValue().getGrantAdmins()).isEmpty();
        }

        @Test
        void throwsNotFoundExceptionWhenNoSchemeFound() {
            when(schemeRepository.findById(schemeId)).thenReturn(Optional.empty());
            Assertions.assertThrows(NotFoundException.class, () -> schemeEditorService.deleteEditor(schemeId, editorId));
        }

        @Test
        void throwsNotFoundExceptionWhenNoAdminFound() {
            final SchemeEntity scheme = SchemeEntity.builder()
                    .createdBy(1)
                    .build();
            when(schemeRepository.findById(schemeId)).thenReturn(Optional.of(scheme));
            when(grantAdminRepository.findById(editorId)).thenReturn(Optional.empty());
            Assertions.assertThrows(NotFoundException.class, () -> schemeEditorService.deleteEditor(schemeId, editorId));
        }

        @Test
        void throwsForbiddenExceptionWhenSchemeCreatedByMatchesEditorId() {
            final SchemeEntity scheme = SchemeEntity.builder()
                    .createdBy(editorId)
                    .build();
            final GrantAdmin admin = GrantAdmin.builder()
                    .schemes(Collections.singletonList(scheme))
                    .build();
            scheme.addAdmin(admin);
            when(schemeRepository.findById(schemeId)).thenReturn(Optional.of(scheme));
            when(grantAdminRepository.findById(editorId)).thenReturn(Optional.of(admin));
            Assertions.assertThrows(ForbiddenException.class, () -> schemeEditorService.deleteEditor(schemeId, editorId));
        }
    }
}
