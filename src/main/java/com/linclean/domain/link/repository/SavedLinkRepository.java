package com.linclean.domain.link.repository;

import com.linclean.domain.link.entity.SavedLink;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavedLinkRepository extends JpaRepository<SavedLink, Long> {

    Optional<SavedLink> findByIdAndMember_Id(Long id, Long memberId);

    boolean existsByMember_IdAndAnalysis_AnalysisId(Long memberId, java.util.UUID analysisId);

    boolean existsByMember_IdAndAnalysis_OriginalUrl(Long memberId, String url);

    boolean existsByMember_IdAndTitle(Long memberId, String title);

    boolean existsByMember_IdAndTitleAndIdNot(Long memberId, String title, Long id);

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

    @Query("SELECT sl.category.id, COUNT(sl) FROM SavedLink sl WHERE sl.category.id IN :categoryIds GROUP BY sl.category.id")
    List<Object[]> countByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    @Modifying
    @Query("UPDATE SavedLink sl SET sl.category = null WHERE sl.category.id = :categoryId")
    void clearCategoryByCategoryId(@Param("categoryId") Long categoryId);
}
