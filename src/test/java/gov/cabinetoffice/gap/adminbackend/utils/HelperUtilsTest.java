package gov.cabinetoffice.gap.adminbackend.utils;

import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class HelperUtilsTest {

    @Mock
    private Authentication auth;

    @Mock
    private SecurityContext context;

    @Test
    void buildUrlWithNoParamsTest() {
        String builtUrl = HelperUtils.buildUrl("testHost", "testPath", null);
        assertThat(builtUrl).isEqualTo("https://testHost/testPath");
    }

    @Test
    void buildUrlWithWithMultipleParamsTest() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("param1", "value1");
        queryParams.add("param2", "value2");
        queryParams.add("param3", "value3");

        String builtUrl = HelperUtils.buildUrl("testHost", "testPath", queryParams);
        assertThat(builtUrl).isEqualTo("https://testHost/testPath?param1=value1&param2=value2&param3=value3");
    }

    @Test
    void buildUrlWithNullHostUrlAndPathTest() {
        assertThatThrownBy(() -> HelperUtils.buildUrl(null, "testPath", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HelperUtils.buildUrl("hostUrl", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parseJsonExpectedHappyTest() {
        String schemeJson = HelperUtils.asJsonString(SCHEME_DTO_EXAMPLE);
        assertThat(schemeJson).isEqualTo("{\"schemeId\":" + SAMPLE_SCHEME_ID + ",\"funderId\":" + SAMPLE_ORGANISATION_ID
                + ",\"name\":\"" + SAMPLE_SCHEME_NAME + "\",\"ggisReference\":\"" + SAMPLE_GGIS_REFERENCE
                + "\",\"contactEmail\":\"" + SAMPLE_SCHEME_CONTACT + "\",\"lastUpdatedByADeletedUser\":false}");
    }

    @Test
    void isAnonymousSession_ReturnsTrueIfAnonymousSession() {

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            final GrantedAuthority role = new SimpleGrantedAuthority("ROLE_ANONYMOUS");

            when(context.getAuthentication())
                    .thenReturn(auth);

            when(auth.getAuthorities())
                    .thenReturn((Collection) List.of(role));

            assertThat(HelperUtils.isAnonymousSession()).isTrue();
        }
    }

    @Test
    void isAnonymousSession_ReturnsFalseIfIdNotAnonymousSession() {

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(context);

            final GrantedAuthority role = new SimpleGrantedAuthority("ROLE_ADMIN");

            when(context.getAuthentication())
                    .thenReturn(auth);

            when(auth.getAuthorities())
                    .thenReturn((Collection) List.of(role));

            assertThat(HelperUtils.isAnonymousSession()).isFalse();
        }
    }

}