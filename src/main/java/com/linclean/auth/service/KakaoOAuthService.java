package com.linclean.auth.service;

import com.linclean.auth.client.KakaoApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final KakaoApiClient kakaoApiClient;

    public String getKakaoId(String kakaoAccessToken) {
        return kakaoApiClient.getUserInfo(kakaoAccessToken).kakaoId();
    }
}
