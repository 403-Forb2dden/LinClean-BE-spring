package com.linclean.domain.notice.controller;

import com.linclean.domain.notice.dto.NoticeCursorPageResponse;
import com.linclean.domain.notice.dto.NoticeDetailResponse;
import com.linclean.domain.notice.dto.NoticeListItemResponse;
import com.linclean.domain.notice.exception.NoticeException;
import com.linclean.domain.notice.service.NoticeService;
import com.linclean.global.exception.ErrorCode;
import com.linclean.security.MemberSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = NoticeController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class}
)
@ActiveProfiles("test")
class NoticeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NoticeService noticeService;

    @MockBean
    MemberSyncService memberSyncService;

    @Test
    void getNotices_defaultParams_returns200() throws Exception {
        NoticeCursorPageResponse response = new NoticeCursorPageResponse(
                List.of(new NoticeListItemResponse(5L, "[공지] 이용 안내", true, Instant.parse("2026-04-15T09:00:00Z"))),
                null,
                false
        );
        given(noticeService.getNotices(null, 20)).willReturn(response);

        mockMvc.perform(get("/api/v1/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(5))
                .andExpect(jsonPath("$.data.items[0].title").value("[공지] 이용 안내"))
                .andExpect(jsonPath("$.data.items[0].isPinned").value(true))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist());
    }

    @Test
    void getNotices_withCursorAndSize_returnsNextPage() throws Exception {
        NoticeCursorPageResponse response = new NoticeCursorPageResponse(List.of(), "nextCursorValue", true);
        given(noticeService.getNotices(eq("someCursor"), eq(10))).willReturn(response);

        mockMvc.perform(get("/api/v1/notices").param("cursor", "someCursor").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value("nextCursorValue"));
    }

    @Test
    void getNotice_found_returns200WithContent() throws Exception {
        NoticeDetailResponse response = new NoticeDetailResponse(
                5L, "[공지] 이용 안내", "안녕하세요...", true,
                Instant.parse("2026-04-15T09:00:00Z"), Instant.parse("2026-04-15T09:00:00Z")
        );
        given(noticeService.getNotice(5L)).willReturn(response);

        mockMvc.perform(get("/api/v1/notices/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.content").value("안녕하세요..."))
                .andExpect(jsonPath("$.data.isPinned").value(true));
    }

    @Test
    void getNotice_notFound_returns404() throws Exception {
        given(noticeService.getNotice(99L))
                .willThrow(new NoticeException(ErrorCode.NOTICE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/notices/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTICE_001"));
    }

    @Test
    void getNotices_sizeZero_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/notices").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNotices_negativeSize_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/notices").param("size", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNotices_sizeOverMax_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/notices").param("size", "51"))
                .andExpect(status().isBadRequest());
    }
}
