package com.linclean.domain.link.dto.response;

import java.util.List;

public record SavedLinkListResponse(
        List<SavedLinkResponse> items,
        boolean hasNext,
        String nextCursor
) {}
