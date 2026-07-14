package com.linclean.domain.analysis.repository;

import com.linclean.domain.analysis.entity.Analysis;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AnalysisRepositoryTest {

    private static final UUID START_CURSOR = new UUID(0L, 0L);
    private static final PageRequest PAGE = PageRequest.of(0, 100);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("linclean_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    AnalysisRepository analysisRepository;

    @Autowired
    EntityManager em;

    private long memberId;

    @BeforeEach
    void insertMember() {
        em.createNativeQuery("""
                INSERT INTO member (public_id, clerk_id, created_at, updated_at, deleted_at)
                VALUES (gen_random_uuid(), 'recheck-repo-test', now(), now(), NULL)
                """).executeUpdate();
        memberId = ((Number) em.createNativeQuery(
                "SELECT id FROM member WHERE clerk_id = 'recheck-repo-test'"
        ).getSingleResult()).longValue();
    }

    private void insertAnalysis(UUID id, String status, String analyzedAtExpr, String lastCheckedExpr) {
        em.createNativeQuery("""
                INSERT INTO analysis
                    (analysis_id, version, member_id, original_url, status, verdict, score, summary,
                     analyzed_at, last_checked_at, created_at, updated_at)
                VALUES ('%s'::uuid, 0, %d, 'https://%s.com', '%s', 'safe', 90, 'ok', %s, %s, now(), now())
                """.formatted(id, memberId, id, status, analyzedAtExpr, lastCheckedExpr))
                .executeUpdate();
    }

    private void insertSavedLink(UUID analysisId, String title) {
        em.createNativeQuery("""
                INSERT INTO saved_link (member_id, analysis_id, title, is_bookmarked, created_at, updated_at)
                VALUES (%d, '%s'::uuid, '%s', false, now(), now())
                """.formatted(memberId, analysisId, title))
                .executeUpdate();
    }

    @Nested
    class findStaleSavedAnalyses {

        @Test
        @Transactional
        void 저장링크가_참조하고_마지막_검사후_N일_지난_SUCCEEDED_분석만_조회한다() {
            UUID staleByAnalyzedAt = UUID.randomUUID();  // last_checked_at 없음 → analyzed_at 폴백으로 stale
            UUID staleByLastChecked = UUID.randomUUID(); // last_checked_at 10일 전 → stale
            UUID staleFailed = UUID.randomUUID();        // FAILED(지난 재검사 실패) → 재시도 대상으로 포함
            UUID fresh = UUID.randomUUID();              // last_checked_at 1일 전 → 최신
            UUID notSaved = UUID.randomUUID();           // 저장 링크 없음 → 제외
            UUID queued = UUID.randomUUID();             // QUEUED → 제외

            insertAnalysis(staleByAnalyzedAt, "succeeded", "now() - INTERVAL '10 days'", "NULL");
            insertAnalysis(staleByLastChecked, "succeeded", "now() - INTERVAL '10 days'", "now() - INTERVAL '10 days'");
            insertAnalysis(staleFailed, "failed", "now() - INTERVAL '10 days'", "NULL");
            insertAnalysis(fresh, "succeeded", "now() - INTERVAL '10 days'", "now() - INTERVAL '1 day'");
            insertAnalysis(notSaved, "succeeded", "now() - INTERVAL '10 days'", "NULL");
            insertAnalysis(queued, "queued", "now() - INTERVAL '10 days'", "NULL");

            insertSavedLink(staleByAnalyzedAt, "link-1");
            insertSavedLink(staleByLastChecked, "link-2");
            insertSavedLink(staleFailed, "link-3");
            insertSavedLink(fresh, "link-4");
            insertSavedLink(queued, "link-5");
            em.flush();

            Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);
            Slice<Analysis> result = analysisRepository.findStaleSavedAnalyses(threshold, START_CURSOR, PAGE);

            assertThat(result.getContent())
                    .extracting(Analysis::getAnalysisId)
                    .containsExactlyInAnyOrder(staleByAnalyzedAt, staleByLastChecked, staleFailed);
        }

        @Test
        @Transactional
        void 커서보다_큰_analysisId만_조회하여_keyset_페이지네이션을_지원한다() {
            UUID a = UUID.randomUUID();
            UUID b = UUID.randomUUID();
            insertAnalysis(a, "succeeded", "now() - INTERVAL '10 days'", "NULL");
            insertAnalysis(b, "succeeded", "now() - INTERVAL '10 days'", "NULL");
            insertSavedLink(a, "cursor-link-a");
            insertSavedLink(b, "cursor-link-b");
            em.flush();

            Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);

            // DB(Postgres) 정렬 기준으로 결과 순서를 그대로 사용해 커서 의미를 검증한다.
            List<Analysis> all = analysisRepository
                    .findStaleSavedAnalyses(threshold, START_CURSOR, PAGE).getContent();
            assertThat(all).hasSize(2);

            UUID first = all.get(0).getAnalysisId();
            UUID second = all.get(1).getAnalysisId();

            // 첫 항목 커서 이후 → 두 번째만
            assertThat(analysisRepository.findStaleSavedAnalyses(threshold, first, PAGE).getContent())
                    .extracting(Analysis::getAnalysisId)
                    .containsExactly(second);

            // 마지막 항목 커서 이후 → 없음
            assertThat(analysisRepository.findStaleSavedAnalyses(threshold, second, PAGE).getContent())
                    .isEmpty();
        }
    }
}
