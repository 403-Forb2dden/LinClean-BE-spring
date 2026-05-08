package com.linclean.domain.terms.exception;

import com.linclean.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class TermsException extends RuntimeException {

    private final ErrorCode errorCode;

    public TermsException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
