package com.linclean.auth.service;

import com.linclean.auth.dto.request.KakaoLoginRequest;
import com.linclean.auth.dto.request.RefreshRequest;
import com.linclean.auth.dto.response.TokenResponse;
import com.linclean.auth.exception.InvalidTokenException;
import com.linclean.auth.jwt.JwtProperties;
import com.linclean.auth.jwt.JwtProvider;
import com.linclean.auth.repository.RefreshTokenRepository;
import com.linclean.domain.member.entity.Member;
import com.linclean.domain.member.repository.MemberRepository;
import com.linclean.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public TokenResponse loginWithKakao(KakaoLoginRequest request) {
        // 카카오 토큰으로 kakaoId 조회 (토큰 위변조 검증 포함)
        String kakaoId = kakaoOAuthService.getKakaoId(request.kakaoAccessToken());

        // 기존 회원이면 재사용, 없으면 신규 가입 처리
        var existing = memberRepository.findByKakaoId(kakaoId);
        Member member = existing.orElseGet(() ->
                memberRepository.save(Member.builder().kakaoId(kakaoId).build()));
        boolean isNewMember = existing.isEmpty();

        return issueTokensForLogin(member, isNewMember);
    }

    public TokenResponse refresh(RefreshRequest request) {
        Claims claims = jwtProvider.parseRefreshToken(request.refreshToken());
        Long memberId = jwtProvider.extractMemberId(claims);
        String jti = jwtProvider.extractJti(claims);

        // GETDEL로 원자적 조회+삭제 — 동시 요청이 와도 한 쪽만 성공
        boolean deleted = refreshTokenRepository
                .findAndDeleteByMemberIdAndJti(memberId, jti).isPresent();

        if (!deleted) {
            // Redis에 없는 jti → 이미 사용된 RT 재사용 시도
            // 토큰 탈취 가능성이 있으므로 해당 멤버의 모든 RT를 무효화
            log.warn("Refresh Token 재사용 공격 감지! memberId={}, jti={}", memberId, jti);
            refreshTokenRepository.deleteAllByMemberId(memberId);
            throw new InvalidTokenException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidTokenException(ErrorCode.INVALID_TOKEN));

        // 검증 통과 → 새 AT/RT 발급 (RTR: 기존 RT는 위에서 이미 삭제됨)
        return rotateTokens(member);
    }

    public void logout(Long memberId) {
        refreshTokenRepository.deleteAllByMemberId(memberId);
        log.info("로그아웃 완료 - memberId={}", memberId);
    }

    private TokenResponse issueTokensForLogin(Member member, boolean isNewMember) {
        String[] tokens = generateAndSaveTokens(member);
        return TokenResponse.ofLogin(tokens[0], tokens[1], jwtProperties.accessTokenExpiry(), isNewMember);
    }

    private TokenResponse rotateTokens(Member member) {
        String[] tokens = generateAndSaveTokens(member);
        return TokenResponse.ofRefresh(tokens[0], tokens[1], jwtProperties.accessTokenExpiry());
    }

    private String[] generateAndSaveTokens(Member member) {
        String accessToken = jwtProvider.createAccessToken(member);
        String refreshToken = jwtProvider.createRefreshToken(member);

        // RT의 jti를 Redis key로 사용해 개별 토큰 단위 무효화 지원
        Claims refreshClaims = jwtProvider.parseRefreshToken(refreshToken);
        String jti = jwtProvider.extractJti(refreshClaims);

        Duration ttl = Duration.ofSeconds(jwtProperties.refreshTokenExpiry());
        refreshTokenRepository.save(member.getId(), jti, member.getPublicId(), ttl);

        return new String[]{accessToken, refreshToken};
    }
}
