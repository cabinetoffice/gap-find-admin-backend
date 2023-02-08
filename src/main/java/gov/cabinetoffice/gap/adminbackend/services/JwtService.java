package gov.cabinetoffice.gap.adminbackend.services;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.config.CognitoConfigProperties;
import gov.cabinetoffice.gap.adminbackend.exceptions.InvalidJwtException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.ColaJwtPayload;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * The methods from this class are shamelessly taken from the applicant-backend with some
 * minor modifications
 */
@RequiredArgsConstructor
@Service
public class JwtService {

    private final CognitoConfigProperties cognitoProps;

    public DecodedJWT verifyToken(String jwt) {
        if (jwt.length() <= 0) {
            throw new InvalidJwtException("No Jwt has been passed in the request");
        }
        DecodedJWT decodedJWT = JWT.decode(jwt);

        boolean isNotExpired = Instant.now().isBefore(decodedJWT.getExpiresAt().toInstant());
        boolean isExpectedIssuer = decodedJWT.getIssuer().equals(this.cognitoProps.getDomain());
        boolean isExpectedAud = decodedJWT.getAudience().get(0).equals(this.cognitoProps.getAppClientId());
        if (!isExpectedAud || !isExpectedIssuer || !isNotExpired) {
            throw new UnauthorizedException("Token is not valid");
        }

        verifyJwtSignature(this.cognitoProps.getDomain(), decodedJWT);
        return decodedJWT;
    }

    @SneakyThrows
    public void verifyJwtSignature(String domain, DecodedJWT decodedJWT) {
        JwkProvider provider = getProvider(domain);
        Jwk jwk = provider.get(decodedJWT.getKeyId());
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
        algorithm.verify(decodedJWT);
    }

    // moved this to a separate method to aid testing/mocking
    public JwkProvider getProvider(String domain) {
        return new UrlJwkProvider(domain);
    }

    public ColaJwtPayload getColaPayloadFromJwt(DecodedJWT decodedJWT) throws IllegalArgumentException {

        UUID cognitoSubscription = UUID.fromString(decodedJWT.getSubject());
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

        return ColaJwtPayload.builder().sub(cognitoSubscription).givenName(givenName).familyName(familyName)
                .departmentName(deptName).emailAddress(emailAddress).build();
    }

}
