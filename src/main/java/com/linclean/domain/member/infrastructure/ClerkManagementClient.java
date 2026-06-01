package com.linclean.domain.member.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClerkManagementClient {

    private final WebClient clerkManagementWebClient;

    public void deleteUser(String clerkId) {
        try {
            clerkManagementWebClient.delete()
                    .uri("/v1/users/{clerkId}", clerkId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Clerk 계정 삭제 완료 - clerkId={}", clerkId);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("Clerk 계정이 이미 삭제됨 - clerkId={}", clerkId);
                return;
            }
            log.error("Clerk 계정 삭제 실패 - clerkId={}, status={}", clerkId, e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Clerk API 연결 오류 - clerkId={}", clerkId, e);
        }
    }
}
