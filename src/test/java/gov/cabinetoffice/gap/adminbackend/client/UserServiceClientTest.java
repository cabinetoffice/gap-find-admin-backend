package gov.cabinetoffice.gap.adminbackend.client;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserDto;
import gov.cabinetoffice.gap.adminbackend.exceptions.InvalidBodyException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

    @Mock
    private UserServiceConfig userServiceConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserServiceClient userServiceClient;

    @Test
    void getUserForSub_Successful() {
        final String sub = "d522c5ac-dea1-4d79-ba07-62d5c7203da1";
        final String domain = "domain";
        final String url = domain + "/user?userSub={userSub}";
        final UserDto expectedUserDto = UserDto.builder().emailAddress("email@email.com").build();

        when(userServiceConfig.getDomain()).thenReturn(domain);
        when(userServiceConfig.getSecret()).thenReturn("secret");
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserDto.class), anyMap()))
                .thenReturn(new ResponseEntity(expectedUserDto, HttpStatus.OK));

        final UserDto result = userServiceClient.getUserForSub(sub);

        assertThat(result.getEmailAddress()).isEqualTo(expectedUserDto.getEmailAddress());
    }

    @Test
    void getUserForSub_EmptyBody() {
        final String sub = "d522c5ac-dea1-4d79-ba07-62d5c7203da1";
        final String domain = "domain";
        final String expectedUrl = domain + "/user?userSub={userSub}";

        when(userServiceConfig.getDomain()).thenReturn(domain);
        when(userServiceConfig.getSecret()).thenReturn("secret");
        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class),
                any(Map.class))).thenReturn(new ResponseEntity(HttpStatus.OK));

        assertThrows(InvalidBodyException.class, () -> userServiceClient.getUserForSub(sub),
                "Null body from " + expectedUrl + "where sub is : " + sub + " in user service");
    }

    @Test
    void getUserForSub_404() {
        final String sub = "d522c5ac-dea1-4d79-ba07-62d5c7203da1";
        final String domain = "domain";
        final String expectedUrl = domain + "/user?userSub={userSub}";

        when(userServiceConfig.getDomain()).thenReturn(domain);
        when(userServiceConfig.getSecret()).thenReturn("secret");
        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class),
                any(Map.class))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(UserNotFoundException.class, () -> userServiceClient.getUserForSub(sub),
                "User not found for sub " + sub + " in user service");
    }

    @Test
    void getUserForSub_Exception() {
        final String sub = "d522c5ac-dea1-4d79-ba07-62d5c7203da1";
        final String domain = "domain";
        final String expectedUrl = domain + "/user?userSub={userSub}";

        when(userServiceConfig.getDomain()).thenReturn(domain);
        when(userServiceConfig.getSecret()).thenReturn("secret");
        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class),
                any(Map.class))).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> userServiceClient.getUserForSub(sub));
    }

    @Test
    void getUserForSub_UnexpectedError() {
        final String sub = "d522c5ac-dea1-4d79-ba07-62d5c7203da1";
        final String domain = "domain";
        final String url = "domain/user?userSub={userSub}";

        when(userServiceConfig.getDomain()).thenReturn(domain);
        when(userServiceConfig.getSecret()).thenReturn("secret");
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserDto.class), anyMap()))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpClientErrorException.class, () -> userServiceClient.getUserForSub(sub));
    }

}
