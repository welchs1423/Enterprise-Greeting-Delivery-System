package com.egds.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the JWT token issuance endpoint
 * ({@code POST /api/v1/auth/token}).
 * Both fields are mandatory; blank values are rejected by validation.
 */
public final class TokenRequest {

    /** The login username; must not be blank. */
    @NotBlank
    private String username;

    /** The login password; must not be blank. */
    @NotBlank
    private String password;

    /** Required by Jackson for JSON deserialization. */
    public TokenRequest() {
    }

    /**
     * Constructs a TokenRequest with the given credentials.
     *
     * @param user the username value
     * @param pass the password value
     */
    public TokenRequest(final String user, final String pass) {
        this.username = user;
        this.password = pass;
    }

    /**
     * Returns the username.
     *
     * @return the username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param value the new username value
     */
    public void setUsername(final String value) {
        this.username = value;
    }

    /**
     * Returns the password.
     *
     * @return the password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param value the new password value
     */
    public void setPassword(final String value) {
        this.password = value;
    }
}
