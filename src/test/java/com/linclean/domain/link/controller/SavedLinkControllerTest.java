package com.linclean.domain.link.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linclean.domain.analysis.entity.Verdict;
import com.linclean.domain.analysis.exception.AnalysisException;
import com.linclean.domain.link.dto.request.SavedLinkListQuery;
import com.linclean.domain.link.dto.response.BookmarkToggleResponse;
import com.linclean.domain.link.dto.response.CategoryUpdateResponse;
import com.linclean.domain.link.dto.response.SavedLinkListResponse;
import com.linclean.domain.link.dto.response.SavedLinkResponse;
import com.linclean.domain.link.exception.SavedLinkException;
import com.linclean.domain.link.service.SavedLinkService;
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

class SavedLinkControllerTest {

    private static final UUID ANALYSIS_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Authentication AUTH = new UsernamePasswordAuthenticationToken(
            new MemberPrincipal(1L, UUID.randomUUID()), null, List.of());

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SavedLinkService savedLinkService;

    @BeforeEach
    void setUp() {
        savedLinkService = mock(SavedLinkService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new SavedLinkController(savedLinkService))
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

    private SavedLinkResponse savedLinkResponse(long id, Long categoryId) {
        return new SavedLinkResponse(
                id, ANALYSIS_UUID, categoryId,
                "https://example.com", "https://example.com/final",
                "제목", "설명",
                Verdict.SAFE, false, Instant.now());
    }

    // ── POST /api/v1/saved-links ──────────────────────────────────────────

    @Test
    void createSavedLink_returns201() throws Exception {
        given(savedLinkService.createSavedLink(eq(1L), any())).willReturn(savedLinkResponse(10L, null));

        mockMvc.perform(post("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "analysisId", ANALYSIS_UUID.toString(),
                                "title", "제목",
                                "description", "설명"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.analysisId").value(ANALYSIS_UUID.toString()));
    }

    @Test
    void createSavedLink_missingAnalysisId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createSavedLink_titleTooLong_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "analysisId", ANALYSIS_UUID.toString(),
                                "title", "a".repeat(501)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createSavedLink_analysisNotFound_returns404() throws Exception {
        given(savedLinkService.createSavedLink(eq(1L), any()))
                .willThrow(new AnalysisException(ErrorCode.ANALYSIS_NOT_FOUND));

        mockMvc.perform(post("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "analysisId", ANALYSIS_UUID.toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ANALYSIS_NOT_FOUND.getCode()));
    }

    @Test
    void createSavedLink_analysisNotSucceeded_returns422() throws Exception {
        given(savedLinkService.createSavedLink(eq(1L), any()))
                .willThrow(new AnalysisException(ErrorCode.ANALYSIS_NOT_SUCCEEDED));

        mockMvc.perform(post("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "analysisId", ANALYSIS_UUID.toString()))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCode.ANALYSIS_NOT_SUCCEEDED.getCode()));
    }

    @Test
    void createSavedLink_dangerVerdict_returns422() throws Exception {
        given(savedLinkService.createSavedLink(eq(1L), any()))
                .willThrow(new SavedLinkException(ErrorCode.SAVED_LINK_FORBIDDEN_DANGER));

        mockMvc.perform(post("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "analysisId", ANALYSIS_UUID.toString()))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCode.SAVED_LINK_FORBIDDEN_DANGER.getCode()));
    }

    @Test
    void createSavedLink_categoryNotFound_returns404() throws Exception {
        given(savedLinkService.createSavedLink(eq(1L), any()))
                .willThrow(new SavedLinkException(ErrorCode.CATEGORY_NOT_FOUND));

        mockMvc.perform(post("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "analysisId", ANALYSIS_UUID.toString(),
                                "categoryId", 99))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()));
    }

    @Test
    void createSavedLink_duplicate_returns409() throws Exception {
        given(savedLinkService.createSavedLink(eq(1L), any()))
                .willThrow(new SavedLinkException(ErrorCode.SAVED_LINK_DUPLICATE));

        mockMvc.perform(post("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "analysisId", ANALYSIS_UUID.toString()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.SAVED_LINK_DUPLICATE.getCode()));
    }

    // ── GET /api/v1/saved-links ───────────────────────────────────────────

    @Test
    void getSavedLinks_returns200() throws Exception {
        SavedLinkListResponse listResponse = new SavedLinkListResponse(
                List.of(savedLinkResponse(10L, null)), false, null);
        given(savedLinkService.getSavedLinks(eq(1L), any(SavedLinkListQuery.class)))
                .willReturn(listResponse);

        mockMvc.perform(get("/api/v1/saved-links")
                        .with(authentication(AUTH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(10))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    void getSavedLinks_withFilters_returns200() throws Exception {
        SavedLinkListResponse listResponse = new SavedLinkListResponse(
                List.of(savedLinkResponse(10L, 5L)), true, "next-cursor");
        given(savedLinkService.getSavedLinks(eq(1L), any(SavedLinkListQuery.class)))
                .willReturn(listResponse);

        mockMvc.perform(get("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .param("categoryId", "5")
                        .param("bookmarked", "true")
                        .param("cursor", "abc")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("next-cursor"));
    }

    @Test
    void getSavedLinks_sizeZero_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getSavedLinks_sizeOver50_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/saved-links")
                        .with(authentication(AUTH))
                        .param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // ── DELETE /api/v1/saved-links/{id} ──────────────────────────────────

    @Test
    void deleteSavedLink_returns204() throws Exception {
        willDoNothing().given(savedLinkService).deleteSavedLink(1L, 10L);

        mockMvc.perform(delete("/api/v1/saved-links/10")
                        .with(authentication(AUTH)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSavedLink_notFound_returns404() throws Exception {
        willThrow(new SavedLinkException(ErrorCode.SAVED_LINK_NOT_FOUND))
                .given(savedLinkService).deleteSavedLink(1L, 99L);

        mockMvc.perform(delete("/api/v1/saved-links/99")
                        .with(authentication(AUTH)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.SAVED_LINK_NOT_FOUND.getCode()));
    }

    // ── PATCH /api/v1/saved-links/{id}/bookmark ───────────────────────────

    @Test
    void toggleBookmark_returns200() throws Exception {
        given(savedLinkService.toggleBookmark(1L, 10L))
                .willReturn(new BookmarkToggleResponse(10L, true));

        mockMvc.perform(patch("/api/v1/saved-links/10/bookmark")
                        .with(authentication(AUTH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.isBookmarked").value(true));
    }

    @Test
    void toggleBookmark_notFound_returns404() throws Exception {
        given(savedLinkService.toggleBookmark(1L, 99L))
                .willThrow(new SavedLinkException(ErrorCode.SAVED_LINK_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/saved-links/99/bookmark")
                        .with(authentication(AUTH)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.SAVED_LINK_NOT_FOUND.getCode()));
    }

    // ── PATCH /api/v1/saved-links/{id} ────────────────────────────────────

    @Test
    void updateCategory_withCategory_returns200() throws Exception {
        given(savedLinkService.updateCategory(eq(1L), eq(10L), any()))
                .willReturn(new CategoryUpdateResponse(10L, 5L));

        mockMvc.perform(patch("/api/v1/saved-links/10/category")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.categoryId").value(5));
    }

    @Test
    void updateCategory_removeCategory_returns200() throws Exception {
        given(savedLinkService.updateCategory(eq(1L), eq(10L), any()))
                .willReturn(new CategoryUpdateResponse(10L, null));

        mockMvc.perform(patch("/api/v1/saved-links/10/category")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.categoryId").doesNotExist());
    }

    @Test
    void updateCategory_savedLinkNotFound_returns404() throws Exception {
        given(savedLinkService.updateCategory(eq(1L), eq(99L), any()))
                .willThrow(new SavedLinkException(ErrorCode.SAVED_LINK_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/saved-links/99/category")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\": 5}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.SAVED_LINK_NOT_FOUND.getCode()));
    }

    @Test
    void updateCategory_categoryNotFound_returns404() throws Exception {
        given(savedLinkService.updateCategory(eq(1L), eq(10L), any()))
                .willThrow(new SavedLinkException(ErrorCode.CATEGORY_NOT_FOUND));

        mockMvc.perform(patch("/api/v1/saved-links/10/category")
                        .with(authentication(AUTH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\": 99}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()));
    }
}
