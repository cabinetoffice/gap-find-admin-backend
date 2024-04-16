package gov.cabinetoffice.gap.adminbackend.services;

import com.contentful.java.cma.model.CMAEntry;
import com.contentful.java.cma.model.CMASystem;
import gov.cabinetoffice.gap.adminbackend.config.ContentfulConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.OpenSearchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringJUnitConfig
class OpenSearchServiceTest {
    @Spy
    @InjectMocks
    private OpenSearchService openSearchService;

    @Mock
    private OpenSearchConfig openSearchConfig;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private ContentfulConfigProperties contentfulProperties;

    private CMAEntry contentfulEntry;

    @BeforeEach
    void beforeEach() {
        when(openSearchConfig.getUrl()).thenReturn("testUrl");
        when(openSearchConfig.getDomain()).thenReturn("testDomain");
        when(openSearchConfig.getUsername()).thenReturn("testUsername");
        when(openSearchConfig.getPassword()).thenReturn("testPassword");

        contentfulEntry = new CMAEntry();
        final CMASystem system = new CMASystem();
        system.setId("testId");
        contentfulEntry.setSystem(system);
    }

    @Test
    void indexEntry() {
        final WebClient mockWebClient = mock(WebClient.class);
        final WebClient.RequestBodyUriSpec mockRequestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        final WebClient.RequestHeadersSpec mockRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final WebClient.RequestHeadersUriSpec mockRequestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        final WebClient.ResponseSpec mockResponseSpec = mock(WebClient.ResponseSpec.class);

        when(contentfulProperties.getSpaceId()).thenReturn("Space");
        when(contentfulProperties.getEnvironmentId()).thenReturn("environment");
        when(contentfulProperties.getAccessToken()).thenReturn("accessToken");
        when(contentfulProperties.getDeliveryAPIAccessToken()).thenReturn("deliveryAccessToken");

        when(webClientBuilder.build()).thenReturn(mockWebClient);

        when(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri(anyString())).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.headers(any())).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"system\":{\"id\":\"testId\"},\"environmentId\":\"master\",\"id\":\"testId\",\"published\":false,\"archived\":false}"));

        when(mockWebClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("testUrl/testDomain/_doc/testId")).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.body(any(), eq(String.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.header("Content-Type", "application/json; UTF-8")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.header("Authorization", "Basic dGVzdFVzZXJuYW1lOnRlc3RQYXNzd29yZA==")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(void.class)).thenReturn(Mono.empty());

        openSearchService.indexEntry(contentfulEntry);

        verify(webClientBuilder.build().get(), times(1))
                .uri("https://api.contentful.com/spaces/Space/environments/environment/entries/testId");

        verify(webClientBuilder.build().put(), times(1))
                .uri("testUrl/testDomain/_doc/testId");
    }

    @Test
    void removeIndexEntry() {
        final WebClient mockWebClient = mock(WebClient.class);
        final WebClient.RequestBodyUriSpec mockRequestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        final WebClient.RequestHeadersSpec mockRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final WebClient.ResponseSpec mockResponseSpec = mock(WebClient.ResponseSpec.class);
        final WebClient.RequestHeadersUriSpec mockRequestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);

        when(contentfulProperties.getSpaceId()).thenReturn("Space");
        when(contentfulProperties.getEnvironmentId()).thenReturn("environment");
        when(contentfulProperties.getAccessToken()).thenReturn("accessToken");
        when(contentfulProperties.getDeliveryAPIAccessToken()).thenReturn("deliveryAccessToken");

        when(webClientBuilder.build()).thenReturn(mockWebClient);

        when(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri(anyString())).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.headers(any())).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"system\":{\"id\":\"testId\"},\"environmentId\":\"master\",\"id\":\"testId\",\"published\":false,\"archived\":false}"));

        when(mockWebClient.method(HttpMethod.DELETE)).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("testUrl/testDomain/_doc/testId")).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.body(any(), eq(String.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.header("Content-Type", "application/json; UTF-8")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.header("Authorization", "Basic dGVzdFVzZXJuYW1lOnRlc3RQYXNzd29yZA==")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(void.class)).thenReturn(Mono.empty());

        openSearchService.removeIndexEntry(contentfulEntry);

        verify(webClientBuilder.build().get(), times(1))
                .uri("https://api.contentful.com/spaces/Space/environments/environment/entries/testId");

        verify(webClientBuilder.build().method(HttpMethod.DELETE), times(1))
                .uri("testUrl/testDomain/_doc/testId");
    }
}
