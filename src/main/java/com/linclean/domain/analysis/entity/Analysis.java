package com.linclean.domain.analysis.entity;

import com.linclean.domain.analysis.converter.AnalysisStatusConverter;
import com.linclean.domain.analysis.converter.VerdictConverter;
import com.linclean.domain.member.entity.Member;
import com.linclean.global.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Analysis extends BaseAuditEntity {

    @Version
    private Long version;

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "analysis_id")
    private UUID analysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "final_url", length = 2048)
    private String finalUrl;

    @Convert(converter = AnalysisStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AnalysisStatus status = AnalysisStatus.QUEUED;

    @Convert(converter = VerdictConverter.class)
    @Column(name = "verdict", length = 20)
    private Verdict verdict;

    @Column(name = "score")
    private Integer score;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stages", columnDefinition = "jsonb")
    private Stages stages;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_stage")
    private Integer errorStage;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "engine_version", length = 50)
    private String engineVersion;

    @Column(name = "analyzed_at")
    private Instant analyzedAt;

    @Column(name = "elapsed_ms")
    private Integer elapsedMs;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    public void updateToSucceeded(
            String finalUrl, Verdict verdict, Integer score, String summary,
            Stages stages, String engineVersion, Instant analyzedAt, Integer elapsedMs) {
        Assert.notNull(finalUrl, "SUCCEEDED 상태 전환에는 finalUrl이 필요합니다");
        Assert.notNull(verdict, "SUCCEEDED 상태 전환에는 verdict가 필요합니다");
        Assert.notNull(score, "SUCCEEDED 상태 전환에는 score가 필요합니다");
        Assert.notNull(stages, "SUCCEEDED 상태 전환에는 stages가 필요합니다");
        Assert.notNull(summary, "SUCCEEDED 상태 전환에는 summary가 필요합니다");
        this.status = AnalysisStatus.SUCCEEDED;
        this.finalUrl = finalUrl;
        this.verdict = verdict;
        this.score = score;
        this.summary = summary;
        this.stages = stages;
        this.engineVersion = engineVersion;
        this.analyzedAt = analyzedAt;
        this.elapsedMs = elapsedMs;
    }

    public void markForRecheck(UUID newRequestId, Instant checkedAt) {
        this.status = AnalysisStatus.QUEUED;
        this.requestId = newRequestId;
        this.lastCheckedAt = checkedAt;
        // 직전이 FAILED였던 건도 재검사 대상이므로, 과거 오류 흔적을 지우고 다시 시작한다.
        this.errorCode = null;
        this.errorStage = null;
        this.errorMessage = null;
    }

    public void updateToFailed(
            String errorCode, Integer errorStage, String errorMessage,
            String engineVersion, Instant analyzedAt, Integer elapsedMs) {
        this.status = AnalysisStatus.FAILED;
        this.errorCode = errorCode;
        this.errorStage = errorStage;
        this.errorMessage = errorMessage;
        this.engineVersion = engineVersion;
        this.analyzedAt = analyzedAt;
        this.elapsedMs = elapsedMs;
    }
}
