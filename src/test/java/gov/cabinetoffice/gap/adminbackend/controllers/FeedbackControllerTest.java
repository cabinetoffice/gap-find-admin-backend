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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    void submitFeedback() throws Exception {
        this.mockMvc
                .perform(post("/feedback/add").param("comment", "test").param("satisfaction", "1")
                        .header(HttpHeaders.AUTHORIZATION, "JWT"))
                .andExpect(status().isOk()).andExpect(content().string("")).andReturn();
    }

    @Test
    void submitScoreOnly() throws Exception {
        this.mockMvc.perform(post("/feedback/add").param("satisfaction", "3").header(HttpHeaders.AUTHORIZATION, "JWT"))
                .andExpect(status().isOk()).andExpect(content().string("")).andReturn();
    }

    @Test
    void submitCommentOnly() throws Exception {
        this.mockMvc.perform(post("/feedback/add").param("comment", "test").header(HttpHeaders.AUTHORIZATION, "JWT"))
                .andExpect(status().isOk()).andExpect(content().string("")).andReturn();
    }

    @Test
    void submitNothing() throws Exception {
        this.mockMvc.perform(post("/feedback/add").header(HttpHeaders.AUTHORIZATION, "JWT"))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void submitFeedbackAdminJourney() throws Exception {

    }

}
