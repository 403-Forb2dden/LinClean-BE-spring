package com.linclean.domain.link.service;

import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.entity.AnalysisStatus;
import com.linclean.domain.analysis.entity.Verdict;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import com.linclean.domain.link.dto.request.CategoryUpdateRequest;
import com.linclean.domain.link.dto.request.SavedLinkCreateRequest;
import com.linclean.domain.link.dto.request.SavedLinkListQuery;
import com.linclean.domain.link.dto.response.BookmarkToggleResponse;
import com.linclean.domain.link.dto.response.CategoryUpdateResponse;
import com.linclean.domain.link.dto.response.SavedLinkListResponse;
import com.linclean.domain.link.dto.response.SavedLinkResponse;
import com.linclean.domain.link.entity.Category;
import com.linclean.domain.link.entity.SavedLink;
import com.linclean.domain.analysis.exception.AnalysisException;
import com.linclean.domain.link.exception.SavedLinkException;
import com.linclean.domain.link.repository.CategoryRepository;
import com.linclean.domain.link.repository.SavedLinkRepository;
import com.linclean.domain.member.entity.Member;
import com.linclean.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SavedLinkServiceTest {

    @Mock SavedLinkRepository savedLinkRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock AnalysisRepository analysisRepository;
    @InjectMocks SavedLinkService savedLinkService;

    private Member member;
    private Analysis succeededSafeAnalysis;
    private UUID analysisUuid;

    @BeforeEach
    void setUp() {
        member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", 1L);

        analysisUuid = UUID.randomUUID();
        succeededSafeAnalysis = Analysis.builder()
                .member(member)
                .originalUrl("https://example.com")
                .build();
        ReflectionTestUtils.setField(succeededSafeAnalysis, "analysisId", analysisUuid);
        ReflectionTestUtils.setField(succeededSafeAnalysis, "status", AnalysisStatus.SUCCEEDED);
        ReflectionTestUtils.setField(succeededSafeAnalysis, "verdict", Verdict.SAFE);
        ReflectionTestUtils.setField(succeededSafeAnalysis, "finalUrl", "https://example.com/final");
    }

    // ── createSavedLink ───────────────────────────────────────────────────

    @Nested
    class CreateSavedLink {

        @Test
        void success_withoutCategory() {
            given(analysisRepository.findById(analysisUuid)).willReturn(Optional.of(succeededSafeAnalysis));

            SavedLink saved = makeSavedLink(10L, null);
            given(savedLinkRepository.save(any(SavedLink.class))).willReturn(saved);

            SavedLinkResponse response = savedLinkService.createSavedLink(1L,
                    new SavedLinkCreateRequest(analysisUuid, null, "제목", "설명"));

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.categoryId()).isNull();
            assertThat(response.analysisId()).isEqualTo(analysisUuid);
        }

        @Test
        void success_withCategory() {
            Category category = makeCategory(5L);
            given(analysisRepository.findById(analysisUuid)).willReturn(Optional.of(succeededSafeAnalysis));
            given(categoryRepository.findByIdAndMember_Id(5L, 1L)).willReturn(Optional.of(category));

            SavedLink saved = makeSavedLink(11L, category);
            given(savedLinkRepository.save(any(SavedLink.class))).willReturn(saved);

            SavedLinkResponse response = savedLinkService.createSavedLink(1L,
                    new SavedLinkCreateRequest(analysisUuid, 5L, "제목", "설명"));

            assertThat(response.categoryId()).isEqualTo(5L);
        }

        @Test
        void analysisNotFound_throws() {
            given(analysisRepository.findById(analysisUuid)).willReturn(Optional.empty());

            assertThatThrownBy(() -> savedLinkService.createSavedLink(1L,
                    new SavedLinkCreateRequest(analysisUuid, null, null, null)))
                    .isInstanceOf(AnalysisException.class)
                    .satisfies(ex -> assertThat(((AnalysisException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ANALYSIS_NOT_FOUND));
        }

        @Test
        void analysisOwnedByOther_throwsNotFound() {
            Member other = Member.builder().build();
            ReflectionTestUtils.setField(other, "id", 99L);
            ReflectionTestUtils.setField(succeededSafeAnalysis, "member", other);
            given(analysisRepository.findById(analysisUuid)).willReturn(Optional.of(succeededSafeAnalysis));

            assertThatThrownBy(() -> savedLinkService.createSavedLink(1L,
                    new SavedLinkCreateRequest(analysisUuid, null, null, null)))
                    .isInstanceOf(AnalysisException.class)
                    .satisfies(ex -> assertThat(((AnalysisException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ANALYSIS_NOT_FOUND));
        }

        @Test
        void analysisQueued_throwsNotSucceeded() {
            ReflectionTestUtils.setField(succeededSafeAnalysis, "status", AnalysisStatus.QUEUED);
            given(analysisRepository.findById(analysisUuid)).willReturn(Optional.of(succeededSafeAnalysis));

            assertThatThrownBy(() -> savedLinkService.createSavedLink(1L,
                    new SavedLinkCreateRequest(analysisUuid, null, null, null)))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ANALYSIS_NOT_SUCCEEDED));
        }

        @Test
        void analysisDanger_throwsForbiddenDanger() {
            ReflectionTestUtils.setField(succeededSafeAnalysis, "verdict", Verdict.DANGER);
            given(analysisRepository.findById(analysisUuid)).willReturn(Optional.of(succeededSafeAnalysis));

            assertThatThrownBy(() -> savedLinkService.createSavedLink(1L,
                    new SavedLinkCreateRequest(analysisUuid, null, null, null)))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.SAVED_LINK_FORBIDDEN_DANGER));
        }

        @Test
        void categoryNotFound_throws() {
            given(analysisRepository.findById(analysisUuid)).willReturn(Optional.of(succeededSafeAnalysis));
            given(categoryRepository.findByIdAndMember_Id(99L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> savedLinkService.createSavedLink(1L,
                    new SavedLinkCreateRequest(analysisUuid, 99L, null, null)))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND));
        }

        @Test
        void duplicate_throws() {
            given(analysisRepository.findById(analysisUuid)).willReturn(Optional.of(succeededSafeAnalysis));
            given(savedLinkRepository.existsByMember_IdAndAnalysis_AnalysisId(1L, analysisUuid))
                    .willReturn(true);

            assertThatThrownBy(() -> savedLinkService.createSavedLink(1L,
                    new SavedLinkCreateRequest(analysisUuid, null, null, null)))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.SAVED_LINK_DUPLICATE));
        }
    }

    // ── getSavedLinks ─────────────────────────────────────────────────────

    @Nested
    class GetSavedLinks {

        @Test
        void noFilters_firstPage_hasNextFalse() {
            given(savedLinkRepository.findByFilters(eq(1L), eq(null), eq(null), eq(null), any(Pageable.class)))
                    .willReturn(List.of(makeSavedLink(10L, null)));

            SavedLinkListResponse response = savedLinkService.getSavedLinks(1L,
                    new SavedLinkListQuery(null, null, null, 20));

            assertThat(response.items()).hasSize(1);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        void hasNextTrue_returnsEncodedCursor() {
            List<SavedLink> rows = List.of(
                    makeSavedLink(30L, null),
                    makeSavedLink(20L, null),
                    makeSavedLink(10L, null));
            given(savedLinkRepository.findByFilters(eq(1L), eq(null), eq(null), eq(null), any(Pageable.class)))
                    .willReturn(rows);

            SavedLinkListResponse response = savedLinkService.getSavedLinks(1L,
                    new SavedLinkListQuery(null, null, null, 2));

            assertThat(response.items()).hasSize(2);
            assertThat(response.hasNext()).isTrue();
            String expected = Base64.getUrlEncoder().withoutPadding().encodeToString("20".getBytes());
            assertThat(response.nextCursor()).isEqualTo(expected);
        }

        @Test
        void cursorProvided_decodesAndPassesToRepository() {
            String cursor = Base64.getUrlEncoder().withoutPadding().encodeToString("50".getBytes());
            given(savedLinkRepository.findByFilters(eq(1L), eq(null), eq(null), eq(50L), any(Pageable.class)))
                    .willReturn(List.of());

            SavedLinkListResponse response = savedLinkService.getSavedLinks(1L,
                    new SavedLinkListQuery(null, null, cursor, 20));

            assertThat(response.items()).isEmpty();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        void sizeDefaultsTo20WhenNull() {
            SavedLinkListQuery query = new SavedLinkListQuery(null, null, null, null);
            assertThat(query.size()).isEqualTo(20);
        }
    }

    // ── deleteSavedLink ───────────────────────────────────────────────────

    @Nested
    class DeleteSavedLink {

        @Test
        void success_deletesLink() {
            SavedLink link = makeSavedLink(10L, null);
            given(savedLinkRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(link));

            savedLinkService.deleteSavedLink(1L, 10L);

            then(savedLinkRepository).should().delete(link);
        }

        @Test
        void notFound_throws() {
            given(savedLinkRepository.findByIdAndMember_Id(99L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> savedLinkService.deleteSavedLink(1L, 99L))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.SAVED_LINK_NOT_FOUND));
        }
    }

    // ── toggleBookmark ────────────────────────────────────────────────────

    @Nested
    class ToggleBookmark {

        @Test
        void falseToTrue() {
            SavedLink link = makeSavedLink(10L, null);
            given(savedLinkRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(link));

            BookmarkToggleResponse response = savedLinkService.toggleBookmark(1L, 10L);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.isBookmarked()).isTrue();
        }

        @Test
        void notFound_throws() {
            given(savedLinkRepository.findByIdAndMember_Id(99L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> savedLinkService.toggleBookmark(1L, 99L))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.SAVED_LINK_NOT_FOUND));
        }
    }

    // ── updateCategory ────────────────────────────────────────────────────

    @Nested
    class UpdateCategory {

        @Test
        void assignCategory() {
            Category category = makeCategory(5L);
            SavedLink link = makeSavedLink(10L, null);
            given(savedLinkRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(link));
            given(categoryRepository.findByIdAndMember_Id(5L, 1L)).willReturn(Optional.of(category));

            CategoryUpdateResponse response = savedLinkService.updateCategory(1L, 10L, new CategoryUpdateRequest(5L));

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.categoryId()).isEqualTo(5L);
        }

        @Test
        void removeCategory_setNull() {
            Category category = makeCategory(5L);
            SavedLink link = makeSavedLink(10L, category);
            given(savedLinkRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(link));

            CategoryUpdateResponse response = savedLinkService.updateCategory(1L, 10L, new CategoryUpdateRequest(null));

            assertThat(response.categoryId()).isNull();
        }

        @Test
        void categoryNotFound_throws() {
            SavedLink link = makeSavedLink(10L, null);
            given(savedLinkRepository.findByIdAndMember_Id(10L, 1L)).willReturn(Optional.of(link));
            given(categoryRepository.findByIdAndMember_Id(99L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> savedLinkService.updateCategory(1L, 10L, new CategoryUpdateRequest(99L)))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND));
        }

        @Test
        void savedLinkNotFound_throws() {
            given(savedLinkRepository.findByIdAndMember_Id(99L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> savedLinkService.updateCategory(1L, 99L, new CategoryUpdateRequest(5L)))
                    .isInstanceOf(SavedLinkException.class)
                    .satisfies(ex -> assertThat(((SavedLinkException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.SAVED_LINK_NOT_FOUND));
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────

    private SavedLink makeSavedLink(long id, Category category) {
        SavedLink link = SavedLink.builder()
                .member(member)
                .analysis(succeededSafeAnalysis)
                .category(category)
                .title("제목")
                .description("설명")
                .build();
        ReflectionTestUtils.setField(link, "id", id);
        return link;
    }

    private Category makeCategory(long id) {
        Category category = Category.builder().member(member).name("테스트").build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
