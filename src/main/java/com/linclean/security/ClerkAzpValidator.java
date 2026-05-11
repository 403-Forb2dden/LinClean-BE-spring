package com.linclean.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class ClerkAzpValidator implements OAuth2TokenValidator<Jwt> {

    private final List<String> allowedParties;

    public ClerkAzpValidator(List<String> allowedParties) {
        this.allowedParties = allowedParties.stream()
                .filter(allowedParty -> !allowedParty.isBlank())
                .toList();
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (allowedParties.isEmpty()) {
            return OAuth2TokenValidatorResult.success();
        }

        String azp = jwt.getClaimAsString("azp");
        if (azp == null || !allowedParties.contains(azp)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid authorized party: " + azp, null)
            );
        }
        return OAuth2TokenValidatorResult.success();
    }
}
