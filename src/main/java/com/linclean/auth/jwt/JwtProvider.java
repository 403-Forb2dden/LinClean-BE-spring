package com.linclean.auth.jwt;

import com.linclean.auth.exception.InvalidTokenException;
import com.linclean.domain.member.entity.Member;
import com.linclean.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {

    private static final String CLAIM_MEMBER_ID = "mid";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessTokenExpirySeconds;
    private final long refreshTokenExpirySeconds;

    public JwtProvider(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(
                properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirySeconds = properties.accessTokenExpiry();
        this.refreshTokenExpirySeconds = properties.refreshTokenExpiry();
    }

    public String createAccessToken(Member member) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(member.getPublicId().toString())
                .claim(CLAIM_MEMBER_ID, member.getId())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpirySeconds)))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Member member) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(member.getPublicId().toString())
                .claim(CLAIM_MEMBER_ID, member.getId())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenExpirySeconds)))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    public String extractJti(Claims claims) {
        return claims.getId();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(ErrorCode.TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException |
                 IllegalArgumentException e) {
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
        }
    }
}
