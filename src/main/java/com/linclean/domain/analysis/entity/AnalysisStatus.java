package com.linclean.domain.analysis.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AnalysisStatus {
    QUEUED("queued"),
    SUCCEEDED("succeeded"),
    FAILED("failed");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AnalysisStatus from(String value) {
        for (AnalysisStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AnalysisStatus: " + value);
    }
}
