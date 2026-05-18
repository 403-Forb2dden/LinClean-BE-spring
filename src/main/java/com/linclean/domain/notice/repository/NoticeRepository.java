package com.linclean.domain.notice.repository;

import com.linclean.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 고정 공지 먼저, 이후 최신순으로 첫 페이지 조회
    @Query("""
            SELECT n FROM Notice n
            ORDER BY n.isPinned DESC, n.createdAt DESC, n.id DESC
            LIMIT :limit
            """)
    List<Notice> findFirstPage(@Param("limit") int limit);

    // 커서(lastIsPinned, lastCreatedAt, lastId) 기준 다음 페이지 keyset 조회
    // - 커서가 pinned 항목이면: 남은 pinned + 전체 unpinned 포함
    // - 커서가 unpinned 항목이면: 해당 시점 이후 unpinned만 조회
    @Query("""
            SELECT n FROM Notice n
            WHERE (n.isPinned = true AND :lastIsPinned = true
                       AND (n.createdAt < :lastCreatedAt
                            OR (n.createdAt = :lastCreatedAt AND n.id < :lastId)))
               OR (n.isPinned = false AND :lastIsPinned = true)
               OR (n.isPinned = false AND :lastIsPinned = false
                       AND (n.createdAt < :lastCreatedAt
                            OR (n.createdAt = :lastCreatedAt AND n.id < :lastId)))
            ORDER BY n.isPinned DESC, n.createdAt DESC, n.id DESC
            LIMIT :limit
            """)
    List<Notice> findNextPage(
            @Param("lastIsPinned") boolean lastIsPinned,
            @Param("lastCreatedAt") Instant lastCreatedAt,
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );
}
