package com.linclean.domain.notice.service;

import com.linclean.domain.notice.dto.NoticeCursorPageResponse;
import com.linclean.domain.notice.dto.NoticeDetailResponse;
import com.linclean.domain.notice.dto.NoticeListItemResponse;
import com.linclean.domain.notice.entity.Notice;
import com.linclean.domain.notice.exception.NoticeException;
import com.linclean.domain.notice.repository.NoticeRepository;
import com.linclean.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public NoticeCursorPageResponse getNotices(String cursor, int size) {
        int limit = size + 1;
        List<Notice> results = (cursor == null)
                ? noticeRepository.findFirstPage(limit)
                : fetchWithCursor(cursor, limit);

        boolean hasNext = results.size() > size;
        List<Notice> items = hasNext ? results.subList(0, size) : results;

        String nextCursor = hasNext ? encodeCursor(items.get(items.size() - 1)) : null;

        return new NoticeCursorPageResponse(
                items.stream().map(NoticeListItemResponse::from).toList(),
                nextCursor,
                hasNext
        );
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponse getNotice(Long id) {
        return noticeRepository.findById(id)
                .map(NoticeDetailResponse::from)
                .orElseThrow(() -> new NoticeException(ErrorCode.NOTICE_NOT_FOUND));
    }

    private List<Notice> fetchWithCursor(String cursor, int limit) {
        String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        String[] parts = decoded.split(",");
        Long lastId = Long.parseLong(parts[0]);
        Instant lastCreatedAt = Instant.ofEpochMilli(Long.parseLong(parts[1]));
        boolean lastIsPinned = "1".equals(parts[2]);
        return noticeRepository.findNextPage(lastIsPinned, lastCreatedAt, lastId, limit);
    }

    private String encodeCursor(Notice notice) {
        String raw = notice.getId() + "," + notice.getCreatedAt().toEpochMilli() + "," + (notice.isPinned() ? "1" : "0");
        return Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
