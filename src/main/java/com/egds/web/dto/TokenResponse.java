package com.egds.web.dto;

/**
 * Response body for the JWT token issuance endpoint ({@code POST /api/v1/auth/token}).
 * Contains the signed JWT bearer token and its configured expiration duration.
 */
public class TokenResponse {

    private final String token;
    private final String tokenType = "Bearer";
    private final long expiresInMs;

    public TokenResponse(String token, long expiresInMs) {
        this.token = token;
        this.expiresInMs = expiresInMs;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public long getExpiresInMs() { return expiresInMs; }
}
