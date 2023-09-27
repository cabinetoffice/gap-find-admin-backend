package gov.cabinetoffice.gap.adminbackend.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

public class HelperUtils {

    /**
     * Used to generate a safely encoded URL, incl. any query params This method will
     * auto-remove any query params where the value is null, to prevent empty request
     * params being added to the URL
     * @param hostUrl the host name
     * @param path the path to the requested resource
     * @param queryParams a map of query params
     * @return an encoded URL
     */
    public static String buildUrl(String hostUrl, String path, MultiValueMap<String, String> queryParams) {
        if (hostUrl == null || path == null) {
            throw new IllegalArgumentException("hostUrl and path cannot be null");
        }
        if (queryParams != null) {
            queryParams.values().removeIf(Objects::isNull);
        }

        return UriComponentsBuilder.newInstance().scheme("https").host(hostUrl).path(path).queryParams(queryParams)// if
                                                                                                                   // queryParams
                                                                                                                   // is
                                                                                                                   // null,
                                                                                                                   // nothing
                                                                                                                   // is
                                                                                                                   // added
                .build().toUriString();
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

            // so null values aren't included in the json
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String asJsonStringWithNulls(final Object obj) {
        try {
            final ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
            return mapper.writeValueAsString(obj);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Splits the session object value by the dot, and returns the section after the dot
     * i.e. "newScheme.name" -> "name"
     */
    public static String getSessionObjectFieldName(String key) {
        return key.split("\\.")[1];
    }

    public static AdminSession getAdminSessionForAuthenticatedUser() {
        return (AdminSession) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
