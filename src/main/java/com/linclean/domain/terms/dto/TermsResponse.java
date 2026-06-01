package com.linclean.domain.terms.dto;

import com.linclean.domain.terms.entity.ContentFormat;
import com.linclean.domain.terms.entity.Terms;
import com.linclean.domain.terms.entity.TermsType;

import java.time.Instant;

public record TermsResponse(
        TermsType type,
        String title,
        String content,
        ContentFormat contentFormat,
        Instant effectiveAt,
        Instant updatedAt
) {
    public static TermsResponse from(Terms terms) {
        return new TermsResponse(
                terms.getType(),
                terms.getTitle(),
                terms.getContent(),
                terms.getContentFormat(),
                terms.getEffectiveAt(),
                terms.getUpdatedAt()
        );
    }
}
