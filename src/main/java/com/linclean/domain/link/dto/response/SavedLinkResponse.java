package com.linclean.domain.link.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.linclean.domain.analysis.entity.Verdict;
import com.linclean.domain.link.entity.SavedLink;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SavedLinkResponse(
        Long id,
        UUID analysisId,
        Long categoryId,
        String originalUrl,
        String finalUrl,
        String title,
        String description,
        Verdict verdict,
        boolean isBookmarked,
        Instant createdAt
) {
    public static SavedLinkResponse from(SavedLink link) {
        return new SavedLinkResponse(
                link.getId(),
                link.getAnalysis().getAnalysisId(),
                link.getCategory() != null ? link.getCategory().getId() : null,
                link.getAnalysis().getOriginalUrl(),
                link.getAnalysis().getFinalUrl(),
                link.getTitle(),
                link.getDescription(),
                link.getAnalysis().getVerdict(),
                link.isBookmarked(),
                link.getCreatedAt()
        );
    }
}
