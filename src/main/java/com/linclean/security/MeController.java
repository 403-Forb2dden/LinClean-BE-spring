package com.linclean.security;

import com.linclean.global.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class MeController implements MeControllerDocs {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(@AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(new MeResponse(principal.publicId())));
    }

    record MeResponse(UUID publicId) {}
}
