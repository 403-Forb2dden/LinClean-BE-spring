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

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_001", "공지사항을 찾을 수 없습니다."),
    NOTICE_INVALID_CURSOR(HttpStatus.BAD_REQUEST, "NOTICE_002", "유효하지 않은 커서입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
