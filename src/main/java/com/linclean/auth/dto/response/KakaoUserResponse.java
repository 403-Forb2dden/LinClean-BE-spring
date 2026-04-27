package com.linclean.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,
        @JsonProperty("connected_at") String connectedAt
) {
    public String kakaoId() {
        return String.valueOf(id);
    }
}
