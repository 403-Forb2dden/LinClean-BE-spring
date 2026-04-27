package com.linclean.auth.controller;

import com.linclean.auth.dto.request.KakaoLoginRequest;
import com.linclean.auth.dto.request.RefreshRequest;
import com.linclean.auth.dto.response.TokenResponse;
import com.linclean.auth.jwt.MemberPrincipal;
import com.linclean.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao/login")
    public ResponseEntity<TokenResponse> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithKakao(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal MemberPrincipal principal) {
        authService.logout(principal.memberId());
        return ResponseEntity.noContent().build();
    }
}
