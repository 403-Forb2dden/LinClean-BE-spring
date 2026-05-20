package com.linclean.domain.notice.exception;

import com.linclean.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class NoticeException extends RuntimeException {
    private final ErrorCode errorCode;

    public NoticeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
