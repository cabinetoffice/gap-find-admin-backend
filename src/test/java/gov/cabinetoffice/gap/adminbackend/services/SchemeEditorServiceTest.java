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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

import java.util.*;

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
    private AwsEncryptionServiceImpl awsEncryptionService;

    @InjectMocks
    private SchemeEditorService schemeEditorService;

    @Test
    public void testDoesAdminOwnScheme() {
        Integer schemeId = 1;
        Integer adminId = 1;
        Mockito.when(schemeRepository.existsByIdAndGrantAdminsId(schemeId, adminId)).thenReturn(Boolean.TRUE);

        boolean result = schemeEditorService.doesAdminOwnScheme(schemeId, adminId);
        Assertions.assertTrue(result);
    }

    @Test
    public void testDoesAdminOwnScheme_returnsFalse() {
        Integer schemeId = 1;
        Integer adminId = 1;
        Mockito.when(schemeRepository.existsByIdAndGrantAdminsId(schemeId, adminId)).thenReturn(Boolean.FALSE);

        boolean result = schemeEditorService.doesAdminOwnScheme(schemeId, adminId);
        Assertions.assertFalse(result);
    }


    @Test
    void testGetEditorsFromSchemeId_happyPath() {
        GrantAdmin grantAdmin1 = GrantAdmin.builder().id(1).gapUser(GapUser.builder().userSub("sub").build()).build();
        GrantAdmin grantAdmin2 = GrantAdmin.builder().id(2).gapUser(GapUser.builder().userSub("sub").build()).build();
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
                        UserEmailResponseDto.builder().emailAddress("email".getBytes()).build(),
                        UserEmailResponseDto.builder().emailAddress("email".getBytes()).build())));

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
}
