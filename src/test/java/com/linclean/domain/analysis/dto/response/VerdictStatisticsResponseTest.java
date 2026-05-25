package com.linclean.domain.analysis.dto.response;

import com.linclean.domain.analysis.entity.Verdict;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VerdictStatisticsResponseTest {

    @Nested
    class FromRedisHash {

        @Test
        void allVerdictsPresent_mapsCorrectly() {
            Map<String, String> hash = Map.of("SAFE", "100", "CAUTION", "20", "DANGER", "5");

            VerdictStatisticsResponse response = VerdictStatisticsResponse.from(hash);

            assertThat(response.getSafe()).isEqualTo(100);
            assertThat(response.getCaution()).isEqualTo(20);
            assertThat(response.getDanger()).isEqualTo(5);
        }

        @Test
        void missingKey_returnsZero() {
            Map<String, String> hash = Map.of("SAFE", "50");

            VerdictStatisticsResponse response = VerdictStatisticsResponse.from(hash);

            assertThat(response.getSafe()).isEqualTo(50);
            assertThat(response.getCaution()).isEqualTo(0);
            assertThat(response.getDanger()).isEqualTo(0);
        }

        @Test
        void emptyString_returnsZero() {
            Map<String, String> hash = Map.of("SAFE", "10", "CAUTION", "", "DANGER", "5");

            VerdictStatisticsResponse response = VerdictStatisticsResponse.from(hash);

            assertThat(response.getCaution()).isEqualTo(0);
        }

        @Test
        void nonNumericValue_returnsZero() {
            Map<String, String> hash = Map.of("SAFE", "invalid", "CAUTION", "20", "DANGER", "5");

            VerdictStatisticsResponse response = VerdictStatisticsResponse.from(hash);

            assertThat(response.getSafe()).isEqualTo(0);
        }
    }

    @Nested
    class FromDbResult {

        @Test
        void allVerdictsPresent_mapsCorrectly() {
            List<Object[]> dbResult = List.<Object[]>of(
                    new Object[]{Verdict.SAFE, 100L},
                    new Object[]{Verdict.CAUTION, 20L},
                    new Object[]{Verdict.DANGER, 5L}
            );

            VerdictStatisticsResponse response = VerdictStatisticsResponse.from(dbResult);

            assertThat(response.getSafe()).isEqualTo(100);
            assertThat(response.getCaution()).isEqualTo(20);
            assertThat(response.getDanger()).isEqualTo(5);
        }

        @Test
        void emptyList_allZero() {
            VerdictStatisticsResponse response = VerdictStatisticsResponse.from(List.of());

            assertThat(response.getSafe()).isEqualTo(0);
            assertThat(response.getCaution()).isEqualTo(0);
            assertThat(response.getDanger()).isEqualTo(0);
        }

        @Test
        void partialResult_missingVerdictIsZero() {
            List<Object[]> dbResult = Collections.singletonList(new Object[]{Verdict.DANGER, 3L});

            VerdictStatisticsResponse response = VerdictStatisticsResponse.from(dbResult);

            assertThat(response.getDanger()).isEqualTo(3);
            assertThat(response.getSafe()).isEqualTo(0);
            assertThat(response.getCaution()).isEqualTo(0);
        }
    }
}
