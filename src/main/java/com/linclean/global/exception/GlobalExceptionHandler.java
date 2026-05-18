package com.linclean.global.exception;

import com.linclean.domain.analysis.exception.AnalysisException;
import com.linclean.domain.notice.exception.NoticeException;
import com.linclean.domain.terms.exception.TermsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAnalysis(AnalysisException e) {
        log.debug("분석 도메인 오류: {}", e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getDefaultMessage())
                .orElse("요청 파라미터가 유효하지 않습니다.");
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message, Instant.now()));
    }

    @ExceptionHandler(TermsException.class)
    public ResponseEntity<ErrorResponse> handleTerms(TermsException e) {
        log.debug("약관 도메인 오류: {}", e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(NoticeException.class)
    public ResponseEntity<ErrorResponse> handleNotice(NoticeException e) {
        log.debug("공지사항 도메인 오류: {}", e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse("VALIDATION_ERROR", e.getReason(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_PATH_VARIABLE", "유효하지 않은 경로 변수입니다.", Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.", Instant.now()));
    }
}
