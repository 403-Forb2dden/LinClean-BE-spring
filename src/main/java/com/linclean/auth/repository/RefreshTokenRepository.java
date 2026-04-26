package com.linclean.auth.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";
    private static final String KEY_FORMAT = "refresh:%d:%s"; // refresh:{memberId}:{jti}

    private final StringRedisTemplate redisTemplate;

    public void save(Long memberId, String jti, UUID publicId, Duration ttl) {
        String key = buildKey(memberId, jti);
        redisTemplate.opsForValue().set(key, publicId.toString(), ttl);
    }

    public Optional<String> findPublicIdByMemberIdAndJti(Long memberId, String jti) {
        String key = buildKey(memberId, jti);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    /** jti 확인과 동시에 삭제 (원자적 GETDEL). RTR의 1회성 보장. */
    public Optional<String> findAndDeleteByMemberIdAndJti(Long memberId, String jti) {
        String key = buildKey(memberId, jti);
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(key));
    }

    public void deleteByMemberIdAndJti(Long memberId, String jti) {
        redisTemplate.delete(buildKey(memberId, jti));
    }

    /** 멤버의 모든 RT 삭제 (로그아웃, 재사용 공격 감지). SCAN 사용. */
    public void deleteAllByMemberId(Long memberId) {
        String pattern = KEY_PREFIX + memberId + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        List<String> keysToDelete = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(keysToDelete::add);
        }

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("멤버 {}의 Refresh Token {} 개 삭제", memberId, keysToDelete.size());
        }
    }

    private String buildKey(Long memberId, String jti) {
        return String.format(KEY_FORMAT, memberId, jti);
    }
}
