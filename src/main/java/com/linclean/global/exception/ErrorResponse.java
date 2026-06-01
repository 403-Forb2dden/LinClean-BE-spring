package com.linclean.global.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), Instant.now());
    }
}
