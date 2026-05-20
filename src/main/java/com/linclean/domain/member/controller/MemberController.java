package com.linclean.domain.member.controller;

import com.linclean.domain.member.service.MemberWithdrawalService;
import com.linclean.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberWithdrawalService memberWithdrawalService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal MemberPrincipal principal) {
        memberWithdrawalService.withdraw(principal.memberId());
        return ResponseEntity.noContent().build();
    }
}
