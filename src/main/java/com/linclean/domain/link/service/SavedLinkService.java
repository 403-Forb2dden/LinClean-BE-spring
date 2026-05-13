package com.linclean.domain.link.service;

import com.linclean.domain.analysis.entity.Analysis;
import com.linclean.domain.analysis.entity.AnalysisStatus;
import com.linclean.domain.analysis.entity.Verdict;
import com.linclean.domain.analysis.exception.AnalysisException;
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
import com.linclean.domain.link.exception.SavedLinkException;
import com.linclean.domain.link.repository.CategoryRepository;
import com.linclean.domain.link.repository.SavedLinkRepository;
import com.linclean.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedLinkService {

    private final SavedLinkRepository savedLinkRepository;
    private final CategoryRepository categoryRepository;
    private final AnalysisRepository analysisRepository;

    @Transactional
    public SavedLinkResponse createSavedLink(Long memberId, SavedLinkCreateRequest request) {
        Analysis analysis = analysisRepository.findById(request.analysisId())
                .filter(a -> a.getMember().getId().equals(memberId))
                .orElseThrow(() -> new AnalysisException(ErrorCode.ANALYSIS_NOT_FOUND));

        if (analysis.getStatus() != AnalysisStatus.SUCCEEDED) {
            throw new SavedLinkException(ErrorCode.ANALYSIS_NOT_SUCCEEDED);
        }
        if (analysis.getVerdict() == Verdict.DANGER) {
            throw new SavedLinkException(ErrorCode.SAVED_LINK_FORBIDDEN_DANGER);
        }
        if (savedLinkRepository.existsByMember_IdAndAnalysis_AnalysisId(memberId, request.analysisId())) {
            throw new SavedLinkException(ErrorCode.SAVED_LINK_DUPLICATE);
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndMember_Id(request.categoryId(), memberId)
                    .orElseThrow(() -> new SavedLinkException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        SavedLink savedLink = SavedLink.builder()
                .member(analysis.getMember())
                .analysis(analysis)
                .category(category)
                .title(request.title())
                .description(request.description())
                .build();

        return SavedLinkResponse.from(savedLinkRepository.save(savedLink));
    }

    @Transactional(readOnly = true)
    public SavedLinkListResponse getSavedLinks(Long memberId, SavedLinkListQuery query) {

        Long cursorId = decodeCursor(query.cursor());
        int size = query.size();
        List<SavedLink> rows = savedLinkRepository.findByFilters(
                memberId, query.categoryId(), query.bookmarked(), cursorId,
                PageRequest.of(0, size + 1));

        boolean hasNext = rows.size() > size;
        List<SavedLink> page = hasNext ? rows.subList(0, size) : rows;
        String nextCursor = hasNext ? encodeCursor(page.get(page.size() - 1).getId()) : null;

        return new SavedLinkListResponse(
                page.stream().map(SavedLinkResponse::from).toList(),
                hasNext,
                nextCursor);
    }

    @Transactional
    public void deleteSavedLink(Long memberId, Long id) {
        SavedLink link = savedLinkRepository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new SavedLinkException(ErrorCode.SAVED_LINK_NOT_FOUND));
        savedLinkRepository.delete(link);
    }

    @Transactional
    public BookmarkToggleResponse toggleBookmark(Long memberId, Long id) {
        SavedLink link = savedLinkRepository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new SavedLinkException(ErrorCode.SAVED_LINK_NOT_FOUND));
        link.toggleBookmark();
        return new BookmarkToggleResponse(link.getId(), link.isBookmarked());
    }

    @Transactional
    public CategoryUpdateResponse updateCategory(Long memberId, Long id, CategoryUpdateRequest request) {
        SavedLink link = savedLinkRepository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new SavedLinkException(ErrorCode.SAVED_LINK_NOT_FOUND));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndMember_Id(request.categoryId(), memberId)
                    .orElseThrow(() -> new SavedLinkException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        link.updateCategory(category);
        Long resultCategoryId = link.getCategory() != null ? link.getCategory().getId() : null;
        return new CategoryUpdateResponse(link.getId(), resultCategoryId);
    }

    private Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            return Long.parseLong(
                    new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new SavedLinkException(ErrorCode.SAVED_LINK_INVALID_CURSOR);
        }
    }

    private String encodeCursor(Long id) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(id.toString().getBytes(StandardCharsets.UTF_8));
    }
}
