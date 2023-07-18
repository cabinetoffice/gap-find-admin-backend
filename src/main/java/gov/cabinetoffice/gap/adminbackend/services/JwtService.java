package gov.cabinetoffice.gap.adminbackend.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.exceptions.InvalidJwtException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayloadV2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * The methods from this class are shamelessly taken from the applicant-backend with some
 * minor modifications
 */
@RequiredArgsConstructor
@Service
public class JwtService {

    private final UserServiceConfig userServiceConfig;

    private final RestTemplate restTemplate;

    public DecodedJWT verifyToken(final String jwt) {
        final String url = userServiceConfig.getDomain() + "/is-user-logged-in";
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Cookie", userServiceConfig.getCookieName() + "=" + jwt);
        final HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        final ResponseEntity<Boolean> isJwtValid = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
                Boolean.class);
        if (!isJwtValid.getBody()) {
            throw new UnauthorizedException("Token is not valid");
        }
        return JWT.decode(jwt);
    }

    public JwtPayload getPayloadFromJwt(DecodedJWT decodedJWT) throws IllegalArgumentException {
        String sub = decodedJWT.getSubject();
        String givenName = decodedJWT.getClaim("given_name").asString();
        String familyName = decodedJWT.getClaim("family_name").asString();
        String[] jwtFeatures = decodedJWT.getClaims().get("custom:features").asString().split(",");
        String deptName = Arrays.stream(jwtFeatures).filter(feature -> feature.startsWith("dept=")).findFirst()
                .orElseThrow(() -> new InvalidJwtException("JWT is missing expected properties"));
        deptName = deptName.split("=")[1];
        String emailAddress = decodedJWT.getClaim("email").asString();

        if (givenName == null || familyName == null || emailAddress == null) {
            throw new InvalidJwtException("JWT is missing expected properties");
        }

        return JwtPayload.builder().sub(sub).givenName(givenName).familyName(familyName).departmentName(deptName)
                .emailAddress(emailAddress).build();
    }

    public JwtPayloadV2 getPayloadFromJwtV2(DecodedJWT decodedJWT) throws IllegalArgumentException {
        String sub = decodedJWT.getSubject();
        String roles = decodedJWT.getClaim("roles").asString();
        String department = decodedJWT.getClaim("department").asString();
        String emailAddress = decodedJWT.getClaim("email").asString();
        String iss = decodedJWT.getClaim("iss").asString();
        String aud = decodedJWT.getClaim("aud").asString();
        int exp = decodedJWT.getClaim("exp").asInt();
        int iat = decodedJWT.getClaim("iat").asInt();


        if (department == null || roles == null || emailAddress == null) {
            throw new InvalidJwtException("JWT is missing expected properties");
        }

        return JwtPayloadV2.builder()
                .sub(sub)
                .roles(roles)
                .emailAddress(emailAddress)
                .department(department)
                .iss(iss)
                .aud(aud)
                .exp(exp)
                .iat(iat).build();
    }

}
