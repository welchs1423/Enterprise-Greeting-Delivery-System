package com.egds.web.dto;

/**
 * Response body for the JWT token issuance endpoint
 * ({@code POST /api/v1/auth/token}).
 * Contains the signed JWT bearer token and its configured expiration.
 */
public final class TokenResponse {

    /** The signed JWT bearer token string. */
    private final String token;

    /** Token type; always "Bearer" for JWT. */
    private final String tokenType = "Bearer";

    /** Token expiration duration in milliseconds. */
    private final long expiresInMs;

    /**
     * Constructs a TokenResponse with the given token and expiration.
     *
     * @param tokenValue     the signed JWT string
     * @param expiresInMsVal the expiration duration in milliseconds
     */
    public TokenResponse(
            final String tokenValue, final long expiresInMsVal) {
        this.token = tokenValue;
        this.expiresInMs = expiresInMsVal;
    }

    /**
     * Returns the signed JWT bearer token string.
     *
     * @return the token string
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the token type.
     *
     * @return always "Bearer"
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns the token expiration duration in milliseconds.
     *
     * @return the expiration duration
     */
    public long getExpiresInMs() {
        return expiresInMs;
    }
}
