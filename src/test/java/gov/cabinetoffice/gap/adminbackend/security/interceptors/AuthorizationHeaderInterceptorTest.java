package gov.cabinetoffice.gap.adminbackend.security.interceptors;

import gov.cabinetoffice.gap.adminbackend.annotations.SpotlightPublisherHeaderValidator;
import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@WithAdminSession
class AuthorizationHeaderInterceptorTest {

    private static final String EXPECTED_AUTHORIZATION_VALUE = "expectedToken";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @InjectMocks
    private AuthorizationHeaderInterceptor authorizationHeaderInterceptor;

    @Test
    void preHandleValidAuthorization() throws Exception {
        authorizationHeaderInterceptor = new AuthorizationHeaderInterceptor(EXPECTED_AUTHORIZATION_VALUE);
        when(handlerMethod.getMethod()).thenReturn(getClass().getMethod("annotatedTestMethod"));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(EXPECTED_AUTHORIZATION_VALUE);

        boolean result = authorizationHeaderInterceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
    }

    @Test
    void preHandleNullAuthorizationHeader() throws Exception {
        when(handlerMethod.getMethod()).thenReturn(getClass().getMethod("annotatedTestMethod"));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        boolean result = authorizationHeaderInterceptor.preHandle(request, response, handlerMethod);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertFalse(result);
    }

    @Test
    void preHandleInvalidAuthorizationHeader() throws Exception {
        String invalidAuthorizationValue = "invalidToken";
        when(handlerMethod.getMethod()).thenReturn(getClass().getMethod("annotatedTestMethod"));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(invalidAuthorizationValue);

        boolean result = authorizationHeaderInterceptor.preHandle(request, response, handlerMethod);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertFalse(result);
    }

    @Test
    void preHandleValidAuthorizationForNonAnnotatedMethods() throws Exception {
        authorizationHeaderInterceptor = new AuthorizationHeaderInterceptor(EXPECTED_AUTHORIZATION_VALUE);
        when(handlerMethod.getMethod()).thenReturn(getClass().getMethod("nonAnnotatedTestMethod"));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(EXPECTED_AUTHORIZATION_VALUE);

        boolean result = authorizationHeaderInterceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
    }

    // Test method to provide a valid HandlerMethod for testing
    @SpotlightPublisherHeaderValidator
    public void annotatedTestMethod() {
        // This method is just a placeholder for testing HandlerMethod
    }

    // Test method to provide a NON valid HandlerMethod for testing
    public void nonAnnotatedTestMethod() {
        // This method is just a placeholder for testing HandlerMethod
    }

}