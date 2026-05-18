package com.linclean.domain.member.repository;

import com.linclean.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // @SQLRestriction("deleted_at IS NULL") 덕분에 자동으로 활성 회원만 조회됨
    Optional<Member> findByClerkId(String clerkId);

    // @SQLRestriction 우회: 탈퇴(soft-deleted) 회원 존재 여부 확인
    @Query(value = "SELECT COUNT(*) > 0 FROM member WHERE clerk_id = :clerkId AND deleted_at IS NOT NULL", nativeQuery = true)
    boolean existsWithdrawnByClerkId(@Param("clerkId") String clerkId);

    // native query로 @SQLRestriction, @SQLDelete 모두 우회하여 물리 삭제
    @Modifying
    @Query(
            value = "DELETE FROM member WHERE deleted_at IS NOT NULL AND deleted_at < now() - CAST(:retentionDays || ' days' AS INTERVAL)",
            nativeQuery = true
    )
    int hardDeleteExpiredMembers(@Param("retentionDays") int retentionDays);
}
