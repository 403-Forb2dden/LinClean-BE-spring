package com.linclean.auth.exception;

import com.linclean.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class KakaoAuthException extends RuntimeException {
    private final ErrorCode errorCode;

    public KakaoAuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
