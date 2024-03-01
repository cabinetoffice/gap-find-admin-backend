package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.SchemeEditorService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.Validator;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchemeEditorController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SchemeEditorController.class })
class SchemeEditorControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchemeEditorService schemeEditorService;

    @MockBean
    private UserServiceConfig userServiceConfig;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @MockBean
    private Validator validator;

    @Nested
    class deleteSchemeEditor {
        @Test
        void succeeds() throws Exception {
            mockMvc.perform(delete("/schemes/1/editors/2")).andExpect(status().isOk());
            verify(schemeEditorService).deleteEditor(1, 2);
        }
    }
}
