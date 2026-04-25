package com.linclean.auth.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Boolean isNewMember
) {
    public static TokenResponse ofLogin(
            String accessToken, String refreshToken, long expiresIn, boolean isNewMember) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, isNewMember);
    }

    public static TokenResponse ofRefresh(
            String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, null);
    }
}
