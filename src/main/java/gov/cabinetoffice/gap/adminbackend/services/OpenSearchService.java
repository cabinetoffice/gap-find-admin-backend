package gov.cabinetoffice.gap.adminbackend.services;

import com.contentful.java.cma.model.CMAEntry;
import gov.cabinetoffice.gap.adminbackend.config.ContentfulConfigProperties;
import gov.cabinetoffice.gap.adminbackend.config.OpenSearchConfig;
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
    private final ContentfulConfigProperties contentfulProperties;

    public void indexEntry(final CMAEntry contentfulEntry) {
        final String body = getContentfulAdvertAsJson(contentfulEntry.getId());
        webClientBuilder.build().put()
                .uri(createUrl(contentfulEntry))
                .body(Mono.just(body), String.class)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; " + StandardCharsets.UTF_8.name())
                .header(AUTHORIZATION, createAuthHeader())
                .retrieve()
                .bodyToMono(void.class)
                .doOnError(e -> log.error("Failed to create an index entry for ad " + contentfulEntry.getId() + "in open search: {}", e.getMessage()))
                .block();
    }

    public void removeIndexEntry(final CMAEntry contentfulEntry) {
        final String body = getContentfulAdvertAsJson(contentfulEntry.getId());
        webClientBuilder.build().method(HttpMethod.DELETE)
                .uri(createUrl(contentfulEntry))
                .body(Mono.just(body), String.class)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + "; " + StandardCharsets.UTF_8.name())
                .header(AUTHORIZATION, createAuthHeader())
                .retrieve()
                .bodyToMono(void.class)
                .doOnError(e -> log.error("Failed to delete an index entry for ad " + contentfulEntry.getId() + "in open search: {}", e.getMessage()))
                .block();
    }

    private String createUrl(final CMAEntry contentfulEntry) {
        return openSearchConfig.getUrl() + "/" + openSearchConfig.getDomain() + "/_doc/" + contentfulEntry.getId();
    }

    private String createAuthHeader() {
        final String auth = openSearchConfig.getUsername() + ":" + openSearchConfig.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    private String getContentfulAdvertAsJson(String entryId) {
        final String contentfulUrl = String.format(
                "https://api.contentful.com/spaces/%1$s/environments/%2$s/entries/%3$s",
                contentfulProperties.getSpaceId(),
                contentfulProperties.getEnvironmentId(),
                entryId
        );

        return webClientBuilder.build()
                .get()
                .uri(contentfulUrl)
                .headers(h ->
                    h.set("Authorization", String.format("Bearer %s", contentfulProperties.getAccessToken()))
                )
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    log.error("Contentful response -------------------");
                    log.error(response.statusCode().toString());
                    log.error(response.bodyToMono(String.class).toString());
                    log.error("End Contentful response ---------------");

                    return Mono.empty();
                })
                .bodyToMono(String.class)
                .doOnError(exception ->
                    log.error(
                            "getContentfulAdvertAsJson failed on GET to {}, with message: {}",
                            contentfulUrl,
                            exception
                    )
                )
                .block();
    }
}
