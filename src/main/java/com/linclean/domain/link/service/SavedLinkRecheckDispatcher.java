package com.linclean.domain.link.service;

import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import com.linclean.domain.analysis.service.AnalysisAsyncRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

/**
 * 저장 링크 재검사 1건을 처리한다.
 * 기존 Analysis 를 QUEUED 로 리셋하고 새 requestId 를 발급한 뒤(콜백 수용 조건),
 * 커밋 후 기존 위임 로직({@link AnalysisAsyncRunner})을 재사용해 FastAPI 에 재검사를 요청한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SavedLinkRecheckDispatcher {

    private final AnalysisRepository analysisRepository;
    private final AnalysisAsyncRunner asyncRunner;

    @Transactional
    public void dispatch(UUID analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId).orElse(null);
        if (analysis == null) {
            log.warn("재검사 대상 분석 없음 - analysisId={}", analysisId);
            return;
        }

        UUID requestId = UUID.randomUUID();
        String url = analysis.getOriginalUrl();
        analysis.markForRecheck(requestId, Instant.now());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncRunner.run(analysisId, url, requestId);
            }
        });
    }
}