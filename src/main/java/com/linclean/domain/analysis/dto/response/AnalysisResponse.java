package com.linclean.domain.analysis.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.entity.AnalysisReason;
import com.linclean.domain.analysis.entity.AnalysisStatus;
import com.linclean.domain.analysis.entity.Stages;
import com.linclean.domain.analysis.entity.Verdict;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResponse {

    private final UUID analysisId;
    private final AnalysisStatus status;

    private final String originalUrl;
    private final String finalUrl;
    private final Verdict verdict;
    private final Integer score;
    private final String summary;
    private final List<ReasonDto> reasons;
    private final Instant analyzedAt;
    private final Integer elapsedMs;

    private final String errorCode;
    private final Integer errorStage;
    private final String errorMessage;

    private final String contentAnalysisError;

    public static AnalysisResponse forQueued(UUID analysisId) {
        return new AnalysisResponse(
                analysisId, AnalysisStatus.QUEUED,
                null, null, null, null, null, null, null, null,
                null, null, null,
                null
        );
    }

    public static AnalysisResponse forSucceeded(Analysis analysis, List<AnalysisReason> reasons) {
        String contentAnalysisError = null;
        Stages stages = analysis.getStages();
        if (stages != null && stages.getContentAnalysis() != null
                && !stages.getContentAnalysis().isFetched()) {
            contentAnalysisError = stages.getContentAnalysis().getReason();
        }
        return new AnalysisResponse(
                analysis.getAnalysisId(), AnalysisStatus.SUCCEEDED,
                analysis.getOriginalUrl(), analysis.getFinalUrl(),
                analysis.getVerdict(), analysis.getScore(), analysis.getSummary(),
                reasons.stream().map(ReasonDto::from).toList(),
                analysis.getAnalyzedAt(), analysis.getElapsedMs(),
                null, null, null,
                contentAnalysisError
        );
    }

    public static AnalysisResponse forFailed(Analysis analysis) {
        return new AnalysisResponse(
                analysis.getAnalysisId(), AnalysisStatus.FAILED,
                analysis.getOriginalUrl(), null, null, null, null, null, null, null,
                analysis.getErrorCode(), analysis.getErrorStage(), analysis.getErrorMessage(),
                null
        );
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReasonDto {
        private final String code;
        private final int stage;
        private final int weight;
        private final String message;

        public static ReasonDto from(AnalysisReason reason) {
            return new ReasonDto(
                    reason.getCode(),
                    reason.getStage(),
                    reason.getWeight(),
                    reason.getMessage()
            );
        }
    }
}
