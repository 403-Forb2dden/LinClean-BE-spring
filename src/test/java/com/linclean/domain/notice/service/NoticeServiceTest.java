package com.linclean.domain.notice.service;

import com.linclean.domain.notice.dto.NoticeCursorPageResponse;
import com.linclean.domain.notice.dto.NoticeDetailResponse;
import com.linclean.domain.notice.entity.Notice;
import com.linclean.domain.notice.exception.NoticeException;
import com.linclean.domain.notice.repository.NoticeRepository;
import com.linclean.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    NoticeRepository noticeRepository;

    @InjectMocks
    NoticeService noticeService;

    private Notice mockNotice(Long id, String title, boolean isPinned, Instant createdAt) {
        Notice notice = mock(Notice.class);
        given(notice.getId()).willReturn(id);
        given(notice.getTitle()).willReturn(title);
        given(notice.isPinned()).willReturn(isPinned);
        given(notice.getCreatedAt()).willReturn(createdAt);
        return notice;
    }

    // ── 목록 조회 ──────────────────────────────────────────────

    @Test
    void getNotices_noCursor_callsFirstPage() {
        given(noticeRepository.findFirstPage(21)).willReturn(List.of());

        NoticeCursorPageResponse response = noticeService.getNotices(null, 20);

        verify(noticeRepository).findFirstPage(21);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void getNotices_hasMore_returnsNextCursorAndTruncatesItems() {
        Notice notice1 = mockNotice(5L, "공지1", true, Instant.parse("2026-04-15T09:00:00Z"));
        Notice notice2 = mock(Notice.class); // 길이 초과 확인용 — 메서드 호출 없음
        given(noticeRepository.findFirstPage(2)).willReturn(List.of(notice1, notice2));

        NoticeCursorPageResponse response = noticeService.getNotices(null, 1);

        assertThat(response.items()).hasSize(1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
    }

    @Test
    void getNotices_withValidCursor_callsNextPage() {
        Instant createdAt = Instant.parse("2026-04-15T09:00:00Z");
        String cursor = Base64.getUrlEncoder()
                .encodeToString(("5," + createdAt.toEpochMilli() + ",1").getBytes(StandardCharsets.UTF_8));
        given(noticeRepository.findNextPage(eq(true), eq(createdAt), eq(5L), eq(21)))
                .willReturn(List.of());

        NoticeCursorPageResponse response = noticeService.getNotices(cursor, 20);

        verify(noticeRepository).findNextPage(true, createdAt, 5L, 21);
        assertThat(response.hasNext()).isFalse();
    }

    // ── 상세 조회 ──────────────────────────────────────────────

    @Test
    void getNotice_found_returnsDetail() {
        Notice notice = mockNotice(5L, "[공지] 이용 안내", true, Instant.parse("2026-04-15T09:00:00Z"));
        given(notice.getContent()).willReturn("안녕하세요.");
        given(notice.getUpdatedAt()).willReturn(Instant.parse("2026-04-15T09:00:00Z"));
        given(noticeRepository.findById(5L)).willReturn(Optional.of(notice));

        NoticeDetailResponse response = noticeService.getNotice(5L);

        assertThat(response.title()).isEqualTo("[공지] 이용 안내");
    }

    @Test
    void getNotice_notFound_throwsNoticeException() {
        given(noticeRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNotice(99L))
                .isInstanceOf(NoticeException.class)
                .satisfies(ex -> assertThat(((NoticeException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOTICE_NOT_FOUND));
    }
}
