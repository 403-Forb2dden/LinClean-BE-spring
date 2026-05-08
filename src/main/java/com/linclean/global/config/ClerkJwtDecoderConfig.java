package com.linclean.global.config;

import com.linclean.security.ClerkAzpValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ClerkJwtDecoderConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwksUri;

    @Value("${clerk.allowed-azp}")
    private String allowedAzpRaw;

    @Bean
    public JwtDecoder jwtDecoder() {
        List<String> allowedParties = Arrays.stream(allowedAzpRaw.split(","))
                .map(String::trim)
                .toList();

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                new ClerkAzpValidator(allowedParties)
        ));
        return decoder;
    }
}
