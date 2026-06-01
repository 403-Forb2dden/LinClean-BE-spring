package com.linclean.domain.notice.dto;

import com.linclean.domain.notice.entity.Notice;

import java.time.Instant;

public record NoticeDetailResponse(
        Long id,
        String title,
        String content,
        boolean isPinned,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoticeDetailResponse from(Notice notice) {
        return new NoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
