package com.linclean.domain.terms.controller;

import com.linclean.domain.terms.dto.TermsResponse;
import com.linclean.domain.terms.entity.TermsType;
import com.linclean.domain.terms.service.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController implements TermsControllerDocs {

    private final TermsService termsService;

    @GetMapping("/{type}")
    public ResponseEntity<TermsResponse> getTerms(@PathVariable TermsType type) {
        return ResponseEntity.ok(termsService.getTerms(type));
    }
}
