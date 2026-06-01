package com.linclean.domain.link.controller;

import com.linclean.domain.link.dto.request.CategoryUpdateRequest;
import com.linclean.domain.link.dto.request.SavedLinkCreateRequest;
import com.linclean.domain.link.dto.request.SavedLinkListQuery;
import com.linclean.domain.link.dto.request.SavedLinkTitleUpdateRequest;
import com.linclean.domain.link.dto.response.BookmarkToggleResponse;
import com.linclean.domain.link.dto.response.CategoryUpdateResponse;
import com.linclean.domain.link.dto.response.SavedLinkListResponse;
import com.linclean.domain.link.dto.response.SavedLinkResponse;
import com.linclean.domain.link.dto.response.SavedLinkTitleUpdateResponse;
import com.linclean.domain.link.dto.response.UrlCheckResponse;
import com.linclean.global.exception.ErrorResponse;
import com.linclean.global.web.ApiResponse;
import com.linclean.security.MemberPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "SavedLink", description = "저장 링크 API")
public interface SavedLinkControllerDocs {

    @Operation(summary = "링크 저장", description = "분석 완료된 링크를 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "분석 결과 또는 카테고리 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 저장된 분석",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "분석 미완료 또는 위험 URL",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<SavedLinkResponse>> createSavedLink(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody SavedLinkCreateRequest request
    );

    @Operation(summary = "저장 링크 목록 조회", description = "커서 기반 페이지네이션으로 저장 링크를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 쿼리 파라미터",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<SavedLinkListResponse>> getSavedLinks(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "조회 조건") @Valid @ModelAttribute SavedLinkListQuery query
    );

    @Operation(summary = "저장 링크 삭제", description = "저장 링크를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "저장 링크 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteSavedLink(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "저장 링크 ID") @PathVariable Long id
    );

    @Operation(summary = "북마크 토글", description = "북마크 상태를 전환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토글 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "저장 링크 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<BookmarkToggleResponse>> toggleBookmark(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "저장 링크 ID") @PathVariable Long id
    );

    @Operation(summary = "카테고리 변경", description = "저장 링크의 카테고리를 변경합니다. categoryId가 null이면 카테고리를 제거합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "저장 링크 또는 카테고리 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<CategoryUpdateResponse>> updateCategory(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "저장 링크 ID") @PathVariable Long id,
            @RequestBody CategoryUpdateRequest request
    );

    @Operation(summary = "URL 중복 체크", description = "입력한 URL이 이미 저장된 링크에 존재하는지 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "url 파라미터 누락 또는 빈 문자열",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<UrlCheckResponse>> checkUrl(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "확인할 URL") @RequestParam String url
    );

    @Operation(summary = "제목 변경", description = "저장 링크의 제목을 변경합니다. 동일 회원 내 중복된 제목은 허용하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 (빈 제목, 500자 초과)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "저장 링크 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복된 제목",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<SavedLinkTitleUpdateResponse>> updateTitle(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "저장 링크 ID") @PathVariable Long id,
            @Valid @RequestBody SavedLinkTitleUpdateRequest request
    );
}
