package com.linclean.domain.notice.dto;

import com.linclean.domain.notice.entity.Notice;

import java.time.Instant;

public record NoticeListItemResponse(
        Long id,
        String title,
        boolean isPinned,
        Instant createdAt
) {
    public static NoticeListItemResponse from(Notice notice) {
        return new NoticeListItemResponse(
                notice.getId(),
                notice.getTitle(),
                notice.isPinned(),
                notice.getCreatedAt()
        );
    }
}
