package gov.cabinetoffice.gap.adminbackend.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.exceptions.UnauthorizedException;
import gov.cabinetoffice.gap.adminbackend.models.JwtPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
class JwtServiceTest {

    @Mock
    private UserServiceConfig userServiceConfig;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    @InjectMocks
    private JwtService jwtService;

    private String encodedJwt;

    @Nested
    class validJWTTests {

        @BeforeEach
        void beforeEach() {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                KeyPair keyPair = keyPairGenerator.genKeyPair();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date tomorrowDate = calendar.getTime();

                encodedJwt = JWT.create().withExpiresAt(tomorrowDate).withIssuer("TEST_DOMAIN")
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
        void getPayloadFromJwt_HappyPathTest() {
            JwtPayload payloadFromJwt = jwtService.getPayloadFromJwt(JWT.decode(encodedJwt));
            assertThat(payloadFromJwt.getDepartmentName()).isEqualTo("Cabinet Office");
            assertThat(payloadFromJwt.getGivenName()).isEqualTo("Test");
            assertThat(payloadFromJwt.getFamilyName()).isEqualTo("User");
            assertThat(payloadFromJwt.getEmailAddress()).isEqualTo("test.user@and.digital");
            assertThat(payloadFromJwt.getSub()).isEqualTo("106b1a34-cd3a-45d7-924f-beedc33acc70");
        }

    }

    @Nested
    class JwtTests {

        @Test
        void verifyToken_ReturnsTrue() {
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Boolean.class)))
                    .thenReturn(ResponseEntity.of(Optional.of(Boolean.TRUE)));

            try (MockedStatic<JWT> mockedJwt = mockStatic(JWT.class)) {
                DecodedJWT decodedJWT = mock(DecodedJWT.class);
                mockedJwt.when(() -> JWT.decode("testToken")).thenReturn(decodedJWT);

                final DecodedJWT response = jwtService.verifyToken("testToken");

                assertThat(response).isEqualTo(decodedJWT);
            }
        }

        @Test
        void verifyToken_ExpiredJwtTest() {
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Boolean.class)))
                    .thenReturn(ResponseEntity.of(Optional.of(Boolean.FALSE)));

            assertThatThrownBy(() -> jwtService.verifyToken(encodedJwt)).isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Token is not valid");
        }

    }

}
