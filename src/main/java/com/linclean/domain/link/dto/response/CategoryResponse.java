package com.linclean.domain.link.dto.response;

import com.linclean.domain.link.entity.Category;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        int displayOrder,
        long linkCount,
        Instant createdAt
) {
    public static CategoryResponse of(Category category, long linkCount) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDisplayOrder(),
                linkCount,
                category.getCreatedAt()
        );
    }
}
