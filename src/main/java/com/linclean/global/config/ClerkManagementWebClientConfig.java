package com.linclean.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class ClerkManagementWebClientConfig {

    @Value("${clerk.secret-key}")
    private String clerkSecretKey;

    @Value("${clerk.management-api-base-url}")
    private String clerkManagementApiBaseUrl;

    @Bean
    public WebClient clerkManagementWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(clerkManagementApiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + clerkSecretKey)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
