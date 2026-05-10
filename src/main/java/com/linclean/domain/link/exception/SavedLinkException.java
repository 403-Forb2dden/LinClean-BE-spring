package com.linclean.domain.link.exception;

import com.linclean.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SavedLinkException extends RuntimeException {

    private final ErrorCode errorCode;

    public SavedLinkException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}