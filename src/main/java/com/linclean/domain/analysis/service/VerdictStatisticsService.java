package com.linclean.domain.analysis.service;

import com.linclean.domain.analysis.dto.response.VerdictStatisticsResponse;
import com.linclean.domain.analysis.entity.Verdict;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerdictStatisticsService {

    private static final String CACHE_KEY = "analysis:verdict:counts";
    private static final Duration CACHE_TTL = Duration.ofMinutes(90);

    private final AnalysisRepository analysisRepository;
    private final StringRedisTemplate redisTemplate;

    public VerdictStatisticsResponse getVerdictStatistics() {
        try {
            HashOperations<String, String, String> ops = redisTemplate.opsForHash();
            Map<String, String> cached = ops.entries(CACHE_KEY);
            if (!cached.isEmpty()) {
                return VerdictStatisticsResponse.from(cached);
            }
        } catch (Exception e) {
            log.warn("Redis 조회 실패, DB fallback 수행", e);
            return VerdictStatisticsResponse.from(analysisRepository.countGroupByVerdict());
        }
        return queryAndCache();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshCache();
    }

    @Scheduled(fixedRate = 50, initialDelay = 50, timeUnit = TimeUnit.MINUTES)
    public void scheduledRefresh() {
        refreshCache();
    }

    private void refreshCache() {
        log.info("verdict 통계 캐시 갱신 시작");
        try {
            queryAndCache();
            log.info("verdict 통계 캐시 갱신 완료");
        } catch (Exception e) {
            log.error("verdict 통계 캐시 갱신 실패", e);
        }
    }

    private VerdictStatisticsResponse queryAndCache() {
        List<Object[]> dbResult = analysisRepository.countGroupByVerdict();
        VerdictStatisticsResponse response = VerdictStatisticsResponse.from(dbResult);

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put(Verdict.SAFE.name(), String.valueOf(response.getSafe()));
        hashMap.put(Verdict.CAUTION.name(), String.valueOf(response.getCaution()));
        hashMap.put(Verdict.DANGER.name(), String.valueOf(response.getDanger()));

        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                StringRedisConnection conn = (StringRedisConnection) connection;
                conn.hMSet(CACHE_KEY, hashMap);
                conn.expire(CACHE_KEY, CACHE_TTL.getSeconds());
                return null;
            });
        } catch (Exception e) {
            log.warn("Redis 캐시 쓰기 실패, DB 결과 반환", e);
        }

        return response;
    }
}
