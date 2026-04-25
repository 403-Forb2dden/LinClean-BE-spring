package com.linclean.auth.client;

import com.linclean.auth.dto.response.KakaoUserResponse;
import com.linclean.auth.exception.KakaoAuthException;
import com.linclean.global.exception.ErrorCode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class KakaoApiClientTest {

    private MockWebServer mockWebServer;
    private KakaoApiClient kakaoApiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        kakaoApiClient = new KakaoApiClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("정상 응답 - 카카오 사용자 정보 반환")
    void getUserInfo_success() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\": 123456789, \"connected_at\": \"2024-01-01T00:00:00Z\"}"));

        KakaoUserResponse result = kakaoApiClient.getUserInfo("valid-token");

        assertThat(result.id()).isEqualTo(123456789L);
        assertThat(result.kakaoId()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("401 응답 - INVALID_KAKAO_TOKEN 예외")
    void getUserInfo_401_throwsInvalidKakaoToken() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        assertThatThrownBy(() -> kakaoApiClient.getUserInfo("invalid-token"))
                .isInstanceOf(KakaoAuthException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_KAKAO_TOKEN);
    }

    @Test
    @DisplayName("500 응답 - KAKAO_SERVER_ERROR 예외")
    void getUserInfo_500_throwsKakaoServerError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> kakaoApiClient.getUserInfo("some-token"))
                .isInstanceOf(KakaoAuthException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.KAKAO_SERVER_ERROR);
    }
}
