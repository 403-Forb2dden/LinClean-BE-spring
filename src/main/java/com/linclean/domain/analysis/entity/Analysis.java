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

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Analysis extends BaseAuditEntity {

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
    private StagesDto stages;

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
}
