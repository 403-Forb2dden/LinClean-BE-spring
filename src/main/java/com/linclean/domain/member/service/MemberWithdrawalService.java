package com.linclean.domain.member.service;

import com.linclean.domain.member.entity.Member;
import com.linclean.domain.member.exception.MemberException;
import com.linclean.domain.member.infrastructure.ClerkManagementClient;
import com.linclean.domain.member.repository.MemberRepository;
import com.linclean.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private final MemberRepository memberRepository;
    private final ClerkManagementClient clerkManagementClient;

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        String clerkId = member.getClerkId();
        memberRepository.delete(member);
        log.info("회원 소프트 삭제 완료 - memberId={}", memberId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                clerkManagementClient.deleteUser(clerkId);
            }
        });
    }
}
