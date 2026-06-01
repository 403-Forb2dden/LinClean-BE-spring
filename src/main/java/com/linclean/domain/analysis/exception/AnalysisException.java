package com.linclean.domain.analysis.exception;

import com.linclean.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AnalysisException extends RuntimeException {

    private final ErrorCode errorCode;

    public AnalysisException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
