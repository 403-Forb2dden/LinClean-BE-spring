package com.linclean.domain.link.exception;

import com.linclean.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class CategoryException extends RuntimeException {

    private final ErrorCode errorCode;

    public CategoryException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
