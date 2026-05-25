package com.linclean.domain.analysis.service;

import com.linclean.domain.analysis.dto.response.VerdictStatisticsResponse;
import com.linclean.domain.analysis.entity.Verdict;
import com.linclean.domain.analysis.repository.AnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class VerdictStatisticsServiceTest {

    @Mock AnalysisRepository analysisRepository;
    @Mock StringRedisTemplate redisTemplate;
    @InjectMocks VerdictStatisticsService verdictStatisticsService;

    private HashOperations<String, String, String> hashOps;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        hashOps = mock(HashOperations.class);
    }

    @Nested
    class GetVerdictStatistics {

        @BeforeEach
        void setUpHashOps() {
            doReturn(hashOps).when(redisTemplate).opsForHash();
        }

        @Test
        void cacheHit_returnsFromRedisWithoutDbCall() {
            given(hashOps.entries("analysis:verdict:counts"))
                    .willReturn(Map.of("SAFE", "100", "CAUTION", "20", "DANGER", "5"));

            VerdictStatisticsResponse response = verdictStatisticsService.getVerdictStatistics();

            assertThat(response.getSafe()).isEqualTo(100);
            assertThat(response.getCaution()).isEqualTo(20);
            assertThat(response.getDanger()).isEqualTo(5);
            then(analysisRepository).shouldHaveNoInteractions();
        }

        @Test
        void cacheMiss_queriesDbAndCachesResult() {
            List<Object[]> dbResult = List.<Object[]>of(
                    new Object[]{Verdict.SAFE, 200L},
                    new Object[]{Verdict.CAUTION, 30L},
                    new Object[]{Verdict.DANGER, 10L}
            );
            given(hashOps.entries("analysis:verdict:counts")).willReturn(Map.of());
            given(analysisRepository.countGroupByVerdict()).willReturn(dbResult);

            VerdictStatisticsResponse response = verdictStatisticsService.getVerdictStatistics();

            assertThat(response.getSafe()).isEqualTo(200);
            assertThat(response.getCaution()).isEqualTo(30);
            assertThat(response.getDanger()).isEqualTo(10);
            then(redisTemplate).should().executePipelined(any(RedisCallback.class));
        }

        @Test
        void cacheMiss_partialDbResult_zeroForMissingVerdict() {
            given(hashOps.entries("analysis:verdict:counts")).willReturn(Map.of());
            given(analysisRepository.countGroupByVerdict())
                    .willReturn(Collections.singletonList(new Object[]{Verdict.SAFE, 50L}));

            VerdictStatisticsResponse response = verdictStatisticsService.getVerdictStatistics();

            assertThat(response.getSafe()).isEqualTo(50);
            assertThat(response.getCaution()).isEqualTo(0);
            assertThat(response.getDanger()).isEqualTo(0);
        }

        @Test
        void redisReadFailure_fallsBackToDbDirectly() {
            given(hashOps.entries("analysis:verdict:counts"))
                    .willThrow(new RuntimeException("Redis connection refused"));
            given(analysisRepository.countGroupByVerdict())
                    .willReturn(Collections.singletonList(new Object[]{Verdict.SAFE, 10L}));

            VerdictStatisticsResponse response = verdictStatisticsService.getVerdictStatistics();

            assertThat(response.getSafe()).isEqualTo(10);
            then(redisTemplate).should(never()).executePipelined(any(RedisCallback.class));
        }

        @Test
        void redisWriteFailure_returnsDbResultWithoutThrowing() {
            given(hashOps.entries("analysis:verdict:counts")).willReturn(Map.of());
            given(analysisRepository.countGroupByVerdict())
                    .willReturn(Collections.singletonList(new Object[]{Verdict.DANGER, 3L}));
            given(redisTemplate.executePipelined(any(RedisCallback.class)))
                    .willThrow(new RuntimeException("Redis write failed"));

            VerdictStatisticsResponse response = verdictStatisticsService.getVerdictStatistics();

            assertThat(response.getDanger()).isEqualTo(3);
            assertThat(response.getSafe()).isEqualTo(0);
            assertThat(response.getCaution()).isEqualTo(0);
        }
    }

    @Nested
    class RefreshCache {

        @Test
        void scheduledRefresh_queriesDbAndCaches() {
            given(analysisRepository.countGroupByVerdict())
                    .willReturn(Collections.singletonList(new Object[]{Verdict.SAFE, 100L}));

            verdictStatisticsService.scheduledRefresh();

            then(analysisRepository).should().countGroupByVerdict();
            then(redisTemplate).should().executePipelined(any(RedisCallback.class));
        }

        @Test
        void scheduledRefresh_dbFailure_doesNotThrow() {
            given(analysisRepository.countGroupByVerdict())
                    .willThrow(new RuntimeException("DB down"));

            verdictStatisticsService.scheduledRefresh();
        }

        @Test
        void onApplicationReady_queriesDbAndCaches() {
            given(analysisRepository.countGroupByVerdict()).willReturn(List.of());

            verdictStatisticsService.onApplicationReady();

            then(analysisRepository).should().countGroupByVerdict();
        }
    }
}
