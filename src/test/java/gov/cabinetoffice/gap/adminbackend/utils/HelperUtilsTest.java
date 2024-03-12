package gov.cabinetoffice.gap.adminbackend.utils;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HelperUtilsTest {

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

}