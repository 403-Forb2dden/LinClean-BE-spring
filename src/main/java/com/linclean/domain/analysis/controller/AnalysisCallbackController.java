package com.linclean.domain.analysis.controller;

import com.linclean.domain.analysis.dto.callback.AnalysisResultCallback;
import com.linclean.domain.analysis.dto.callback.CallbackReceivedResponse;
import com.linclean.domain.analysis.service.AnalysisCallbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class AnalysisCallbackController {

    private final AnalysisCallbackService callbackService;

    @PostMapping("/analysis-result")
    public ResponseEntity<CallbackReceivedResponse> receiveResult(
            @Valid @RequestBody AnalysisResultCallback callback) {
        callbackService.handleCallback(callback);
        return ResponseEntity.ok(CallbackReceivedResponse.ok());
    }
}
