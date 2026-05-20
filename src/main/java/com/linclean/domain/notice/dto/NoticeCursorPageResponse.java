package com.linclean.domain.notice.dto;

import java.util.List;

public record NoticeCursorPageResponse(
        List<NoticeListItemResponse> items,
        String nextCursor,
        boolean hasNext
) {
}
