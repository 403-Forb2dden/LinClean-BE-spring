package com.linclean.domain.member.service;

import com.linclean.domain.member.entity.Member;
import com.linclean.domain.member.exception.MemberException;
import com.linclean.domain.member.infrastructure.ClerkManagementClient;
import com.linclean.domain.member.repository.MemberRepository;
import com.linclean.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    ClerkManagementClient clerkManagementClient;

    @InjectMocks
    MemberWithdrawalService memberWithdrawalService;

    @BeforeEach
    void setUp() {
        // withdraw() 내부의 TransactionSynchronizationManager.registerSynchronization() 호출을 위해 필요
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Nested
    class withdraw {

        @Test
        void 존재하지_않는_회원_탈퇴시_MEMBER_NOT_FOUND_예외가_발생한다() {
            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberWithdrawalService.withdraw(999L))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> assertThat(((MemberException) e).getErrorCode())
                            .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
        }

        @Test
        void 정상_탈퇴시_회원_소프트삭제가_호출된다() {
            Member member = Member.builder().clerkId("test-clerk-id").build();
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            memberWithdrawalService.withdraw(1L);

            verify(memberRepository).delete(member);
        }
    }
}
