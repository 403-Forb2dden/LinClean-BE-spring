package com.linclean.domain.terms.controller;

import com.linclean.domain.terms.converter.TermsTypeSpringConverter;
import com.linclean.domain.terms.dto.TermsResponse;
import com.linclean.domain.terms.entity.ContentFormat;
import com.linclean.domain.terms.entity.TermsType;
import com.linclean.domain.terms.exception.TermsException;
import com.linclean.domain.terms.service.TermsService;
import com.linclean.global.exception.ErrorCode;
import com.linclean.security.MemberSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = TermsController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class}
)
@Import(TermsTypeSpringConverter.class)
@ActiveProfiles("test")
class TermsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean TermsService termsService;
    @MockBean MemberSyncService memberSyncService;

    @Test
    void getTerms_validType_returns200() throws Exception {
        TermsResponse response = new TermsResponse(
                TermsType.PRIVACY_POLICY,
                "개인정보 처리방침",
                "제1조 ...",
                ContentFormat.MARKDOWN,
                Instant.parse("2026-05-01T00:00:00Z"),
                Instant.parse("2026-05-01T00:00:00Z")
        );
        given(termsService.getTerms(TermsType.PRIVACY_POLICY)).willReturn(response);

        mockMvc.perform(get("/api/v1/terms/privacy_policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("privacy_policy"))
                .andExpect(jsonPath("$.data.title").value("개인정보 처리방침"))
                .andExpect(jsonPath("$.data.contentFormat").value("markdown"));
    }

    @Test
    void getTerms_invalidType_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/terms/unknown_type"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTerms_typeNotInDb_returns404() throws Exception {
        given(termsService.getTerms(TermsType.SERVICE_GUIDE))
                .willThrow(new TermsException(ErrorCode.TERMS_NOT_FOUND));

        mockMvc.perform(get("/api/v1/terms/service_guide"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERMS_001"));
    }
}
