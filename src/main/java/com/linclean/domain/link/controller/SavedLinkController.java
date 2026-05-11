package com.linclean.domain.link.controller;

import com.linclean.domain.link.dto.request.CategoryUpdateRequest;
import com.linclean.domain.link.dto.request.SavedLinkCreateRequest;
import com.linclean.domain.link.dto.response.BookmarkToggleResponse;
import com.linclean.domain.link.dto.response.CategoryUpdateResponse;
import com.linclean.domain.link.dto.response.SavedLinkListResponse;
import com.linclean.domain.link.dto.response.SavedLinkResponse;
import com.linclean.domain.link.service.SavedLinkService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/saved-links")
@RequiredArgsConstructor
public class SavedLinkController implements SavedLinkControllerDocs {

    private final SavedLinkService savedLinkService;

    @PostMapping
    public ResponseEntity<ApiResponse<SavedLinkResponse>> createSavedLink(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody SavedLinkCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(savedLinkService.createSavedLink(principal.memberId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SavedLinkListResponse>> getSavedLinks(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean bookmarked,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.of(
                savedLinkService.getSavedLinks(principal.memberId(), categoryId, bookmarked, cursor, size)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavedLink(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long id) {
        savedLinkService.deleteSavedLink(principal.memberId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<BookmarkToggleResponse>> toggleBookmark(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(savedLinkService.toggleBookmark(principal.memberId(), id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryUpdateResponse>> updateCategory(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long id,
            @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.of(savedLinkService.updateCategory(principal.memberId(), id, request)));
    }
}
