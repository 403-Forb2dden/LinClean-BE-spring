package com.linclean.domain.link.repository;

import com.linclean.domain.link.entity.SavedLink;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavedLinkRepository extends JpaRepository<SavedLink, Long> {

    Optional<SavedLink> findByIdAndMember_Id(Long id, Long memberId);

    @Query("""
            SELECT sl FROM SavedLink sl
            JOIN FETCH sl.analysis
            LEFT JOIN FETCH sl.category
            WHERE sl.member.id = :memberId
              AND (:categoryId IS NULL OR sl.category.id = :categoryId)
              AND (:bookmarked IS NULL OR sl.isBookmarked = :bookmarked)
              AND (:cursor IS NULL OR sl.id < :cursor)
            ORDER BY sl.id DESC
            """)
    List<SavedLink> findByFilters(
            @Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId,
            @Param("bookmarked") Boolean bookmarked,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
