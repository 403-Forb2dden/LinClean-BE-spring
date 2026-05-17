package com.linclean.domain.link.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SavedLinkListQuery(
        Long categoryId,
        Boolean bookmarked,
        String cursor,
        @Min(value = 1, message = "size는 1 이상이어야 합니다.")
        @Max(value = 50, message = "size는 50 이하여야 합니다.")
        Integer size
) {
    public SavedLinkListQuery {
        if (size == null) size = 20;
    }
}
