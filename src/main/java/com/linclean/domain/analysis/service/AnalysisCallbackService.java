package com.linclean.domain.analysis.service;

import com.linclean.domain.analysis.dto.callback.AnalysisResultCallback;
import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.entity.AnalysisReason;
import com.linclean.domain.analysis.entity.AnalysisStatus;
import com.linclean.domain.analysis.repository.AnalysisReasonRepository;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisCallbackService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisReasonRepository analysisReasonRepository;

    @Transactional
    public void handleCallback(AnalysisResultCallback callback) {
        Analysis analysis = analysisRepository.findById(callback.analysisId())
                .orElseGet(() -> {
                    log.warn("콜백 수신 - 존재하지 않는 analysisId={}", callback.analysisId());
                    return null;
                });

        if (analysis == null) {
            return;
        }

        if (!analysis.getRequestId().equals(callback.requestId())) {
            log.warn("콜백 requestId 불일치 - analysisId={}, expected={}, actual={}",
                    callback.analysisId(), analysis.getRequestId(), callback.requestId());
            return;
        }

        if (analysis.getStatus() != AnalysisStatus.QUEUED) {
            log.info("중복 콜백 무시 - analysisId={}, currentStatus={}",
                    callback.analysisId(), analysis.getStatus());
            return;
        }

        if (callback.status() == AnalysisStatus.SUCCEEDED) {
            applySucceeded(analysis, callback);
        } else if (callback.status() == AnalysisStatus.FAILED) {
            applyFailed(analysis, callback);
        } else {
            log.warn("알 수 없는 콜백 status - analysisId={}, status={}",
                    callback.analysisId(), callback.status());
            return;
        }

        log.info("콜백 처리 완료 - analysisId={}, status={}", callback.analysisId(), callback.status());
    }

    private void applySucceeded(Analysis analysis, AnalysisResultCallback callback) {
        analysis.updateToSucceeded(
                callback.finalUrl(),
                callback.verdict(),
                callback.score(),
                callback.summary(),
                callback.stages(),
                callback.engineVersion(),
                callback.analyzedAt(),
                callback.elapsedMs()
        );

        List<AnalysisReason> reasons = callback.reasons().stream()
                .map(r -> AnalysisReason.builder()
                        .analysis(analysis)
                        .code(r.code())
                        .stage(r.stage())
                        .weight(r.weight())
                        .message(r.message())
                        .build())
                .toList();
        // 재검사 성공 콜백은 SUCCEEDED 전환을 반복하므로, 기존 reason을 지운 뒤 새로 저장한다.
        // (같은 @Transactional 안이라 커밋 전까지 다른 조회는 과거 reason을 그대로 본다.)
        analysisReasonRepository.deleteAllByAnalysis_AnalysisId(analysis.getAnalysisId());
        analysisReasonRepository.saveAll(reasons);
    }

    private void applyFailed(Analysis analysis, AnalysisResultCallback callback) {
        String errorCode = null;
        Integer errorStage = null;
        String errorMessage = null;

        if (callback.error() != null) {
            errorCode = callback.error().code();
            errorStage = callback.error().stage();
            errorMessage = callback.error().message();
        }

        analysis.updateToFailed(errorCode, errorStage, errorMessage,
                callback.engineVersion(), callback.analyzedAt(), callback.elapsedMs());
    }
}
