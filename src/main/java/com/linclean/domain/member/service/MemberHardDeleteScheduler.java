package com.linclean.domain.member.service;

import com.linclean.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberHardDeleteScheduler {

    private final MemberRepository memberRepository;

    @Value("${member.withdrawal.retention-days}")
    private int retentionDays;

    @Scheduled(cron = "${member.withdrawal.hard-delete-cron:0 0 3 * * *}")
    @Transactional
    public void hardDeleteExpiredMembers() {
        log.info("하드 삭제 스케줄러 시작 - retentionDays={}", retentionDays);
        try {
            int deleted = memberRepository.hardDeleteExpiredMembers(retentionDays);
            log.info("하드 삭제 완료 - deletedCount={}", deleted);
        } catch (Exception e) {
            log.error("하드 삭제 스케줄러 실패", e);
        }
    }
}
