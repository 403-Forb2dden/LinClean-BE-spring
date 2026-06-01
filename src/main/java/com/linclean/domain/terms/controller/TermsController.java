package com.linclean.domain.terms.controller;

import com.linclean.domain.terms.dto.TermsResponse;
import com.linclean.domain.terms.entity.TermsType;
import com.linclean.domain.terms.service.TermsService;
import com.linclean.global.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController implements TermsControllerDocs {

    private final TermsService termsService;

    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<TermsResponse>> getTerms(@PathVariable TermsType type) {
        return ResponseEntity.ok(ApiResponse.of(termsService.getTerms(type)));
    }
}
