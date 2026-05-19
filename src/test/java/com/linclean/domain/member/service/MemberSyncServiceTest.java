package com.linclean.domain.member.service;

import com.linclean.domain.member.entity.Member;
import com.linclean.domain.member.exception.WithdrawnMemberException;
import com.linclean.domain.member.repository.MemberRepository;
import com.linclean.security.MemberSyncService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberSyncServiceTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberSyncService memberSyncService;

    @Nested
    class findOrCreate {

        @Test
        void 탈퇴한_clerkId로_인증시_WithdrawnMemberException이_발생한다() {
            given(memberRepository.existsWithdrawnByClerkId("withdrawn-clerk-id")).willReturn(true);

            assertThatThrownBy(() -> memberSyncService.findOrCreate("withdrawn-clerk-id"))
                    .isInstanceOf(WithdrawnMemberException.class);

            verify(memberRepository, never()).findByClerkId(any());
            verify(memberRepository, never()).save(any());
        }

        @Test
        void 기존_회원은_탈퇴_여부_확인_후_그대로_반환한다() {
            Member member = Member.builder().clerkId("existing-clerk-id").build();
            given(memberRepository.existsWithdrawnByClerkId("existing-clerk-id")).willReturn(false);
            given(memberRepository.findByClerkId("existing-clerk-id")).willReturn(Optional.of(member));

            Member result = memberSyncService.findOrCreate("existing-clerk-id");

            assertThat(result.getClerkId()).isEqualTo("existing-clerk-id");
            verify(memberRepository, never()).save(any());
        }

        @Test
        void 신규_clerkId는_탈퇴_여부_확인_후_새_회원을_생성한다() {
            Member newMember = Member.builder().clerkId("new-clerk-id").build();
            given(memberRepository.existsWithdrawnByClerkId("new-clerk-id")).willReturn(false);
            given(memberRepository.findByClerkId("new-clerk-id")).willReturn(Optional.empty());
            given(memberRepository.save(any())).willReturn(newMember);

            Member result = memberSyncService.findOrCreate("new-clerk-id");

            assertThat(result.getClerkId()).isEqualTo("new-clerk-id");
            verify(memberRepository).save(any());
        }

        @Test
        void 동시_생성_충돌시_DataIntegrityViolationException을_잡고_재조회한_회원을_반환한다() {
            Member savedMember = Member.builder().clerkId("race-clerk-id").build();
            given(memberRepository.existsWithdrawnByClerkId("race-clerk-id")).willReturn(false);
            given(memberRepository.findByClerkId("race-clerk-id"))
                    .willReturn(Optional.empty())             // 1차 조회: 없음
                    .willReturn(Optional.of(savedMember));   // 예외 후 재조회: 있음
            given(memberRepository.save(any())).willThrow(new DataIntegrityViolationException("duplicate"));

            Member result = memberSyncService.findOrCreate("race-clerk-id");

            assertThat(result.getClerkId()).isEqualTo("race-clerk-id");
        }
    }
}
