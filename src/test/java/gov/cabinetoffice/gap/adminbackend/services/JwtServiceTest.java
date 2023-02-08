package gov.cabinetoffice.gap.adminbackend.services;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import gov.cabinetoffice.gap.adminbackend.config.CognitoConfigProperties;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.ColaJwtPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
class JwtServiceTest {

    @Mock
    private CognitoConfigProperties cognitoProps;

    @Spy
    @InjectMocks
    private JwtService jwtService;

    private final String DOMAIN = "TEST_DOMAIN";

    private final String APP_CLIENT_ID = "TEST_APP_CLIENT_ID";

    private KeyPair keyPair;

    private String encodedJwt;

    @Nested
    class validJWTTests {

        @BeforeEach
        void beforeEach() {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                keyPair = keyPairGenerator.genKeyPair();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date tomorrowDate = calendar.getTime();

                encodedJwt = JWT.create().withExpiresAt(tomorrowDate).withAudience(APP_CLIENT_ID).withIssuer(DOMAIN)
                        .withKeyId(keyPair.getPublic().toString()).withSubject("106b1a34-cd3a-45d7-924f-beedc33acc70")
                        .withClaim("custom:features", "dept=Cabinet Office,user=administrator,user=ordinary_user")
                        .withClaim("given_name", "Test").withClaim("family_name", "User")
                        .withClaim("email", "test.user@and.digital")
                        .sign(Algorithm.RSA256(null, (RSAPrivateKey) keyPair.getPrivate()));
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

        }

        @Test
        void verifyToken_HappyPathTest() throws NoSuchAlgorithmException {
            when(cognitoProps.getDomain()).thenReturn(DOMAIN);
            when(cognitoProps.getAppClientId()).thenReturn(APP_CLIENT_ID);

            doNothing().when(jwtService).verifyJwtSignature(any(), any());
            jwtService.verifyToken(encodedJwt);
        }

        @Test
        void verifyJWTSignature_HappyPathTest() throws JwkException {
            JwkProvider jwkProvider = mock(JwkProvider.class);
            Jwk jwk = mock(Jwk.class);

            when(jwtService.getProvider(DOMAIN)).thenReturn(jwkProvider);
            when(jwkProvider.get(any())).thenReturn(jwk);
            when(jwk.getPublicKey()).thenReturn(keyPair.getPublic());

            jwtService.verifyJwtSignature(DOMAIN, JWT.decode(encodedJwt));
        }

        @Test
        void getColaPayloadFromJwt_HappyPathTest() {
            ColaJwtPayload colaPayloadFromJwt = jwtService.getColaPayloadFromJwt(JWT.decode(encodedJwt));
            assertThat(colaPayloadFromJwt.getDepartmentName()).isEqualTo("Cabinet Office");
            assertThat(colaPayloadFromJwt.getGivenName()).isEqualTo("Test");
            assertThat(colaPayloadFromJwt.getFamilyName()).isEqualTo("User");
            assertThat(colaPayloadFromJwt.getEmailAddress()).isEqualTo("test.user@and.digital");
            assertThat(colaPayloadFromJwt.getSub()).isEqualTo(UUID.fromString("106b1a34-cd3a-45d7-924f-beedc33acc70"));
        }

    }

    @Nested
    class invalidJwtTests {

        @Test
        void verifyToken_ExpiredJwtTest() {
            String encodedJwt;

            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair = keyPairGenerator.genKeyPair();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -1); // expired
                Date yesterdayDate = calendar.getTime();

                encodedJwt = JWT.create().withExpiresAt(yesterdayDate).withAudience(APP_CLIENT_ID).withIssuer(DOMAIN)
                        .withKeyId(keyPair.getPublic().toString()).withSubject("106b1a34-cd3a-45d7-924f-beedc33acc70")
                        .withClaim("custom:features", "dept=Cabinet Office,user=administrator,user=ordinary_user")
                        .withClaim("given_name", "Test").withClaim("family_name", "User")
                        .withClaim("email", "test.user@and.digital")
                        .sign(Algorithm.RSA256(null, (RSAPrivateKey) keyPair.getPrivate()));
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            when(cognitoProps.getDomain()).thenReturn(DOMAIN);
            when(cognitoProps.getAppClientId()).thenReturn(APP_CLIENT_ID);

            assertThatThrownBy(() -> jwtService.verifyToken(encodedJwt)).isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Token is not valid");
        }

        @Test
        void verifyToken_InvalidAppClientIdTest() {
            String encodedJwt;

            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair = keyPairGenerator.genKeyPair();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 1); // expired
                Date tomorrowDate = calendar.getTime();

                encodedJwt = JWT.create().withExpiresAt(tomorrowDate).withAudience("WRONG_APP_CLIENT_ID")
                        .withIssuer(DOMAIN).withKeyId(keyPair.getPublic().toString())
                        .withSubject("106b1a34-cd3a-45d7-924f-beedc33acc70")
                        .withClaim("custom:features", "dept=Cabinet Office,user=administrator,user=ordinary_user")
                        .withClaim("given_name", "Test").withClaim("family_name", "User")
                        .withClaim("email", "test.user@and.digital")
                        .sign(Algorithm.RSA256(null, (RSAPrivateKey) keyPair.getPrivate()));
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            when(cognitoProps.getDomain()).thenReturn(DOMAIN);
            when(cognitoProps.getAppClientId()).thenReturn(APP_CLIENT_ID);

            assertThatThrownBy(() -> jwtService.verifyToken(encodedJwt)).isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Token is not valid");
        }

        @Test
        void verifyToken_InvalidDomainTest() {
            String encodedJwt;

            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair = keyPairGenerator.genKeyPair();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 1); // expired
                Date tomorrowDate = calendar.getTime();

                encodedJwt = JWT.create().withExpiresAt(tomorrowDate).withAudience(APP_CLIENT_ID)
                        .withIssuer("WRONG_DOMAIN").withKeyId(keyPair.getPublic().toString())
                        .withSubject("106b1a34-cd3a-45d7-924f-beedc33acc70")
                        .withClaim("custom:features", "dept=Cabinet Office,user=administrator,user=ordinary_user")
                        .withClaim("given_name", "Test").withClaim("family_name", "User")
                        .withClaim("email", "test.user@and.digital")
                        .sign(Algorithm.RSA256(null, (RSAPrivateKey) keyPair.getPrivate()));
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            when(cognitoProps.getDomain()).thenReturn(DOMAIN);
            when(cognitoProps.getAppClientId()).thenReturn(APP_CLIENT_ID);

            assertThatThrownBy(() -> jwtService.verifyToken(encodedJwt)).isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Token is not valid");
        }

        @Test
        void verifyJWTSignature_InvalidJWTTest() throws JwkException {
            String encodedJwt;
            KeyPair keyPair;

            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                keyPair = keyPairGenerator.genKeyPair();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -1); // expired
                Date yesterdayDate = calendar.getTime();

                encodedJwt = JWT.create().withExpiresAt(yesterdayDate).withAudience(APP_CLIENT_ID).withIssuer(DOMAIN)
                        .withKeyId(keyPair.getPublic().toString()).withSubject("106b1a34-cd3a-45d7-924f-beedc33acc70")
                        .withClaim("custom:features", "dept=Cabinet Office,user=administrator,user=ordinary_user")
                        .withClaim("given_name", "Test").withClaim("family_name", "User")
                        .withClaim("email", "test.user@and.digital")
                        .sign(Algorithm.RSA256(null, (RSAPrivateKey) keyPair.getPrivate()));
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            JwkProvider jwkProvider = mock(JwkProvider.class);
            Jwk jwk = mock(Jwk.class);

            when(jwtService.getProvider(DOMAIN)).thenReturn(jwkProvider);
            when(jwkProvider.get(any())).thenReturn(jwk);
            when(jwk.getPublicKey()).thenReturn(keyPair.getPublic());

            // this arbitrarily modifies the payload of the jwt
            // but the signature is untouched
            String[] splitJwt = encodedJwt.split("\\.");
            splitJwt[1] = splitJwt[1].replace("a", "b");
            String modifiedJwt = String.join(".", splitJwt);

            assertThatThrownBy(() -> jwtService.verifyJwtSignature(DOMAIN, JWT.decode(modifiedJwt)))
                    .isInstanceOf(SignatureVerificationException.class).hasMessage(
                            "The Token's Signature resulted invalid when verified using the Algorithm: SHA256withRSA");
        }

    }

}
