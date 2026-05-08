package com.linclean.domain.terms.controller;

import com.linclean.domain.terms.dto.TermsResponse;
import com.linclean.domain.terms.entity.TermsType;
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

@Tag(name = "Terms", description = "약관 API")
public interface TermsControllerDocs {

    @Operation(summary = "약관 조회", description = "타입별 약관 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 약관 타입",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "약관 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<TermsResponse>> getTerms(
            @Parameter(description = "약관 타입 (terms_of_service | privacy_policy | service_guide)")
            @PathVariable TermsType type
    );
}
