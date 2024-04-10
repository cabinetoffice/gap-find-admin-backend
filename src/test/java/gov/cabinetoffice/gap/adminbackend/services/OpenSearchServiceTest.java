package gov.cabinetoffice.gap.adminbackend.services;

import com.contentful.java.cma.model.CMAEntry;
import com.contentful.java.cma.model.CMASystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.config.OpenSearchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

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
    private ObjectMapper objectMapper;

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
        final WebClient.ResponseSpec mockResponseSpec = mock(WebClient.ResponseSpec.class);
        final JsonNode mockJsonNode = mock(JsonNode.class);

        when(objectMapper.valueToTree(any())).thenReturn(mockJsonNode);
        when(mockJsonNode.toString()).thenReturn("{\"system\":{\"id\":\"testId\"},\"environmentId\":\"master\",\"id\":\"testId\",\"published\":false,\"archived\":false}");
        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("testUrl/testDomain/_doc/testId")).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.body(any(), eq(String.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.header("Content-Type", "application/json; UTF-8")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.header("Authorization", "Basic dGVzdFVzZXJuYW1lOnRlc3RQYXNzd29yZA==")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(void.class)).thenReturn(Mono.empty());

        openSearchService.indexEntry(contentfulEntry);

    }

    @Test
    void removeIndexEntry() {
        final WebClient mockWebClient = mock(WebClient.class);
        final WebClient.RequestBodyUriSpec mockRequestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        final WebClient.RequestHeadersSpec mockRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final WebClient.ResponseSpec mockResponseSpec = mock(WebClient.ResponseSpec.class);
        final JsonNode mockJsonNode = mock(JsonNode.class);

        when(objectMapper.valueToTree(any())).thenReturn(mockJsonNode);
        when(mockJsonNode.toString()).thenReturn("{\"system\":{\"id\":\"testId\"},\"environmentId\":\"master\",\"id\":\"testId\",\"published\":false,\"archived\":false}");
        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.method(HttpMethod.DELETE)).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("testUrl/testDomain/_doc/testId")).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.body(any(), eq(String.class))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.header("Content-Type", "application/json; UTF-8")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.header("Authorization", "Basic dGVzdFVzZXJuYW1lOnRlc3RQYXNzd29yZA==")).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(void.class)).thenReturn(Mono.empty());

        openSearchService.removeIndexEntry(contentfulEntry);

    }
}
