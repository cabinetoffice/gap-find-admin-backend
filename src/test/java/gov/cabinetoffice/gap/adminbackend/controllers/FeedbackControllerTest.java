package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.annotations.WithAdminSession;
import gov.cabinetoffice.gap.adminbackend.services.FeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationFormController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { FeedbackController.class })
@WithAdminSession
public class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedbackService feedbackService;

    @Test
    void submitFullFeedback() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("satisfaction", "5");
        params.add("comment", "I am satisfied!");
        params.add("journey", "advert");
        this.mockMvc
                .perform(post("/feedback/add").params(params)
                        .header(HttpHeaders.AUTHORIZATION, "JWT"))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void submitScoreOnly() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("satisfaction", "5");
        params.add("journey", "advert");
        this.mockMvc.perform(post("/feedback/add").params(params))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void submitCommentOnly() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("comment", "I am so satisfied!");
        params.add("journey", "advert");
        this.mockMvc.perform(post("/feedback/add").params(params))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void submitTooGreatSatisfactionScore() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("satisfaction", "9");
        params.add("journey", "advert");
        this.mockMvc.perform(post("/feedback/add").params(params))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void submitTooSmallSatisfactionScore() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("satisfaction", "-1");
        params.add("journey", "advert");
        this.mockMvc.perform(post("/feedback/add").params(params))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void submitEmptyCommentOnly() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("comment", "");
        params.add("journey", "advert");
        this.mockMvc.perform(post("/feedback/add").params(params))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void submitJourneyOnly() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("journey", "advert");
        this.mockMvc.perform(post("/feedback/add").params(params))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void submitNothing() throws Exception {
        this.mockMvc.perform(post("/feedback/add"))
                .andExpect(status().isBadRequest()).andReturn();
    }
}
