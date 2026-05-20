package com.linclean.domain.link.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linclean.domain.link.dto.response.CategoryListResponse;
import com.linclean.domain.link.dto.response.CategoryRenameResponse;
import com.linclean.domain.link.dto.response.CategoryResponse;
import com.linclean.domain.link.exception.CategoryException;
import com.linclean.domain.link.exception.SavedLinkException;
import com.linclean.domain.link.service.CategoryService;
import com.linclean.global.exception.ErrorCode;
import com.linclean.global.exception.GlobalExceptionHandler;
import com.linclean.security.MemberPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest {

    private static final Authentication AUTH = new UsernamePasswordAuthenticationToken(
            new MemberPrincipal(1L, UUID.randomUUID()), null, List.of());

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = mock(CategoryService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        SecurityContextHolder.getContext().setAuthentication(AUTH);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CategoryResponse categoryResponse(long id, String name, int displayOrder, long linkCount) {
        return new CategoryResponse(id, name, displayOrder, linkCount, Instant.now());
    }

    // ── POST /api/v1/categories ───────────────────────────────────────────

    @Test
    void createCategory_returns201() throws Exception {
        given(categoryService.createCategory(eq(1L), any()))
                .willReturn(categoryResponse(10L, "업무", 0, 0L));

        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"업무\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.name").value("업무"))
                .andExpect(jsonPath("$.data.displayOrder").value(0))
                .andExpect(jsonPath("$.data.linkCount").value(0));
    }

    @Test
    void createCategory_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createCategory_nameTooLong_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "a".repeat(51)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createCategory_duplicateName_returns409() throws Exception {
        given(categoryService.createCategory(eq(1L), any()))
                .willThrow(new CategoryException(ErrorCode.CATEGORY_DUPLICATE_NAME));

        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"중복\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_DUPLICATE_NAME.getCode()));
    }

    @Test
    void createCategory_linkNotFound_returns404() throws Exception {
        given(categoryService.createCategory(eq(1L), any()))
                .willThrow(new SavedLinkException(ErrorCode.SAVED_LINK_NOT_FOUND));

        mockMvc.perform(post("/api/v1/categories")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"업무\", \"linkIds\": [99]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.SAVED_LINK_NOT_FOUND.getCode()));
    }

    // ── GET /api/v1/categories ────────────────────────────────────────────

    @Test
    void getCategories_returns200() throws Exception {
        given(categoryService.getCategories(1L))
                .willReturn(new CategoryListResponse(List.of(categoryResponse(1L, "업무", 0, 3L))));

        mockMvc.perform(get("/api/v1/categories")
                        .with(authentication(AUTH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].linkCount").value(3));
    }

    @Test
    void getCategories_empty_returns200WithEmptyItems() throws Exception {
        given(categoryService.getCategories(1L))
                .willReturn(new CategoryListResponse(List.of()));

        mockMvc.perform(get("/api/v1/categories")
                        .with(authentication(AUTH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    // ── PATCH /api/v1/categories/{id} ─────────────────────────────────────

    @Test
    void renameCategory_returns200() throws Exception {
        given(categoryService.renameCategory(eq(1L), eq(5L), any()))
                .willReturn(new CategoryRenameResponse(5L, "새이름"));

        mockMvc.perform(patch("/api/v1/categories/5")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"새이름\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.name").value("새이름"));
    }

    @Test
    void renameCategory_blankName_returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/categories/5")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void renameCategory_notFound_returns404() throws Exception {
        given(categoryService.renameCategory(eq(1L), eq(99L), any()))
                .willThrow(new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/categories/99")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"새이름\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()));
    }

    @Test
    void renameCategory_forbidden_returns403() throws Exception {
        given(categoryService.renameCategory(eq(1L), eq(5L), any()))
                .willThrow(new CategoryException(ErrorCode.CATEGORY_FORBIDDEN));

        mockMvc.perform(patch("/api/v1/categories/5")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"새이름\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_FORBIDDEN.getCode()));
    }

    @Test
    void renameCategory_duplicateName_returns409() throws Exception {
        given(categoryService.renameCategory(eq(1L), eq(5L), any()))
                .willThrow(new CategoryException(ErrorCode.CATEGORY_DUPLICATE_NAME));

        mockMvc.perform(patch("/api/v1/categories/5")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"중복\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_DUPLICATE_NAME.getCode()));
    }

    // ── DELETE /api/v1/categories/{id} ────────────────────────────────────

    @Test
    void deleteCategory_returns204() throws Exception {
        willDoNothing().given(categoryService).deleteCategory(1L, 5L);

        mockMvc.perform(delete("/api/v1/categories/5")
                        .with(authentication(AUTH)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_notFound_returns404() throws Exception {
        willThrow(new CategoryException(ErrorCode.CATEGORY_NOT_FOUND))
                .given(categoryService).deleteCategory(1L, 99L);

        mockMvc.perform(delete("/api/v1/categories/99")
                        .with(authentication(AUTH)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()));
    }

    @Test
    void deleteCategory_forbidden_returns403() throws Exception {
        willThrow(new CategoryException(ErrorCode.CATEGORY_FORBIDDEN))
                .given(categoryService).deleteCategory(1L, 5L);

        mockMvc.perform(delete("/api/v1/categories/5")
                        .with(authentication(AUTH)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_FORBIDDEN.getCode()));
    }
}
