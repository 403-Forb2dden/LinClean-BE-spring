package com.linclean.auth.service;

import com.linclean.auth.client.KakaoApiClient;
import com.linclean.auth.dto.request.KakaoLoginRequest;
import com.linclean.auth.dto.request.RefreshRequest;
import com.linclean.auth.dto.response.KakaoUserResponse;
import com.linclean.auth.dto.response.TokenResponse;
import com.linclean.auth.exception.InvalidTokenException;
import com.linclean.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AuthServiceIntegrationTest {

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

    @Autowired AuthService authService;
    @MockBean KakaoApiClient kakaoApiClient;

    @Test
    @DisplayName("신규 회원 카카오 로그인 - isNewMember=true")
    void loginWithKakao_newMember() {
        given(kakaoApiClient.getUserInfo(anyString()))
                .willReturn(new KakaoUserResponse(999L, "2024-01-01T00:00:00Z"));

        TokenResponse response = authService.loginWithKakao(
                new KakaoLoginRequest("valid-kakao-token"));

        assertThat(response.isNewMember()).isTrue();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("기존 회원 카카오 로그인 - isNewMember=false")
    void loginWithKakao_existingMember() {
        given(kakaoApiClient.getUserInfo(anyString()))
                .willReturn(new KakaoUserResponse(888L, "2024-01-01T00:00:00Z"));

        authService.loginWithKakao(new KakaoLoginRequest("token1"));
        TokenResponse response = authService.loginWithKakao(new KakaoLoginRequest("token2"));

        assertThat(response.isNewMember()).isFalse();
    }

    @Test
    @DisplayName("RTR - 정상 재발급: 새 RT 발급됨")
    void refresh_success() {
        given(kakaoApiClient.getUserInfo(anyString()))
                .willReturn(new KakaoUserResponse(777L, "2024-01-01T00:00:00Z"));

        TokenResponse loginResponse = authService.loginWithKakao(
                new KakaoLoginRequest("token"));
        TokenResponse refreshResponse = authService.refresh(
                new RefreshRequest(loginResponse.refreshToken()));

        assertThat(refreshResponse.accessToken()).isNotBlank();
        assertThat(refreshResponse.refreshToken())
                .isNotEqualTo(loginResponse.refreshToken());
    }

    @Test
    @DisplayName("RTR - 같은 RT 재사용 시 REFRESH_TOKEN_REUSE_DETECTED")
    void refresh_reuseDetected() {
        given(kakaoApiClient.getUserInfo(anyString()))
                .willReturn(new KakaoUserResponse(666L, "2024-01-01T00:00:00Z"));

        TokenResponse loginResponse = authService.loginWithKakao(
                new KakaoLoginRequest("token"));
        String oldRefreshToken = loginResponse.refreshToken();

        authService.refresh(new RefreshRequest(oldRefreshToken));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(oldRefreshToken)))
                .isInstanceOf(InvalidTokenException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
    }
}
