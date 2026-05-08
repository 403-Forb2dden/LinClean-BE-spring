package com.linclean.domain.analysis.service;

import com.linclean.domain.analysis.entity.AnalysisStatus;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisAsyncRunner {

    private final AnalysisRepository analysisRepository;
    private final WebClient analysisEngineWebClient;

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Async("analysisTaskExecutor")
    @Transactional
    public void run(UUID analysisId, String originalUrl, UUID requestId) {
        try {
            analysisEngineWebClient.post()
                    .uri("/api/v1/analyze")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .header("X-Request-ID", requestId.toString())
                    .bodyValue(Map.of(
                            "analysisId", analysisId.toString(),
                            "url", originalUrl
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("분석 위임 완료 - analysisId={}", analysisId);

        } catch (WebClientResponseException e) {
            log.error("FastAPI 호출 실패 - analysisId={}, status={}, body={}",
                    analysisId, e.getStatusCode(), e.getResponseBodyAsString());
            markFailed(analysisId, "ENGINE_REQUEST_FAILED",
                    "분석 엔진 호출 실패: " + e.getStatusCode());

        } catch (Exception e) {
            log.error("FastAPI 호출 중 예외 - analysisId={}", analysisId, e);
            markFailed(analysisId, "ENGINE_REQUEST_ERROR", "분석 엔진 연결 오류");
        }
    }

    private void markFailed(UUID analysisId, String errorCode, String errorMessage) {
        analysisRepository.findById(analysisId).ifPresent(analysis -> {
            if (analysis.getStatus() == AnalysisStatus.QUEUED) {
                analysis.updateToFailed(errorCode, null, errorMessage,
                        null, Instant.now(), null);
            }
        });
    }
}
