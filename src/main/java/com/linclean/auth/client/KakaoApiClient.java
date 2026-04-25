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
                .onStatus(
                        status -> status == HttpStatus.UNAUTHORIZED,
                        response -> Mono.error(new KakaoAuthException(ErrorCode.INVALID_KAKAO_TOKEN))
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> Mono.error(new KakaoAuthException(ErrorCode.KAKAO_SERVER_ERROR))
                )
                .bodyToMono(KakaoUserResponse.class)
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
