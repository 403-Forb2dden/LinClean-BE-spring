package com.linclean.domain.analysis.repository;

import com.linclean.domain.analysis.entity.Analysis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

    @Query("SELECT a.verdict, COUNT(a) FROM Analysis a WHERE a.verdict IS NOT NULL GROUP BY a.verdict")
    List<Object[]> countGroupByVerdict();

    // SUCCEEDED 뿐 아니라 FAILED(지난 재검사 실패)도 포함해, 일시 장애로 실패한 링크가
    // 다음 주기에 재시도되도록 한다. QUEUED(초기 분석/재검사 진행 중)는 제외한다.
    @Query("""
            SELECT a FROM Analysis a
            WHERE a.status IN (
                    com.linclean.domain.analysis.entity.AnalysisStatus.SUCCEEDED,
                    com.linclean.domain.analysis.entity.AnalysisStatus.FAILED)
              AND COALESCE(a.lastCheckedAt, a.analyzedAt) < :threshold
              AND a.analysisId > :cursor
              AND EXISTS (SELECT 1 FROM SavedLink s WHERE s.analysis = a)
            ORDER BY a.analysisId
            """)
    Slice<Analysis> findStaleSavedAnalyses(@Param("threshold") Instant threshold,
                                           @Param("cursor") UUID cursor,
                                           Pageable pageable);
}
