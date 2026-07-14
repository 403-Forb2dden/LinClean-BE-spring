package com.linclean.domain.link.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 저장 링크 주간 재검사 스케줄러.
 * 기본 매주 월요일 04:00 실행 (cron 은 프로퍼티로 조정 가능).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SavedLinkRecheckScheduler {

    private final SavedLinkRecheckService recheckService;

    @Scheduled(cron = "${link.recheck.cron:0 0 4 * * MON}")
    public void recheckStaleSavedLinks() {
        log.info("저장 링크 재검사 스케줄러 시작");
        try {
            recheckService.runRecheck();
        } catch (Exception e) {
            log.error("저장 링크 재검사 스케줄러 실패", e);
        }
    }
}
