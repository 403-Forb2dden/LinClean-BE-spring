package com.linclean.domain.analysis.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Verdict {
    SAFE("safe"),
    CAUTION("caution"),
    DANGER("danger");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Verdict from(String value) {
        for (Verdict verdict : values()) {
            if (verdict.value.equals(value)) {
                return verdict;
            }
        }
        throw new IllegalArgumentException("Unknown Verdict: " + value);
    }
}
