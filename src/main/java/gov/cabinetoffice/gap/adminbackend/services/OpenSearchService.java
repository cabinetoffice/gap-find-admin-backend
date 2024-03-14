package gov.cabinetoffice.gap.adminbackend.services;

import com.contentful.java.cma.model.CMAEntry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.config.OpenSearchConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchService {

    private final WebClient.Builder webClientBuilder;
    private final OpenSearchConfig openSearchConfig;

    public void indexEntry(final CMAEntry contentfulEntry) {
        final String body = contentfulEntryToJsonString(contentfulEntry);
        webClientBuilder.build().put()
                .uri(createUrl(contentfulEntry))
                .body(BodyInserters.fromValue(body))
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; " + StandardCharsets.UTF_8.name())
                .header(AUTHORIZATION, createAuthHeader())
                .retrieve()
                .bodyToMono(void.class)
                .block();
    }

    public void removeIndexEntry(final CMAEntry contentfulEntry) {
        final String body = contentfulEntryToJsonString(contentfulEntry.getSystem());
        webClientBuilder.build().method(HttpMethod.DELETE)
                .uri(createUrl(contentfulEntry))
                .body(BodyInserters.fromValue(body))
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; " + StandardCharsets.UTF_8.name())
                .header(AUTHORIZATION, createAuthHeader())
                .retrieve()
                .bodyToMono(void.class)
                .block();
    }

    private String createUrl(final CMAEntry contentfulEntry) {
        return openSearchConfig.getUrl() + "/" + openSearchConfig.getDomain() + "/_doc/" + contentfulEntry.getId();
    }

    private String createAuthHeader() {
        final String auth = openSearchConfig.getUsername() + ":" + openSearchConfig.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    private String contentfulEntryToJsonString(final Object contentfulEntry) {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final String contentfulObject = objectMapper.valueToTree(contentfulEntry).toString();
        return contentfulObject.replace("\"system\":", "\"sys\":");
    }
}
