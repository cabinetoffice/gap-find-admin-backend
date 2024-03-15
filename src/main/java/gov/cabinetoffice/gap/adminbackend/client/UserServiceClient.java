package gov.cabinetoffice.gap.adminbackend.client;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.user.UserDto;
import gov.cabinetoffice.gap.adminbackend.exceptions.InvalidBodyException;
import gov.cabinetoffice.gap.adminbackend.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

import static gov.cabinetoffice.gap.adminbackend.utils.HelperUtils.encryptSecret;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final UserServiceConfig userServiceConfig;

    private final RestTemplate restTemplate;

    public UserDto getUserForSub(String sub) {
        final String url = userServiceConfig.getDomain() + "/user?userSub={userSub}";
        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Authorization", encryptSecret(userServiceConfig.getSecret(),userServiceConfig.getPublicKey()));
        final HttpEntity<String> requestEntity = new HttpEntity<>(null, requestHeaders);
        final Map<String, String> params = Collections.singletonMap("userSub", sub);
        ResponseEntity<UserDto> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, UserDto.class, params);
            if (response.getBody() != null) {
                return response.getBody();
            }
            else {
                throw new InvalidBodyException("Null body from " + url + "where sub is : " + sub + " in user service");
            }
        }
        catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new UserNotFoundException("User not found for sub " + sub + " in user service");
            }
            else {
                throw e;
            }
        }

    }

}
