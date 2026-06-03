package com.linclean.global.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * ERROR 로그를 디스코드 웹훅으로 실시간 전송하는 logback appender.
 *
 * <p>logback-spring.xml 에서 {@code <webhookUrl>${DISCORD_WEBHOOK_URL:-}</webhookUrl>} 로 주입한다.
 * URL이 비어 있으면 아무 동작도 하지 않으므로(로컬/미설정 환경) 안전하다.
 * Spring 컨텍스트보다 먼저 동작하므로 빈 주입 대신 표준 {@link HttpClient}를 사용한다.
 *
 * <p>전송은 {@link HttpClient#sendAsync} 로 비동기 처리하며, 디스코드 분당 30회 한도를
 * 넘기지 않도록 분당 {@code maxPerMinute}건으로 제한(초과분은 드롭)한다.
 */
public class DiscordWebhookAppender extends AppenderBase<ILoggingEvent> {

    /** 디스코드 content 최대 길이 2000자. 코드블록/여유분 고려해 보수적으로 자른다. */
    private static final int MAX_CONTENT = 1900;
    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String webhookUrl;
    private int maxPerMinute = 20;

    private HttpClient httpClient;

    // 분당 호출 제한 상태
    private long windowStartMs = 0;
    private int sentInWindow = 0;

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void setMaxPerMinute(int maxPerMinute) {
        this.maxPerMinute = maxPerMinute;
    }

    @Override
    public void start() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return; // 미설정 환경에서는 no-op
        }
        if (!allow()) {
            return; // 분당 제한 초과 → 드롭
        }
        try {
            String payload = buildPayload(event);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            // 비동기 전송 - 실패해도 애플리케이션 로깅 흐름에 영향 주지 않음
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> null);
        } catch (Exception e) {
            // appender 는 절대 예외를 던지지 않는다
            addError("디스코드 웹훅 전송 실패", e);
        }
    }

    private synchronized boolean allow() {
        long now = System.currentTimeMillis();
        if (now - windowStartMs >= 60_000) {
            windowStartMs = now;
            sentInWindow = 0;
        }
        if (sentInWindow < maxPerMinute) {
            sentInWindow++;
            return true;
        }
        return false;
    }

    private String buildPayload(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 **[ERROR]** ").append(TS_FORMAT.format(Instant.ofEpochMilli(event.getTimeStamp())))
                .append('\n')
                .append("**logger**: ").append(event.getLoggerName()).append('\n')
                .append("**message**: ").append(event.getFormattedMessage());

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            sb.append("\n```\n")
                    .append(ThrowableProxyUtil.asString(throwableProxy))
                    .append("\n```");
        }

        String content = sb.toString();
        if (content.length() > MAX_CONTENT) {
            content = content.substring(0, MAX_CONTENT) + "\n...(생략)";
        }

        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("content", content);
        return node.toString();
    }
}
