package com.linclean.domain.analysis.dto.callback;

import com.linclean.domain.analysis.entity.AnalysisStatus;
import com.linclean.domain.analysis.entity.Stages;
import com.linclean.domain.analysis.entity.Verdict;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ValidCallbackFields
public record AnalysisResultCallback(
        @NotNull UUID analysisId,
        @NotNull UUID requestId,
        @NotNull AnalysisStatus status,
        @NotNull String originalUrl,

        // succeeded fields (nullable)
        String finalUrl,
        Verdict verdict,
        @Min(0) @Max(100) Integer score,
        List<ReasonItem> reasons,
        Stages stages,
        String summary,

        // failed fields (nullable)
        ErrorInfo error,

        // common (required)
        @NotNull String engineVersion,
        @NotNull Instant analyzedAt,
        @NotNull Integer elapsedMs
) {
    public record ReasonItem(String code, @Min(1) @Max(4) int stage, int weight, String message) {}
    public record ErrorInfo(String code, Integer stage, String message) {}
}
