package com.linclean.domain.notice.controller;

import com.linclean.domain.notice.dto.NoticeCursorPageResponse;
import com.linclean.domain.notice.dto.NoticeDetailResponse;
import com.linclean.domain.notice.service.NoticeService;
import com.linclean.global.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Validated
public class NoticeController implements NoticeControllerDocs {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<NoticeCursorPageResponse>> getNotices(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.of(noticeService.getNotices(cursor, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(noticeService.getNotice(id)));
    }
}
