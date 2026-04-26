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
        String kakaoId = kakaoOAuthService.getKakaoId(request.kakaoAccessToken());

        var existing = memberRepository.findByKakaoId(kakaoId);
        Member member = existing.orElseGet(() ->
                memberRepository.save(Member.builder().kakaoId(kakaoId).build()));
        boolean isNewMember = existing.isEmpty();

        return issueTokens(member, isNewMember);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshRequest request) {
        Claims claims = jwtProvider.parseRefreshToken(request.refreshToken());
        Long memberId = ((Number) claims.get("mid")).longValue();
        String jti = jwtProvider.extractJti(claims);

        boolean deleted = refreshTokenRepository
                .findAndDeleteByMemberIdAndJti(memberId, jti).isPresent();

        if (!deleted) {
            log.warn("Refresh Token 재사용 공격 감지! memberId={}, jti={}", memberId, jti);
            refreshTokenRepository.deleteAllByMemberId(memberId);
            throw new InvalidTokenException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidTokenException(ErrorCode.INVALID_TOKEN));

        return issueTokens(member, null);
    }

    public void logout(Long memberId) {
        refreshTokenRepository.deleteAllByMemberId(memberId);
        log.info("로그아웃 완료 - memberId={}", memberId);
    }

    private TokenResponse issueTokens(Member member, Boolean isNewMember) {
        String accessToken = jwtProvider.createAccessToken(member);
        String refreshToken = jwtProvider.createRefreshToken(member);

        Claims refreshClaims = jwtProvider.parseRefreshToken(refreshToken);
        String jti = jwtProvider.extractJti(refreshClaims);

        Duration ttl = Duration.ofSeconds(jwtProperties.refreshTokenExpiry());
        refreshTokenRepository.save(member.getId(), jti, member.getPublicId(), ttl);

        if (isNewMember == null) {
            return TokenResponse.ofRefresh(accessToken, refreshToken, jwtProperties.accessTokenExpiry());
        }
        return TokenResponse.ofLogin(accessToken, refreshToken, jwtProperties.accessTokenExpiry(), isNewMember);
    }
}
