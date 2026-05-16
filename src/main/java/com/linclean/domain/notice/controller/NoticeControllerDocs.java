package com.linclean.domain.notice.controller;

import com.linclean.domain.notice.dto.NoticeCursorPageResponse;
import com.linclean.domain.notice.dto.NoticeDetailResponse;
import com.linclean.global.exception.ErrorResponse;
import com.linclean.global.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Notice", description = "공지사항 API")
public interface NoticeControllerDocs {

    @Operation(summary = "공지사항 목록 조회", description = "커서 기반 페이지네이션으로 공지사항 목록을 조회합니다. 고정 공지 먼저, 이후 최신순.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<NoticeCursorPageResponse>> getNotices(
            @Parameter(description = "페이지네이션 커서 (첫 페이지는 생략)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기 (기본 20, 최대 50)") @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 ID로 본문을 포함한 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공지사항 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<NoticeDetailResponse>> getNotice(
            @Parameter(description = "공지사항 ID") @PathVariable Long id
    );
}
