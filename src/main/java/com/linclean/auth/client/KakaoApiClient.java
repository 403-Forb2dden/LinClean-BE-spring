package com.linclean.auth.client;

import com.linclean.auth.dto.response.KakaoUserResponse;
import com.linclean.auth.exception.KakaoAuthException;
import com.linclean.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoApiClient {

    private static final String USER_ME_PATH = "/v2/user/me";

    private final WebClient kakaoWebClient;

    public KakaoUserResponse getUserInfo(String kakaoAccessToken) {
        return kakaoWebClient.get()
                .uri(USER_ME_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                .retrieve()
                // 401: 클라이언트가 보낸 카카오 토큰이 유효하지 않음
                .onStatus(
                        status -> status == HttpStatus.UNAUTHORIZED,
                        response -> Mono.error(new KakaoAuthException(ErrorCode.INVALID_KAKAO_TOKEN))
                )
                // 5xx: 카카오 서버 자체 문제
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> Mono.error(new KakaoAuthException(ErrorCode.KAKAO_SERVER_ERROR))
                )
                .bodyToMono(KakaoUserResponse.class)
                // 타임아웃, 역직렬화 실패 등 나머지 예외는 모두 서버 오류로 통일
                .onErrorMap(
                        e -> !(e instanceof KakaoAuthException),
                        e -> {
                            log.error("카카오 API 호출 실패: {}", e.getMessage(), e);
                            return new KakaoAuthException(ErrorCode.KAKAO_SERVER_ERROR);
                        }
                )
                .block();
    }
}
