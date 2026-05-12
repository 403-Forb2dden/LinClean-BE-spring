package com.linclean.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_001", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "AUTH_003", "Refresh Token 재사용이 감지되었습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "존재하지 않는 회원입니다."),

    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_001", "분석 결과를 찾을 수 없습니다."),
    ANALYSIS_FORBIDDEN(HttpStatus.FORBIDDEN, "ANALYSIS_002", "본인의 분석 결과만 조회할 수 있습니다."),
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS_001", "약관 정보를 찾을 수 없습니다."),

    ANALYSIS_NOT_SUCCEEDED(HttpStatus.UNPROCESSABLE_ENTITY, "ANALYSIS_003", "분석이 완료된 결과만 저장할 수 있습니다."),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_001", "카테고리를 찾을 수 없습니다."),

    SAVED_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "SAVED_LINK_001", "저장된 링크를 찾을 수 없습니다."),
    SAVED_LINK_FORBIDDEN_DANGER(HttpStatus.UNPROCESSABLE_ENTITY, "SAVED_LINK_002", "위험으로 분류된 URL은 저장할 수 없습니다."),
    SAVED_LINK_INVALID_CURSOR(HttpStatus.BAD_REQUEST, "SAVED_LINK_003", "유효하지 않은 커서 값입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
