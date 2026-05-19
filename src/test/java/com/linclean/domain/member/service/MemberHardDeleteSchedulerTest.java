package com.linclean.domain.member.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class MemberHardDeleteSchedulerTest {

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
    MemberHardDeleteScheduler scheduler;

    @Autowired
    EntityManager em;

    @Nested
    class hardDeleteExpiredMembers {

        @Test
        @Transactional
        void 보존기간을_초과한_탈퇴회원은_물리삭제된다() {
            em.createNativeQuery("""
                    INSERT INTO member (public_id, clerk_id, created_at, updated_at, deleted_at)
                    VALUES (gen_random_uuid(), 'expired-clerk-id', now(), now(), now() - INTERVAL '31 days')
                    """).executeUpdate();
            em.flush();

            scheduler.hardDeleteExpiredMembers();

            Long count = count("expired-clerk-id");
            assertThat(count).isZero();
        }

        @Test
        @Transactional
        void 보존기간_이내의_탈퇴회원은_삭제되지_않는다() {
            em.createNativeQuery("""
                    INSERT INTO member (public_id, clerk_id, created_at, updated_at, deleted_at)
                    VALUES (gen_random_uuid(), 'recent-clerk-id', now(), now(), now() - INTERVAL '10 days')
                    """).executeUpdate();
            em.flush();

            scheduler.hardDeleteExpiredMembers();

            Long count = count("recent-clerk-id");
            assertThat(count).isOne();
        }

        @Test
        @Transactional
        void 정확히_보존기간_당일인_탈퇴회원은_삭제되지_않는다() {
            em.createNativeQuery("""
                    INSERT INTO member (public_id, clerk_id, created_at, updated_at, deleted_at)
                    VALUES (gen_random_uuid(), 'boundary-clerk-id', now(), now(), now() - INTERVAL '30 days')
                    """).executeUpdate();
            em.flush();

            scheduler.hardDeleteExpiredMembers();

            Long count = count("boundary-clerk-id");
            assertThat(count).isOne();
        }

        @Test
        @Transactional
        void 활성_회원은_삭제되지_않는다() {
            em.createNativeQuery("""
                    INSERT INTO member (public_id, clerk_id, created_at, updated_at, deleted_at)
                    VALUES (gen_random_uuid(), 'active-clerk-id', now(), now(), NULL)
                    """).executeUpdate();
            em.flush();

            scheduler.hardDeleteExpiredMembers();

            Long count = count("active-clerk-id");
            assertThat(count).isOne();
        }

        private Long count(String clerkId) {
            return ((Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM member WHERE clerk_id = '" + clerkId + "'"
            ).getSingleResult()).longValue();
        }
    }
}
