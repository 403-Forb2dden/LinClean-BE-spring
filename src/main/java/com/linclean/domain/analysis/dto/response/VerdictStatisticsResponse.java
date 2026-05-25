package com.linclean.domain.analysis.dto.response;

import com.linclean.domain.analysis.entity.Verdict;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VerdictStatisticsResponse {

    private final long safe;
    private final long caution;
    private final long danger;

    public static VerdictStatisticsResponse from(Map<String, String> redisHash) {
        return new VerdictStatisticsResponse(
                parseLong(redisHash.get(Verdict.SAFE.name())),
                parseLong(redisHash.get(Verdict.CAUTION.name())),
                parseLong(redisHash.get(Verdict.DANGER.name()))
        );
    }

    public static VerdictStatisticsResponse from(List<Object[]> dbResult) {
        Map<Verdict, Long> counts = new EnumMap<>(Verdict.class);
        for (Verdict v : Verdict.values()) {
            counts.put(v, 0L);
        }
        for (Object[] row : dbResult) {
            counts.put((Verdict) row[0], (Long) row[1]);
        }
        return new VerdictStatisticsResponse(
                counts.get(Verdict.SAFE),
                counts.get(Verdict.CAUTION),
                counts.get(Verdict.DANGER)
        );
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
