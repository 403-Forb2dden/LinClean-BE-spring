package com.linclean.domain.link.service;

import com.linclean.domain.link.dto.request.CategoryCreateRequest;
import com.linclean.domain.link.dto.request.CategoryRenameRequest;
import com.linclean.domain.link.dto.response.CategoryListResponse;
import com.linclean.domain.link.dto.response.CategoryRenameResponse;
import com.linclean.domain.link.dto.response.CategoryResponse;
import com.linclean.domain.link.entity.Category;
import com.linclean.domain.link.entity.SavedLink;
import com.linclean.domain.link.exception.CategoryException;
import com.linclean.domain.link.exception.SavedLinkException;
import com.linclean.domain.link.repository.CategoryRepository;
import com.linclean.domain.link.repository.SavedLinkRepository;
import com.linclean.domain.member.entity.Member;
import com.linclean.domain.member.repository.MemberRepository;
import com.linclean.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock SavedLinkRepository savedLinkRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks CategoryService categoryService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", 1L);
    }

    // ── createCategory ────────────────────────────────────────────────────

    @Nested
    class CreateCategory {

        @Test
        void success_withoutLinks() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(categoryRepository.existsByMember_IdAndName(1L, "업무")).willReturn(false);
            given(categoryRepository.countByMember_Id(1L)).willReturn(2L);
            Category saved = makeCategory(10L, "업무", 2);
            given(categoryRepository.save(any(Category.class))).willReturn(saved);

            CategoryResponse response = categoryService.createCategory(1L,
                    new CategoryCreateRequest("업무", null));

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.name()).isEqualTo("업무");
            assertThat(response.displayOrder()).isEqualTo(2);
            assertThat(response.linkCount()).isEqualTo(0L);
        }

        @Test
        void success_withLinks_assignsCategoryAndCountsLinks() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(categoryRepository.existsByMember_IdAndName(1L, "업무")).willReturn(false);
            given(categoryRepository.countByMember_Id(1L)).willReturn(0L);
            Category saved = makeCategory(10L, "업무", 0);
            given(categoryRepository.save(any(Category.class))).willReturn(saved);

            SavedLink link1 = makeSavedLink(1L);
            SavedLink link2 = makeSavedLink(2L);
            given(savedLinkRepository.findByIdAndMember_Id(1L, 1L)).willReturn(Optional.of(link1));
            given(savedLinkRepository.findByIdAndMember_Id(2L, 1L)).willReturn(Optional.of(link2));

            CategoryResponse response = categoryService.createCategory(1L,
                    new CategoryCreateRequest("업무", List.of(1L, 2L)));

            assertThat(response.linkCount()).isEqualTo(2L);
        }

        @Test
        void duplicateName_throws() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(categoryRepository.existsByMember_IdAndName(1L, "중복")).willReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(1L,
                    new CategoryCreateRequest("중복", null)))
                    .isInstanceOf(CategoryException.class)
                    .satisfies(ex -> assertThat(((CategoryException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_DUPLICATE_NAME));
        }

        @Test
        void linkNotFound_throws() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(categoryRepository.existsByMember_IdAndName(1L, "업무")).willReturn(false);
            given(categoryRepository.countByMember_Id(1L)).willReturn(0L);
            Category saved = makeCategory(10L, "업무", 0);
            given(categoryRepository.save(any(Category.class))).willReturn(saved);
            given(savedLinkRepository.findByIdAndMember_Id(99L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.createCategory(1L,
                    new CategoryCreateRequest("업무", List.of(99L))))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.SAVED_LINK_NOT_FOUND));
        }
    }

    // ── getCategories ─────────────────────────────────────────────────────

    @Nested
    class GetCategories {

        @Test
        void returnsAllCategoriesWithLinkCounts() {
            Category cat1 = makeCategory(1L, "업무", 0);
            Category cat2 = makeCategory(2L, "개인", 1);
            given(categoryRepository.findAllByMember_IdOrderByDisplayOrderAscCreatedAtAsc(1L))
                    .willReturn(List.of(cat1, cat2));
            given(savedLinkRepository.countByCategoryIds(List.of(1L, 2L)))
                    .willReturn(List.<Object[]>of(new Object[]{1L, 3L}));

            CategoryListResponse response = categoryService.getCategories(1L);

            assertThat(response.items()).hasSize(2);
            assertThat(response.items().get(0).linkCount()).isEqualTo(3L);
            assertThat(response.items().get(1).linkCount()).isEqualTo(0L);
        }

        @Test
        void emptyList_returnsEmptyItems() {
            given(categoryRepository.findAllByMember_IdOrderByDisplayOrderAscCreatedAtAsc(1L))
                    .willReturn(List.of());

            CategoryListResponse response = categoryService.getCategories(1L);

            assertThat(response.items()).isEmpty();
        }
    }

    // ── renameCategory ────────────────────────────────────────────────────

    @Nested
    class RenameCategory {

        @Test
        void success_renames() {
            Category cat = makeCategory(5L, "옛날이름", 0);
            given(categoryRepository.findById(5L)).willReturn(Optional.of(cat));
            given(categoryRepository.existsByMember_IdAndName(1L, "새이름")).willReturn(false);

            CategoryRenameResponse response = categoryService.renameCategory(1L, 5L,
                    new CategoryRenameRequest("새이름"));

            assertThat(response.id()).isEqualTo(5L);
            assertThat(response.name()).isEqualTo("새이름");
        }

        @Test
        void categoryNotFound_throws() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.renameCategory(1L, 99L,
                    new CategoryRenameRequest("새이름")))
                    .isInstanceOf(CategoryException.class)
                    .satisfies(ex -> assertThat(((CategoryException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND));
        }

        @Test
        void otherMemberCategory_throwsForbidden() {
            Category cat = makeCategory(5L, "옛날이름", 0);
            Member other = Member.builder().build();
            ReflectionTestUtils.setField(other, "id", 2L);
            ReflectionTestUtils.setField(cat, "member", other);
            given(categoryRepository.findById(5L)).willReturn(Optional.of(cat));

            assertThatThrownBy(() -> categoryService.renameCategory(1L, 5L,
                    new CategoryRenameRequest("새이름")))
                    .isInstanceOf(CategoryException.class)
                    .satisfies(ex -> assertThat(((CategoryException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_FORBIDDEN));
        }

        @Test
        void duplicateName_throws() {
            Category cat = makeCategory(5L, "옛날이름", 0);
            given(categoryRepository.findById(5L)).willReturn(Optional.of(cat));
            given(categoryRepository.existsByMember_IdAndName(1L, "중복")).willReturn(true);

            assertThatThrownBy(() -> categoryService.renameCategory(1L, 5L,
                    new CategoryRenameRequest("중복")))
                    .isInstanceOf(CategoryException.class)
                    .satisfies(ex -> assertThat(((CategoryException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_DUPLICATE_NAME));
        }
    }

    // ── deleteCategory ────────────────────────────────────────────────────

    @Nested
    class DeleteCategory {

        @Test
        void success_clearsCategoryFromLinksAndDeletes() {
            Category cat = makeCategory(5L, "업무", 0);
            given(categoryRepository.findById(5L)).willReturn(Optional.of(cat));

            categoryService.deleteCategory(1L, 5L);

            then(savedLinkRepository).should().clearCategoryByCategoryId(5L);
            then(categoryRepository).should().delete(cat);
        }

        @Test
        void categoryNotFound_throws() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(1L, 99L))
                    .isInstanceOf(CategoryException.class)
                    .satisfies(ex -> assertThat(((CategoryException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND));
        }

        @Test
        void otherMemberCategory_throwsForbidden() {
            Category cat = makeCategory(5L, "업무", 0);
            Member other = Member.builder().build();
            ReflectionTestUtils.setField(other, "id", 2L);
            ReflectionTestUtils.setField(cat, "member", other);
            given(categoryRepository.findById(5L)).willReturn(Optional.of(cat));

            assertThatThrownBy(() -> categoryService.deleteCategory(1L, 5L))
                    .isInstanceOf(CategoryException.class)
                    .satisfies(ex -> assertThat(((CategoryException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_FORBIDDEN));
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private Category makeCategory(long id, String name, int displayOrder) {
        Category cat = Category.builder()
                .member(member)
                .name(name)
                .displayOrder(displayOrder)
                .build();
        ReflectionTestUtils.setField(cat, "id", id);
        ReflectionTestUtils.setField(cat, "createdAt", Instant.now());
        return cat;
    }

    private SavedLink makeSavedLink(long id) {
        com.linclean.domain.analysis.entity.Analysis analysis =
                com.linclean.domain.analysis.entity.Analysis.builder()
                        .member(member)
                        .originalUrl("https://example.com")
                        .build();
        SavedLink link = SavedLink.builder()
                .member(member)
                .analysis(analysis)
                .build();
        ReflectionTestUtils.setField(link, "id", id);
        return link;
    }
}
