package com.linclean.domain.terms.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ContentFormat {
    MARKDOWN("markdown"),
    HTML("html"),
    PLAIN_TEXT("plain_text");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ContentFormat from(String value) {
        for (ContentFormat format : values()) {
            if (format.value.equals(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown ContentFormat: " + value);
    }
}
