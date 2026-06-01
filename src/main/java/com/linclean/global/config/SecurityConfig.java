package com.linclean.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linclean.global.security.InternalApiKeyFilter;
import com.linclean.security.ClerkJwtAuthenticationConverter;
import com.linclean.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final ClerkJwtAuthenticationConverter clerkJwtAuthenticationConverter;
    private final ObjectMapper objectMapper;

    @Value("${internal.api-key}")
    private String internalApiKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/terms/**",
                                "/api/v1/analyses/statistics"
                        ).permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/clerk-test.html").permitAll()
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(clerkJwtAuthenticationConverter))
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(
                        new InternalApiKeyFilter(internalApiKey, objectMapper),
                        BearerTokenAuthenticationFilter.class
                );

        return http.build();
    }
}
