package com.linclean.auth.jwt;

import com.linclean.auth.exception.InvalidTokenException;
import com.linclean.domain.member.entity.Member;
import com.linclean.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtProviderTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";

    private JwtProvider jwtProvider;
    private Member testMember;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(new JwtProperties(SECRET, 1800L, 1209600L));
        testMember = Member.builder().kakaoId("12345678").build();
        ReflectionTestUtils.setField(testMember, "id", 1L);
        ReflectionTestUtils.setField(testMember, "publicId", UUID.randomUUID());
    }

    @Test
    @DisplayName("Access Token 생성 및 파싱 성공")
    void createAndParseAccessToken() {
        String token = jwtProvider.createAccessToken(testMember);
        Claims claims = jwtProvider.parseAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo(testMember.getPublicId().toString());
        assertThat(((Number) claims.get("mid")).longValue()).isEqualTo(1L);
        assertThat(claims.get("type", String.class)).isEqualTo("access");
    }

    @Test
    @DisplayName("Refresh Token 생성 및 파싱 성공 - jti 포함")
    void createAndParseRefreshToken() {
        String token = jwtProvider.createRefreshToken(testMember);
        Claims claims = jwtProvider.parseRefreshToken(token);

        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
        assertThat(claims.getId()).isNotNull();
    }

    @Test
    @DisplayName("Refresh Token을 Access Token으로 파싱 시 INVALID_TOKEN")
    void parseRefreshAsAccess_fails() {
        String refreshToken = jwtProvider.createRefreshToken(testMember);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("위조된 토큰 파싱 시 INVALID_TOKEN")
    void parseTampered_fails() {
        String token = jwtProvider.createAccessToken(testMember) + "tampered";

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(token))
                .isInstanceOf(InvalidTokenException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("만료된 토큰 파싱 시 TOKEN_EXPIRED")
    void parseExpired_fails() {
        JwtProvider shortProvider = new JwtProvider(new JwtProperties(SECRET, 0L, 0L));
        String token = shortProvider.createAccessToken(testMember);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(token))
                .isInstanceOf(InvalidTokenException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }
}
