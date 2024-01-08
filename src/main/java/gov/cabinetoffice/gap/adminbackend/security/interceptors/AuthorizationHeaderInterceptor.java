package gov.cabinetoffice.gap.adminbackend.security.interceptors;

import gov.cabinetoffice.gap.adminbackend.annotations.SpotlightPublisherHeaderValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

//this is needed to "authenticate" all the call from the spotlightPublisherLambda
@Log4j2
public class AuthorizationHeaderInterceptor implements HandlerInterceptor {

    private final String expectedAuthorizationValue;

    public AuthorizationHeaderInterceptor(String expectedAuthorizationValue) {
        this.expectedAuthorizationValue = expectedAuthorizationValue;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("Intercepting request: " + request.getRequestURI());
        if (handler instanceof final HandlerMethod handlerMethod) {
            final Method method = handlerMethod.getMethod();

            SpotlightPublisherHeaderValidator annotation = method
                    .getAnnotation(SpotlightPublisherHeaderValidator.class);
            log.info("SpotlightPublisherHeaderValidator: " + annotation);

            if (annotation != null) {
                final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authorizationHeader == null || !authorizationHeader.equals(expectedAuthorizationValue)) {
                    // TODO do we want this log?? is it safe?? shall we change what it
                    // says??
                    log.info("Authorization Header Value: " + authorizationHeader
                            + " does not match the expected value");

                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }
        }

        return true;
    }

}
