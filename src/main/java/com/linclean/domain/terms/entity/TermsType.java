package com.linclean.domain.terms.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TermsType {
    TERMS_OF_SERVICE("terms_of_service"),
    PRIVACY_POLICY("privacy_policy"),
    SERVICE_GUIDE("service_guide");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TermsType from(String value) {
        for (TermsType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TermsType: " + value);
    }
}
