package gov.cabinetoffice.gap.adminbackend.services;

import com.contentful.java.cma.model.CMAEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.gap.adminbackend.config.OpenSearchConfig;
import gov.cabinetoffice.gap.adminbackend.exceptions.IndexingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchService {

    private final WebClient.Builder webClientBuilder;
    private final OpenSearchConfig openSearchConfig;
    private final ObjectMapper objectMapper;

    public void indexEntry(final CMAEntry contentfulEntry) {
        final String body = contentfulEntryToJsonString(contentfulEntry);
        webClientBuilder.build().put()
                .uri(createUrl(contentfulEntry))
                .body(Mono.just(body), String.class)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; " + StandardCharsets.UTF_8.name())
                .header(AUTHORIZATION, createAuthHeader())
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    log.info("Elastic search update json string: {}", body);
                    log.error("response code from open search: {}", clientResponse.statusCode());
                    log.error("response from open search: {}", clientResponse.bodyToMono(String.class));
                    throw new IndexingException("failed to add CMA entry with ID " + contentfulEntry.getId() + " to index");
                })
                .bodyToMono(void.class)
                .block();
    }

    public void removeIndexEntry(final CMAEntry contentfulEntry) {
        final String body = contentfulEntryToJsonString(contentfulEntry.getSystem());
        webClientBuilder.build().method(HttpMethod.DELETE)
                .uri(createUrl(contentfulEntry))
                .body(Mono.just(body), String.class)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; " + StandardCharsets.UTF_8.name())
                .header(AUTHORIZATION, createAuthHeader())
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    log.info("Elastic search delete json string: {}", body);
                    log.error("response code from open search: {}", clientResponse.statusCode());
                    log.error("response from open search: {}", clientResponse.bodyToMono(String.class));
                    throw new IndexingException("failed to remove CMA entry with ID " + contentfulEntry.getId() + " from index");
                })
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
        final String contentfulObject = objectMapper.valueToTree(contentfulEntry).toString();
        return contentfulObject.replace("\"system\":", "\"sys\":");
    }
}
