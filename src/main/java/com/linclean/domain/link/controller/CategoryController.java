package com.linclean.domain.link.controller;

import com.linclean.domain.link.dto.request.CategoryCreateRequest;
import com.linclean.domain.link.dto.request.CategoryRenameRequest;
import com.linclean.domain.link.dto.response.CategoryListResponse;
import com.linclean.domain.link.dto.response.CategoryRenameResponse;
import com.linclean.domain.link.dto.response.CategoryResponse;
import com.linclean.domain.link.service.CategoryService;
import com.linclean.global.web.ApiResponse;
import com.linclean.security.MemberPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryControllerDocs {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(categoryService.createCategory(principal.memberId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>> getCategories(
            @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(categoryService.getCategories(principal.memberId())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryRenameResponse>> renameCategory(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRenameRequest request) {
        return ResponseEntity.ok(ApiResponse.of(
                categoryService.renameCategory(principal.memberId(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long id) {
        categoryService.deleteCategory(principal.memberId(), id);
        return ResponseEntity.noContent().build();
    }
}
