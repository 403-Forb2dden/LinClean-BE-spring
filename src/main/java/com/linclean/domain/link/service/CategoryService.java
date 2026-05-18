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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SavedLinkRepository savedLinkRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CategoryResponse createCategory(Long memberId, CategoryCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CategoryException(ErrorCode.MEMBER_NOT_FOUND));

        if (categoryRepository.existsByMember_IdAndName(memberId, request.name())) {
            throw new CategoryException(ErrorCode.CATEGORY_DUPLICATE_NAME);
        }

        int displayOrder = (int) categoryRepository.countByMember_Id(memberId);

        Category category = Category.builder()
                .member(member)
                .name(request.name())
                .displayOrder(displayOrder)
                .build();

        Category saved = categoryRepository.save(category);

        long linkCount = 0;
        if (request.linkIds() != null && !request.linkIds().isEmpty()) {
            for (Long linkId : request.linkIds()) {
                SavedLink link = savedLinkRepository.findByIdAndMember_Id(linkId, memberId)
                        .orElseThrow(() -> new SavedLinkException(ErrorCode.SAVED_LINK_NOT_FOUND));
                link.updateCategory(saved);
            }
            linkCount = request.linkIds().size();
        }

        return CategoryResponse.of(saved, linkCount);
    }

    @Transactional(readOnly = true)
    public CategoryListResponse getCategories(Long memberId) {
        List<Category> categories =
                categoryRepository.findAllByMember_IdOrderByDisplayOrderAscCreatedAtAsc(memberId);

        if (categories.isEmpty()) {
            return new CategoryListResponse(List.of());
        }

        List<Long> categoryIds = categories.stream().map(Category::getId).toList();
        Map<Long, Long> countMap = savedLinkRepository.countByCategoryIds(categoryIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        List<CategoryResponse> items = categories.stream()
                .map(cat -> CategoryResponse.of(cat, countMap.getOrDefault(cat.getId(), 0L)))
                .toList();

        return new CategoryListResponse(items);
    }

    @Transactional
    public CategoryRenameResponse renameCategory(Long memberId, Long categoryId, CategoryRenameRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getMember().getId().equals(memberId)) {
            throw new CategoryException(ErrorCode.CATEGORY_FORBIDDEN);
        }

        if (categoryRepository.existsByMember_IdAndName(memberId, request.name())) {
            throw new CategoryException(ErrorCode.CATEGORY_DUPLICATE_NAME);
        }

        category.rename(request.name());
        return new CategoryRenameResponse(category.getId(), category.getName());
    }

    @Transactional
    public void deleteCategory(Long memberId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getMember().getId().equals(memberId)) {
            throw new CategoryException(ErrorCode.CATEGORY_FORBIDDEN);
        }

        savedLinkRepository.clearCategoryByCategoryId(categoryId);
        categoryRepository.delete(category);
    }
}
