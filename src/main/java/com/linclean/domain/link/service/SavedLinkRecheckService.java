package com.linclean.domain.link.service;

import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * 저장 링크 주간 재검사 오케스트레이션.
 * 마지막 검사 후 N일 지난 저장 링크(참조 Analysis)를 keyset 페이지로 순회하며
 * 건별로 {@link SavedLinkRecheckDispatcher} 에 재검사를 위임한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SavedLinkRecheckService {

    private static final int BATCH_SIZE = 100;
    /** UUID 오름차순 순회 시작 커서 (모든 값보다 작음). */
    private static final UUID START_CURSOR = new UUID(0L, 0L);

    private final AnalysisRepository analysisRepository;
    private final SavedLinkRecheckDispatcher dispatcher;

    @Value("${link.recheck.stale-days:7}")
    private int staleDays;

    public void runRecheck() {
        Instant threshold = Instant.now().minus(staleDays, ChronoUnit.DAYS);
        UUID cursor = START_CURSOR;
        int dispatched = 0;
        int failed = 0;

        while (true) {
            Slice<Analysis> slice = analysisRepository.findStaleSavedAnalyses(
                    threshold, cursor, PageRequest.of(0, BATCH_SIZE));
            List<Analysis> batch = slice.getContent();
            if (batch.isEmpty()) {
                break;
            }

            // TODO(follow-up): 현재 위임은 AnalysisAsyncRunner 의 analysisTaskExecutor(실시간 분석과 공유)에
            //  백프레셔를 위임한다. stale 규모가 커지면 재검사 전용 bounded executor 분리 + 레이트 리밋 필요.
            for (Analysis analysis : batch) {
                UUID analysisId = analysis.getAnalysisId();
                cursor = analysisId;
                try {
                    dispatcher.dispatch(analysisId);
                    dispatched++;
                } catch (Exception e) {
                    failed++;
                    log.error("재검사 위임 실패 - analysisId={}", analysisId, e);
                }
            }

            if (!slice.hasNext()) {
                break;
            }
        }

        log.info("저장 링크 재검사 완료 - staleDays={}, dispatched={}, failed={}",
                staleDays, dispatched, failed);
    }
}