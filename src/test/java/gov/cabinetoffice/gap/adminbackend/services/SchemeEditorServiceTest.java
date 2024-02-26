package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemeEditorsDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserEmailResponseDto;
import gov.cabinetoffice.gap.adminbackend.entities.GapUser;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdmin;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.enums.SchemeEditorRoleEnum;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.services.encryption.AwsEncryptionServiceImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import static org.mockito.Mockito.*;

import java.util.*;

@SpringJUnitConfig
@WithAdminSession
public class SchemeEditorServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserServiceConfig userServiceConfig;

    @Mock
    private SchemeService schemeService;

    @Mock
    private SchemeRepository schemeRepository;

    @Mock
    private AwsEncryptionServiceImpl awsEncryptionService;

    @InjectMocks
    private SchemeEditorService schemeEditorService;

    @Test
    public void testDoesAdminOwnScheme() {
        Integer schemeId = 1;
        Integer adminId = 1;
        SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId).createdBy(adminId).build();
        when(schemeService.findSchemeById(schemeId)).thenReturn(schemeEntity);
        List<SchemeEntity> adminSchemes = new ArrayList<>();
        adminSchemes.add(schemeEntity);
        Mockito.when(schemeRepository.findByCreatedBy(adminId)).thenReturn(adminSchemes);

        boolean result = schemeEditorService.doesAdminOwnScheme(schemeId, adminId);
        Assertions.assertTrue(result);
    }

    @Test
    public void testDoesAdminOwnScheme_returnsFalse() {
        Integer schemeId = 1;
        Integer adminId = 1;
        SchemeEntity schemeEntity = SchemeEntity.builder().id(schemeId).createdBy(adminId).build();
        when(schemeService.findSchemeById(schemeId)).thenReturn(schemeEntity);
        Mockito.when(schemeRepository.findByCreatedBy(adminId)).thenReturn(new ArrayList<>());

        boolean result = schemeEditorService.doesAdminOwnScheme(schemeId, adminId);
        Assertions.assertFalse(result);
    }

    @Test
    public void testDoesAdminOwnScheme_SchemeNotFound() {
        Integer schemeId = 1;
        Integer adminId = 1;
        when(schemeService.findSchemeById(schemeId)).thenThrow(new SchemeEntityException("Scheme not found"));

        Assertions.assertThrows(SchemeEntityException.class,
                () -> schemeEditorService.doesAdminOwnScheme(schemeId, adminId));
    }

    @Test
    void testGetEditorsFromSchemeId_happyPath() {
        GrantAdmin grantAdmin1 = GrantAdmin.builder().id(1).gapUser(GapUser.builder().userSub("sub").build()).build();
        GrantAdmin grantAdmin2 = GrantAdmin.builder().id(2).gapUser(GapUser.builder().userSub("sub").build()).build();
        List<GrantAdmin> editors = Arrays.asList(grantAdmin1, grantAdmin2);
        SchemeEntity schemeEntity = SchemeEntity.builder().id(1).createdBy(1).grantAdmins(editors).build();

        when(schemeService.findSchemeById(anyInt())).thenReturn(schemeEntity);

        ResponseEntity<List<UserEmailResponseDto>> responseEntity = ResponseEntity.ok(Arrays.asList(
                UserEmailResponseDto.builder()
                        .emailAddress("email1".getBytes()).build(),
                UserEmailResponseDto.builder()
                        .emailAddress("email2".getBytes()).build()));
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

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
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
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
        ResponseEntity<List<UserEmailResponseDto>> responseEntity = ResponseEntity.ok(Arrays.asList(
                UserEmailResponseDto.builder()
                        .emailAddress("email1".getBytes()).build(),
                UserEmailResponseDto.builder()
                        .emailAddress("email2".getBytes()).build()));

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        when(awsEncryptionService.decryptField(any()))
                .thenThrow(new IllegalStateException("Wrong Encryption Context!"));

        Assertions.assertThrows(IllegalStateException.class, () -> {
            schemeEditorService.getEditorsFromSchemeId(1, "authHeader");
        });
    }
}
