package gov.cabinetoffice.gap.adminbackend.controllers;

import gov.cabinetoffice.gap.adminbackend.config.UserServiceConfig;
import gov.cabinetoffice.gap.adminbackend.dtos.errors.GenericErrorDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePostDTO;
import gov.cabinetoffice.gap.adminbackend.exceptions.SchemeEntityException;
import gov.cabinetoffice.gap.adminbackend.mappers.ValidationErrorMapperImpl;
import gov.cabinetoffice.gap.adminbackend.services.ApplicationFormService;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.services.SchemeService;
import gov.cabinetoffice.gap.adminbackend.services.UserService;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpSession;
import java.util.Collections;

import static gov.cabinetoffice.gap.adminbackend.testdata.SchemeTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchemeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SchemeController.class, ControllerExceptionHandler.class })
class SchemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchemeService schemeService;

    @SpyBean
    private ValidationErrorMapperImpl validationErrorMapper;

    @MockBean
    private UserServiceConfig userServiceConfig;

    @MockBean
    private GrantAdvertService grantAdvertService;

    @MockBean
    private UserService userService;

    @MockBean
    private ApplicationFormService applicationFormService;

    @Test
    void testSuccessfullyGettingScheme() throws Exception {
        Mockito.when(this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID)).thenReturn(SCHEME_DTO_EXAMPLE);

        this.mockMvc.perform(get("/schemes/" + SAMPLE_SCHEME_ID)).andExpect(status().isOk())
                .andExpect(content().json(EXPECTED_SINGLE_SCHEME_JSON_RESPONSE));
    }

    @Test
    void testGettingSchemeThatDoesntExist() throws Exception {
        Mockito.when(this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID)).thenThrow(new EntityNotFoundException());

        this.mockMvc.perform(get("/schemes/" + SAMPLE_SCHEME_ID)).andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void getSchemeById_IllegalArgument() throws Exception {
        Mockito.when(this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID))
                .thenThrow(new IllegalArgumentException());

        this.mockMvc.perform(get("/schemes/" + SAMPLE_SCHEME_ID)).andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void getSchemeById_AccessDenied() throws Exception {
        Mockito.when(this.schemeService.getSchemeBySchemeId(SAMPLE_SCHEME_ID))
                .thenThrow(new AccessDeniedException("Access Denied"));

        this.mockMvc.perform(get("/schemes/" + SAMPLE_SCHEME_ID)).andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void updateSchemeData_SuccessfullyUpdatingScheme() throws Exception {
        Mockito.doNothing().when(this.schemeService).patchExistingScheme(SAMPLE_SCHEME_ID, SCHEME_PATCH_DTO_EXAMPLE);

        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_JSON))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
    }

    @Test
    void updateSchemeData_AttemptingToPatchSchemeWithEmptyProperties() throws Exception {

        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_EMPTY_PROPERTIES))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(HelperUtils.asJsonString(SCHEME_PATCH_BLANK_VALIDATION_ERROR_JSON)));
    }

    @Test
    void updateSchemeData_AttemptingToPatchSchemeWithInvalidProperties_MaxLength() throws Exception {
        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_INVALID_PROPERTIES_MAX_LENGTH))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(HelperUtils.asJsonString(SCHEME_PATCH_MAX_LENGTH_VALIDATION_ERROR_JSON)));
    }

    @Test
    void updateSchemeData_AttemptingToPatchSchemeWithIncorrectRequestBodyProperties() throws Exception {
        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_NULL_JSON))
                .andExpect(status().isBadRequest()).andExpect(content().json(SCHEME_PATCH_DTO_CLASS_ERRORS_ALL_NULL));
    }

    @Test
    void updateSchemeData_AttemptingToPatchWithInvalidRequestBodyJson() throws Exception {
        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_INVALID_JSON))
                .andExpect(status().isBadRequest()).andExpect(content().string(""));
    }

    @Test
    void updateSchemeData_AttemptingToPatchSchemeWhichCantBeFound() throws Exception {
        Mockito.doThrow(new EntityNotFoundException()).when(this.schemeService).patchExistingScheme(SAMPLE_SCHEME_ID,
                SCHEME_PATCH_DTO_EXAMPLE);

        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_JSON))
                .andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    void updateSchemeData_IllegalArgument() throws Exception {
        Mockito.doThrow(new IllegalArgumentException()).when(this.schemeService).patchExistingScheme(SAMPLE_SCHEME_ID,
                SCHEME_PATCH_DTO_EXAMPLE);

        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_JSON))
                .andExpect(status().isBadRequest()).andExpect(content().string(""));
    }

    @Test
    void updateSchemeData_AccessDenied() throws Exception {
        Mockito.doThrow(new AccessDeniedException("")).when(this.schemeService).patchExistingScheme(SAMPLE_SCHEME_ID,
                SCHEME_PATCH_DTO_EXAMPLE);

        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_JSON))
                .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    void updateSchemeData_UnexpectedError() throws Exception {
        Mockito.doThrow(new SchemeEntityException(
                "Something went wrong while trying to update scheme with the id of: " + SAMPLE_SCHEME_ID))
                .when(this.schemeService).patchExistingScheme(SAMPLE_SCHEME_ID, SCHEME_PATCH_DTO_EXAMPLE);

        this.mockMvc
                .perform(patch("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO(
                        "Something went wrong while trying to update scheme with the id of: " + SAMPLE_SCHEME_ID))));
    }

    @Test
    void createSchemeHappyPathTest() throws Exception {
        Mockito.when(this.schemeService.postNewScheme(any(SchemePostDTO.class), any(HttpSession.class)))
                .thenReturn(SAMPLE_SCHEME_ID);

        this.mockMvc
                .perform(post("/schemes").content(HelperUtils.asJsonString(SCHEME_POST_DTO_EXAMPLE))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string(SAMPLE_SCHEME_ID.toString()));

    }

    @Test
    void createSchemeHandleExceptionUnhappyPathTest() throws Exception {
        String exceptionMessage = "Something went wrong while creating a new grant scheme.";
        Mockito.when(this.schemeService.postNewScheme(any(SchemePostDTO.class), any(HttpSession.class)))
                .thenThrow(new SchemeEntityException(exceptionMessage));

        MvcResult mvcResult = this.mockMvc
                .perform(post("/schemes").content(HelperUtils.asJsonString(SCHEME_POST_DTO_EXAMPLE))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()).andReturn();

        assertThat(mvcResult.getResolvedException().getMessage()).isEqualTo(exceptionMessage);
    }

    @Test
    void createSchemeHandleNullValuesPathTest() throws Exception {
        SchemePostDTO schemePostDtoExample = new SchemePostDTO(null, null, null);

        this.mockMvc
                .perform(post("/schemes").content(HelperUtils.asJsonString(schemePostDtoExample))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andExpect(content().json(SCHEME_POST_ALL_NULL_DTO_JSON))
                .andReturn();
    }

    @Test
    void createNewGrantScheme_IllegalArgument() throws Exception {
        Mockito.when(this.schemeService.postNewScheme(any(SchemePostDTO.class), any(HttpSession.class)))
                .thenThrow(new IllegalArgumentException());

        this.mockMvc
                .perform(post("/schemes").content(HelperUtils.asJsonString(SCHEME_POST_DTO_EXAMPLE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andExpect(content().string("")).andReturn();
    }

    @Test
    void testSuccessfullyDeletingScheme() throws Exception {
        Mockito.doNothing().when(this.schemeService).deleteASchemeById(SAMPLE_SCHEME_ID);

        this.mockMvc.perform(delete("/schemes/" + SAMPLE_SCHEME_ID)).andExpect(status().isOk())
                .andExpect(content().string("Scheme deleted successfully"));
    }

    @Test
    void testDeletingSchemeThatDoesntExist() throws Exception {
        Mockito.doThrow(new EntityNotFoundException()).when(this.schemeService).deleteASchemeById(SAMPLE_SCHEME_ID);

        this.mockMvc.perform(delete("/schemes/" + SAMPLE_SCHEME_ID)).andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void deleteAScheme_IllegalArgument() throws Exception {
        Mockito.doThrow(new IllegalArgumentException()).when(this.schemeService).deleteASchemeById(SAMPLE_SCHEME_ID);

        this.mockMvc.perform(delete("/schemes/" + SAMPLE_SCHEME_ID)).andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void testDeletingSchemeUnexpectedError() throws Exception {
        String exceptionMessage = "Something went wrong while trying to delete the scheme with the id of: "
                + SAMPLE_SCHEME_ID;
        Mockito.doThrow(new SchemeEntityException(exceptionMessage)).when(this.schemeService)
                .deleteASchemeById(SAMPLE_SCHEME_ID);

        this.mockMvc
                .perform(delete("/schemes/" + SAMPLE_SCHEME_ID).contentType(MediaType.APPLICATION_JSON)
                        .content(SCHEME_PATCH_DTO_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(HelperUtils.asJsonString(new GenericErrorDTO(exceptionMessage))));
    }

    @Test
    void deleteAScheme_AccessDenied() throws Exception {
        Mockito.doThrow(new AccessDeniedException("")).when(this.schemeService).deleteASchemeById(SAMPLE_SCHEME_ID);

        this.mockMvc.perform(delete("/schemes/" + SAMPLE_SCHEME_ID)).andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void getAllSchemesHappyPathWithResultsTest() throws Exception {
        Mockito.when(this.schemeService.getSignedInUsersSchemes()).thenReturn(SCHEME_DTOS_EXAMPLE);

        this.mockMvc.perform(get("/schemes").param("paginate", "false")).andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(SCHEME_DTOS_EXAMPLE)));
    }

    @Test
    void getAllSchemesHappyPathWithNoResultsTest() throws Exception {
        Mockito.when(this.schemeService.getSignedInUsersSchemes()).thenReturn(Collections.emptyList());

        this.mockMvc.perform(get("/schemes").param("paginate", "false")).andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllSchemesExceptionHandledTest() throws Exception {
        String exceptionMessage = "Something went wrong while trying to find all schemes belonging to: "
                + SAMPLE_ORGANISATION_ID;
        Mockito.when(this.schemeService.getSignedInUsersSchemes())
                .thenThrow(new SchemeEntityException(exceptionMessage));

        MvcResult mvcResult = this.mockMvc.perform(get("/schemes").param("paginate", "false"))
                .andExpect(status().isInternalServerError()).andReturn();

        assertThat(mvcResult.getResolvedException().getMessage()).isEqualTo(exceptionMessage);
    }

    @Test
    void getAllSchemes_InvalidArgumentHandling() throws Exception {
        Mockito.when(this.schemeService.getSignedInUsersSchemes()).thenThrow(new IllegalArgumentException());

        this.mockMvc.perform(get("/schemes").param("paginate", "false")).andExpect(status().isBadRequest())
                .andExpect(content().string("")).andReturn();
    }

    @Test
    void getSchemes_Paginated_HappyPathWithDefaultPagination() throws Exception {
        Pageable expectedPageable = PageRequest.of(0, 20);

        Mockito.when(this.schemeService.getPaginatedSchemes(expectedPageable)).thenReturn(SCHEME_DTOS_EXAMPLE);

        this.mockMvc.perform(get("/schemes").param("paginate", "true")).andExpect(status().isOk())
                .andExpect(content().json(HelperUtils.asJsonString(SCHEME_DTOS_EXAMPLE)));

        verify(this.schemeService).getPaginatedSchemes(expectedPageable);
        verify(this.schemeService, never()).getSignedInUsersSchemes();
    }

    @Test
    void getSchemes_Paginated_HappyPathWithCustomPagination() throws Exception {

        Pageable expectedPageable = PageRequest.of(0, 5);

        Mockito.when(this.schemeService.getPaginatedSchemes(expectedPageable)).thenReturn(SCHEME_DTOS_EXAMPLE);

        this.mockMvc.perform(get("/schemes").param("paginate", "true").param("page", "0").param("size", "5"))
                .andExpect(status().isOk()).andExpect(content().json(HelperUtils.asJsonString(SCHEME_DTOS_EXAMPLE)));

        verify(this.schemeService).getPaginatedSchemes(expectedPageable);
    }

    @Test
    void getSchemes_Paginated_HappyPathWithCustomPaginationAndSort() throws Exception {

        Pageable expectedPageable = PageRequest.of(0, 5, Sort.by("id").descending());

        Mockito.when(this.schemeService.getPaginatedSchemes(expectedPageable)).thenReturn(SCHEME_DTOS_EXAMPLE);

        this.mockMvc
                .perform(get("/schemes").param("paginate", "true").param("page", "0").param("size", "5").param("sort",
                        "id,desc"))
                .andExpect(status().isOk()).andExpect(content().json(HelperUtils.asJsonString(SCHEME_DTOS_EXAMPLE)));

        verify(this.schemeService).getPaginatedSchemes(expectedPageable);
    }

    @Test
    void getSchemes_Paginated_InvalidArgumentHandling() throws Exception {
        Mockito.when(this.schemeService.getPaginatedSchemes(any(Pageable.class)))
                .thenThrow(new IllegalArgumentException());

        this.mockMvc.perform(get("/schemes").param("paginate", "true")).andExpect(status().isBadRequest())
                .andExpect(content().string("")).andReturn();
    }

    @Nested
    class GetAdminsSchemes {

        @Test
        void HappyPath() throws Exception {
            Mockito.when(schemeService.getAdminsSchemes(1)).thenReturn(SCHEME_DTOS_EXAMPLE);

            mockMvc.perform(get("/schemes/admin/1")).andExpect(status().isOk())
                    .andExpect(content().json(HelperUtils.asJsonString(SCHEME_DTOS_EXAMPLE)));
        }

    }

}
