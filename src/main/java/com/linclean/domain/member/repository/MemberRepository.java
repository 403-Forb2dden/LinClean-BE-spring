package com.linclean.domain.member.repository;

import com.linclean.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // @SQLRestriction("deleted_at IS NULL") 덕분에 자동으로 활성 회원만 조회됨
    Optional<Member> findByKakaoId(String kakaoId);
}
