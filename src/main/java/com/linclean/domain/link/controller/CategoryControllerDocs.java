package com.linclean.domain.link.controller;

import com.linclean.domain.link.dto.request.CategoryCreateRequest;
import com.linclean.domain.link.dto.request.CategoryRenameRequest;
import com.linclean.domain.link.dto.response.CategoryListResponse;
import com.linclean.domain.link.dto.response.CategoryRenameResponse;
import com.linclean.domain.link.dto.response.CategoryResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Category", description = "카테고리(폴더) API")
public interface CategoryControllerDocs {

    @Operation(summary = "카테고리 생성", description = "새 카테고리를 생성하고 선택적으로 링크를 배치합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "링크를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이름 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody CategoryCreateRequest request
    );

    @Operation(summary = "카테고리 목록 조회", description = "회원의 카테고리를 displayOrder → createdAt 순으로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResponse<CategoryListResponse>> getCategories(
            @AuthenticationPrincipal MemberPrincipal principal
    );

    @Operation(summary = "카테고리 이름 변경")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이름 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<CategoryRenameResponse>> renameCategory(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "카테고리 ID") @PathVariable Long id,
            @Valid @RequestBody CategoryRenameRequest request
    );

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. 소속 링크의 카테고리는 null로 변경됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "카테고리 ID") @PathVariable Long id
    );
}
