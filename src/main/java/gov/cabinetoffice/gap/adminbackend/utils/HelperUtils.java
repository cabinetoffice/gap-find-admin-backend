package gov.cabinetoffice.gap.adminbackend.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
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
     * ie. "newScheme.name" -> "name"
     */
    public static String getSessionObjectFieldName(String key) {
        return key.split("\\.")[1];
    }

    public static AdminSession getAdminSessionForAuthenticatedUser() {
        return (AdminSession) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static String getJwtFromCookies(final HttpServletRequest request, final String userServiceCookieName) {
        final Cookie[] cookies = request.getCookies();
        final Cookie userServiceToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(userServiceCookieName)).findFirst()
                .orElseThrow(UnauthorizedException::new);
        return userServiceToken.getValue();
    }

    public static String encryptSecret(String secret, String publicKey) {

        try {
            final byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            final PublicKey rsaPublicKey = keyFactory.generatePublic(keySpec);
            final Cipher encryptCipher = Cipher.getInstance("RSA");

            encryptCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            final byte[] cipherText = encryptCipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while encrypting the secret " + e);
        }
    }
}
