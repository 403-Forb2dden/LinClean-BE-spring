package com.linclean.security;

import com.linclean.domain.member.entity.Member;
import com.linclean.domain.member.exception.WithdrawnMemberException;
import com.linclean.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSyncService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member findOrCreate(String clerkId) {
        if (memberRepository.existsWithdrawnByClerkId(clerkId)) {
            throw new WithdrawnMemberException();
        }
        try {
            return memberRepository.findByClerkId(clerkId)
                    .orElseGet(() -> memberRepository.save(
                            Member.builder().clerkId(clerkId).build()
                    ));
        } catch (DataIntegrityViolationException e) {
            return memberRepository.findByClerkId(clerkId)
                    .orElseThrow(() -> e);
        }
    }
}
