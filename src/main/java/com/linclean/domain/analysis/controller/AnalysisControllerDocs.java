package com.linclean.domain.analysis.controller;

import com.linclean.domain.analysis.dto.request.AnalysisRequest;
import com.linclean.domain.analysis.dto.response.AnalysisResponse;
import com.linclean.domain.analysis.dto.response.VerdictStatisticsResponse;
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

import java.util.UUID;

@Tag(name = "Analysis", description = "URL 분석 API")
public interface AnalysisControllerDocs {

    @Operation(summary = "verdict 통계 조회", description = "전체 분석 결과의 verdict(safe/caution/danger) 건수를 반환합니다. 인증 불필요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResponse<VerdictStatisticsResponse>> getVerdictStatistics();

    @Operation(summary = "URL 분석 요청", description = "URL 안전성 분석을 요청합니다. 분석은 비동기로 수행됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "분석 요청 접수"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<AnalysisResponse>> requestAnalysis(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody AnalysisRequest request
    );

    @Operation(summary = "분석 결과 조회", description = "분석 ID로 분석 결과를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 분석 결과만 조회 가능",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "분석 결과를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<AnalysisResponse>> getAnalysis(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Parameter(description = "분석 ID") @PathVariable UUID analysisId
    );
}
