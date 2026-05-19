package com.linclean.domain.link.entity;

import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.global.entity.BaseEntity;
import com.linclean.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "saved_link",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_saved_link_member_analysis",
                        columnNames = {"member_id", "analysis_id"}),
                @UniqueConstraint(name = "uq_saved_link_member_title",
                        columnNames = {"member_id", "title"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SavedLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_bookmarked", nullable = false)
    @Builder.Default
    private boolean isBookmarked = false;

    public void toggleBookmark() {
        this.isBookmarked = !this.isBookmarked;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }
}
