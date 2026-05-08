package com.linclean.domain.analysis.controller;

import com.linclean.security.MemberPrincipal;
import com.linclean.domain.analysis.dto.request.AnalysisRequest;
import com.linclean.domain.analysis.dto.response.AnalysisResponse;
import com.linclean.domain.analysis.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<AnalysisResponse> requestAnalysis(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody AnalysisRequest request) {
        AnalysisResponse response = analysisService.requestAnalysis(principal.memberId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisResponse> getAnalysis(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable UUID analysisId) {
        AnalysisResponse response = analysisService.getAnalysis(principal.memberId(), analysisId);
        return ResponseEntity.ok(response);
    }
}
