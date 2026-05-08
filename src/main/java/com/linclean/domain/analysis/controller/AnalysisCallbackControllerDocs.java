package com.linclean.domain.analysis.controller;

import com.linclean.domain.analysis.dto.callback.AnalysisResultCallback;
import com.linclean.domain.analysis.dto.callback.CallbackReceivedResponse;
import com.linclean.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Internal", description = "분석 엔진 내부 콜백 API")
public interface AnalysisCallbackControllerDocs {

    @Operation(summary = "분석 결과 콜백", description = "분석 엔진에서 분석 완료 결과를 전달합니다. (내부 전용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "콜백 수신 완료",
                    content = @Content(schema = @Schema(implementation = CallbackReceivedResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "내부 API 키 인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CallbackReceivedResponse> receiveResult(
            @Valid @RequestBody AnalysisResultCallback callback
    );
}
