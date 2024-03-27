package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.dtos.errors.FieldErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.models.ValidationError;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.HashMap;

import static gov.cabinetoffice.gap.adminbackend.testdata.SessionTestData.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SessionsController.class, ControllerExceptionHandler.class })
class SessionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @Test
    void testSuccessfullyAddingToSession() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", "testKey");
        params.add("value", "testValue");

        this.mockMvc.perform(patch("/sessions/add").params(params)).andExpect(status().isOk())
                .andExpect(request().sessionAttribute("testKey", "testValue"));
    }

    @Test
    void testFailAddingToInvalidValueToSession() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", "newScheme.grantName");
        params.add("value", "");
        FieldErrorsDTO fieldErrorsDTO = new FieldErrorsDTO(
                Collections.singletonList(new ValidationError("grantName", "Enter the name of your grant")));

        this.mockMvc.perform(patch("/sessions/add").params(params)).andExpect(status().isBadRequest())
                .andExpect(content().json(HelperUtils.asJsonString(fieldErrorsDTO)));
    }

    @Test
    void testAddingToSessionWithoutParams() throws Exception {
        this.mockMvc.perform(patch("/sessions/add")).andExpect(status().isBadRequest());
    }

    @Test
    void testGettingDataFromSession() throws Exception {
        this.mockMvc.perform(get("/sessions/testKey").sessionAttr("testKey", "testValue")).andExpect(status().isOk())
                .andExpect(content().json("{ sessionValue : testValue }"))
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testGettingNonExistentDataFromSession() throws Exception {
        this.mockMvc.perform(get("/sessions/testNonExistingKey")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void testGettingNonStringDataFromSession() throws Exception {
        this.mockMvc.perform(get("/sessions/testNonStringKey").sessionAttr("testNonStringKey", Boolean.TRUE))
                .andExpect(status().isUnsupportedMediaType()).andExpect(content().string(""));
    }

    @Test
    void returnObjectFromSessionTest() throws Exception {
        HashMap<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("newScheme.name", "Sample Name");
        sessionAttributes.put("newScheme.description", "Sample Description");

        this.mockMvc.perform(get("/sessions/object/newScheme").sessionAttrs(sessionAttributes))
                .andExpect(status().isOk()).andExpect(content()
                        .json("{" + "\"name\": \"Sample Name\"," + "\"description\": \"Sample Description\"" + "}"));
    }

    @Test
    void returnObjectFromSessionNoMatchingKeyTest() throws Exception {
        this.mockMvc.perform(get("/sessions/object/newScheme")).andExpect(status().isNoContent());
    }

    @Test
    void returnObjectFromSessionNoMatchOnEnumTest() throws Exception {
        this.mockMvc.perform(get("/sessions/object/secretInfo")).andExpect(status().isBadRequest());
    }

    @Test
    void deleteObjectFromSessionHappyPathTest() throws Exception {
        HashMap<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("newScheme.name", "Sample Name");
        sessionAttributes.put("newScheme.description", "Sample Description");

        this.mockMvc.perform(delete("/sessions/object/newScheme").sessionAttrs(sessionAttributes))
                .andExpect(status().isOk());
    }

    @Test
    void deleteObjectFromSessionNotFoundTest() throws Exception {
        this.mockMvc.perform(delete("/sessions/object/newScheme")).andExpect(status().isNotFound());
    }

    @Test
    void testSuccessfullyBatchAddingToSession() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("objectKey", "newScheme");

        this.mockMvc
                .perform(patch("/sessions/batch-add").params(params).contentType(MediaType.APPLICATION_JSON)
                        .content(SESSION_BATCH_ADD_BODY_JSON))
                .andExpect(status().isOk()).andExpect(request().sessionAttribute("newScheme.grantName", "sampleSchemeName"))
                .andExpect(request().sessionAttribute("newScheme.ggisReference", "sampleSchemeGGiSReference"));
    }

    @Test
    void testFailBatchAddingToInvalidValueToSession() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("objectKey", "newScheme");

        this.mockMvc
                .perform(patch("/sessions/batch-add").params(params).contentType(MediaType.APPLICATION_JSON)
                        .content(SESSION_BATCH_ADD_INVALID_BODY_JSON))
                .andExpect(status().isBadRequest()).andExpect(content().json(SESSION_VALIDATION_ERRORS_BATCH_ADD_JSON));
    }

    @Test
    void testBatchAddingToSessionWithoutParams() throws Exception {
        this.mockMvc.perform(patch("/sessions/batch-add")).andExpect(status().isBadRequest());
    }

}
