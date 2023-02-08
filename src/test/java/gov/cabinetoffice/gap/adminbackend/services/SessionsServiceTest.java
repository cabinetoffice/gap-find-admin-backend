package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class SessionsServiceTest {

    @InjectMocks
    private SessionsService sessionsService;

    @Test
    void retrieveObjectFromSession() {
        MockHttpSession mockHttpSession = new MockHttpSession();

        mockHttpSession.setAttribute("newScheme.name", "Sample Name");
        mockHttpSession.setAttribute("newScheme.description", "Sample Description");
        HashMap<String, String> sessionObject = sessionsService.retrieveObjectFromSession(SessionObjectEnum.newScheme,
                mockHttpSession);

        assertThat(!sessionObject.isEmpty()).isTrue();

        assertThat(sessionObject.get("name")).as("Name key should be populated with expected value")
                .isEqualTo("Sample Name");

        assertThat(sessionObject.get("description")).as("Description key should be populated with expected value")
                .isEqualTo("Sample Description");

    }

    @Test
    void deleteObjectFromSession() {
        MockHttpSession mockHttpSession = new MockHttpSession();

        mockHttpSession.setAttribute("newScheme.name", "Sample Name");
        mockHttpSession.setAttribute("newScheme.description", "Sample Description");

        assertThat(mockHttpSession.getAttribute("newScheme.name")).isEqualTo("Sample Name");
        assertThat(mockHttpSession.getAttribute("newScheme.description")).isEqualTo("Sample Description");

        boolean valuesFound = sessionsService.deleteObjectFromSession(SessionObjectEnum.newScheme, mockHttpSession);

        assertThat(valuesFound).isTrue();
        assertThat(mockHttpSession.getAttribute("newScheme.name")).isNull();
        assertThat(mockHttpSession.getAttribute("newScheme.description")).isNull();

    }

}